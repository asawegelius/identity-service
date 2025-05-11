package com.wegelius.identity.service;

import com.wegelius.identity.exception.EmailAlreadyExistsException;
import com.wegelius.identity.logging.LogFactory;
import com.wegelius.identity.model.RegisterUserRequest;
import com.wegelius.identity.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class UserService {


    private final Logger appLog;
    private final Logger secLog;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, LogFactory logFactory) {
        this.userRepository = userRepository;
        this.appLog = logFactory.getLogger(getClass());
        this.secLog = logFactory.getSecurityLogger();
    }

    /**
     * Inserts a new user and their credentials into the database.
     * <p>
     * Audit triggers on {@code user} and {@code credential} tables will automatically
     * record the operation. The password is hashed using the configured {@link org.springframework.security.crypto.password.PasswordEncoder}.
     *
     * @param request user input including email, password, and optional display name
     * @throws EmailAlreadyExistsException if the email is already registered
     */
    public void registerUser(RegisterUserRequest request) {
        userRepository.registerUser(request);
    }
}
