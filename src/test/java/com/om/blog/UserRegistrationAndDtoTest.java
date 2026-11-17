package com.om.blog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.om.blog.entities.Role;
import com.om.blog.entities.User;
import com.om.blog.exceptions.ResourceAlreadyInUseException;
import com.om.blog.payloads.UserDto;
import com.om.blog.repositories.AuthorSubscriptionRepo;
import com.om.blog.repositories.RoleRepo;
import com.om.blog.repositories.UserFollowRepo;
import com.om.blog.repositories.UserRepo;
import com.om.blog.services.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRegistrationAndDtoTest {

    private ObjectMapper objectMapper;
    private UserServiceImpl userService;
    private boolean existsByEmailResult;
    private boolean existsByMobileResult;
    private User savedUserCapture;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "modelMapper", new ModelMapper());

        // Lightweight dynamic proxy for UserRepo without byte-buddy agent attachment
        UserRepo dummyUserRepo = (UserRepo) Proxy.newProxyInstance(
                UserRepo.class.getClassLoader(),
                new Class<?>[]{UserRepo.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("existsByEmail".equals(methodName)) {
                        return existsByEmailResult;
                    }
                    if ("existsByMobileNumber".equals(methodName)) {
                        return existsByMobileResult;
                    }
                    if ("save".equals(methodName)) {
                        User u = (User) args[0];
                        savedUserCapture = u;
                        u.setId(101);
                        return u;
                    }
                    return null;
                }
        );
        ReflectionTestUtils.setField(userService, "userRepo", dummyUserRepo);

        // Lightweight dynamic proxy for RoleRepo
        RoleRepo dummyRoleRepo = (RoleRepo) Proxy.newProxyInstance(
                RoleRepo.class.getClassLoader(),
                new Class<?>[]{RoleRepo.class},
                (proxy, method, args) -> {
                    if ("findByName".equals(method.getName())) {
                        Role r = new Role();
                        r.setId(1);
                        r.setName((String) args[0]);
                        return Optional.of(r);
                    }
                    return Optional.empty();
                }
        );
        ReflectionTestUtils.setField(userService, "roleRepo", dummyRoleRepo);

        // Lightweight PasswordEncoder
        PasswordEncoder dummyEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return "$2a$10$encoded_" + rawPassword;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encodedPassword.equals("$2a$10$encoded_" + rawPassword);
            }
        };
        ReflectionTestUtils.setField(userService, "passwordEncoder", dummyEncoder);

        // Lightweight dynamic proxy for UserFollowRepo
        UserFollowRepo dummyFollowRepo = (UserFollowRepo) Proxy.newProxyInstance(
                UserFollowRepo.class.getClassLoader(),
                new Class<?>[]{UserFollowRepo.class},
                (proxy, method, args) -> 0L
        );
        ReflectionTestUtils.setField(userService, "userFollowRepo", dummyFollowRepo);

        // Lightweight dynamic proxy for AuthorSubscriptionRepo
        AuthorSubscriptionRepo dummySubRepo = (AuthorSubscriptionRepo) Proxy.newProxyInstance(
                AuthorSubscriptionRepo.class.getClassLoader(),
                new Class<?>[]{AuthorSubscriptionRepo.class},
                (proxy, method, args) -> Boolean.FALSE
        );
        ReflectionTestUtils.setField(userService, "authorSubscriptionRepo", dummySubRepo);
    }

    @Test
    @DisplayName("Issue 1: JSON with null followerCount/followingCount/id deserializes successfully without HttpMessageNotReadableException")
    void testUserDtoDeserializationWithNullAndClientFields() throws Exception {
        String json = """
                {
                  "id": 999,
                  "name": "Rahul Sharma",
                  "email": "rahul.sharma@example.com",
                  "password": "Rahul@123",
                  "about": "Java backend developer",
                  "mobileNumber": "9876543210",
                  "address": "Ahmedabad, Gujarat, India",
                  "githubUrl": "https://github.com/rahulsharma",
                  "linkedinUrl": "https://www.linkedin.com/in/rahulsharma",
                  "instaUrl": "https://www.instagram.com/rahulsharma",
                  "followerCount": null,
                  "followingCount": null,
                  "roles": [{"id": 1, "name": "ROLE_ADMIN"}]
                }
                """;

        UserDto dto = objectMapper.readValue(json, UserDto.class);

        assertNotNull(dto);
        assertEquals("Rahul Sharma", dto.getName());
        assertEquals("rahul.sharma@example.com", dto.getEmail());
        assertEquals("Rahul@123", dto.getPassword());
        assertEquals("9876543210", dto.getMobileNumber());

        // Read-only fields must NOT be set from client JSON payload
        assertNull(dto.getId(), "Database-generated ID must be read-only and ignored from request");
        assertNull(dto.getRoles(), "Roles must be read-only and cannot be overridden by client");
        assertEquals(0L, dto.getFollowerCount(), "Follower count must default to 0 and not fail on null");
        assertEquals(0L, dto.getFollowingCount(), "Following count must default to 0 and not fail on null");
    }

    @Test
    @DisplayName("Issue 1: Password is write-only and is never serialized in response JSON")
    void testPasswordNeverSerializedInOutputJson() throws Exception {
        UserDto dto = new UserDto();
        dto.setId(10);
        dto.setName("Om Sharma");
        dto.setEmail("om@example.com");
        dto.setPassword("SecretPassword!123");

        String serializedJson = objectMapper.writeValueAsString(dto);

        assertFalse(serializedJson.contains("SecretPassword"), "Password must never be serialized in output JSON");
        assertFalse(serializedJson.contains("password"), "Password field must be omitted in response JSON");
    }

    @Test
    @DisplayName("Issue 1: Registration succeeds with valid data, assigns ROLE_USER, and encodes password")
    void testSuccessfulRegistration() {
        UserDto inputDto = new UserDto();
        inputDto.setName("Rahul Sharma");
        inputDto.setEmail("rahul@example.com");
        inputDto.setPassword("Rahul@123");
        inputDto.setMobileNumber("9876543210");

        existsByEmailResult = false;
        existsByMobileResult = false;

        UserDto result = userService.createUser(inputDto);

        assertNotNull(result);
        assertEquals(101, result.getId());
        assertEquals("Rahul Sharma", result.getName());
        assertEquals("rahul@example.com", result.getEmail());
        assertNotNull(savedUserCapture);
        assertEquals("$2a$10$encoded_Rahul@123", savedUserCapture.getPassword());
        assertTrue(savedUserCapture.getRoles().stream().anyMatch(r -> "ROLE_USER".equals(r.getName())));
    }

    @Test
    @DisplayName("Issue 1: Duplicate email is rejected with ResourceAlreadyInUseException")
    void testDuplicateEmailRejected() {
        UserDto inputDto = new UserDto();
        inputDto.setName("Rahul Sharma");
        inputDto.setEmail("existing@example.com");
        inputDto.setPassword("Rahul@123");

        existsByEmailResult = true;

        ResourceAlreadyInUseException ex = assertThrows(ResourceAlreadyInUseException.class, () ->
                userService.createUser(inputDto)
        );

        assertTrue(ex.getMessage().contains("Email is already registered"));
    }

    @Test
    @DisplayName("Issue 1: Duplicate mobile number is rejected with ResourceAlreadyInUseException")
    void testDuplicateMobileRejected() {
        UserDto inputDto = new UserDto();
        inputDto.setName("Rahul Sharma");
        inputDto.setEmail("newuser@example.com");
        inputDto.setPassword("Rahul@123");
        inputDto.setMobileNumber("9876543210");

        existsByEmailResult = false;
        existsByMobileResult = true;

        ResourceAlreadyInUseException ex = assertThrows(ResourceAlreadyInUseException.class, () ->
                userService.createUser(inputDto)
        );

        assertTrue(ex.getMessage().contains("Mobile number is already registered"));
    }
}
