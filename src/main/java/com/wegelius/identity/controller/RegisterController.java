package com.wegelius.identity.controller;

import com.wegelius.identity.model.RegisterUserRequest;
import com.wegelius.identity.service.UserService;
import com.wegelius.identity.exception.EmailAlreadyExistsException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user in the system.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Checks if the provided email is already in use</li>
     *     <li>Stores the user in the {@code user} table</li>
     *     <li>Stores a hashed password in the {@code credential} table</li>
     *     <li>Triggers audit via database triggers</li>
     * </ul>
     *
     * @param request DTO containing the user's email, password, and display name
     * @throws EmailAlreadyExistsException if a user with the same email already exists
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterUserRequest request) {
        userService.registerUser(request);
        return ResponseEntity.noContent().build();
    }
}

