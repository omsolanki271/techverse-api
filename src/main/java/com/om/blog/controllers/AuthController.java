package com.om.blog.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.om.blog.entities.Role;
import com.om.blog.entities.User;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.GoogleLoginRequest;
import com.om.blog.payloads.LoginRequest;
import com.om.blog.payloads.LoginResponse;
import com.om.blog.payloads.UserDto;
import com.om.blog.repositories.RoleRepo;
import com.om.blog.repositories.UserRepo;
import com.om.blog.security.JwtService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${google.client-id}")
    private String googleClientId;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(request.getEmail());

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDto userDto = modelMapper.map(user, UserDto.class);

        return ResponseEntity.ok(
                new LoginResponse(token, userDto)
        );
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request
    ) {
        GoogleIdToken idToken;
        try {
            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    transport,
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            idToken = verifier.verify(request.getCredential());
        } catch (Exception e) {
            throw new BadCredentialsException("Failed to verify Google token: " + e.getMessage());
        }

        if (idToken == null) {
            throw new BadCredentialsException("Invalid or expired Google token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        Boolean emailVerified = payload.getEmailVerified();
        if (emailVerified == null || !emailVerified) {
            throw new BadCredentialsException("Google email is not verified");
        }

        String email = payload.getEmail();
        String googleId = payload.getSubject();

        User user = userRepo.findByGoogleId(googleId)
                .or(() -> userRepo.findByEmail(email))
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);

                    String name = (String) payload.get("name");
                    if (name == null || name.trim().isEmpty()) {
                        name = email.split("@")[0];
                    }
                    if (name.length() < 4) {
                        name = name + "_user";
                    }
                    newUser.setName(name);
                    newUser.setAbout("TechVerse user authenticated via Google");
                    newUser.setGoogleId(googleId);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

                    Role role = roleRepo.findByName("ROLE_USER")
                            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", 0));
                    Set<Role> roles = new HashSet<>();
                    roles.add(role);
                    newUser.setRoles(roles);

                    return userRepo.save(newUser);
                });

        if (user.getGoogleId() == null || user.getGoogleId().isBlank()) {
            user.setGoogleId(googleId);
            user = userRepo.save(user);
        }

        String token = jwtService.generateToken(user.getEmail());
        UserDto userDto = modelMapper.map(user, UserDto.class);

        return ResponseEntity.ok(
                new LoginResponse(token, userDto)
        );
    }
}