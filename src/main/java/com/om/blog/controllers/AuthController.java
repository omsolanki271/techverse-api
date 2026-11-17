package com.om.blog.controllers;

import com.om.blog.entities.User;
import com.om.blog.payloads.LoginRequest;
import com.om.blog.payloads.LoginResponse;
import com.om.blog.payloads.UserDto;
import com.om.blog.repositories.UserRepo;
import com.om.blog.security.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    private com.om.blog.services.UserService userService;

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

        UserDto userDto;
        try {
            userDto = userService.getUserById(user.getId());
        } catch (Exception e) {
            userDto = modelMapper.map(user, UserDto.class);
        }

        return ResponseEntity.ok(
                new LoginResponse(token, userDto)
        );
    }
}