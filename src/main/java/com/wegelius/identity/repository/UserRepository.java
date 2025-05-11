package com.wegelius.identity.repository;

import com.wegelius.identity.exception.EmailAlreadyExistsException;
import com.wegelius.identity.logging.LogFactory;
import com.wegelius.identity.model.RegisterUserRequest;
import org.slf4j.Logger;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;

@Repository
public class UserRepository {

    private final Logger appLog;
    private final Logger secLog;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, LogFactory logFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.appLog = logFactory.getLogger(getClass());
        this.secLog = logFactory.getSecurityLogger();
    }

    /**
     * Inserts a new user and their credentials into the database.
     * <p>
     * This method:
     * <ul>
     *   <li>Checks if the email already exists in the {@code user} table</li>
     *   <li>Inserts a new user into the {@code user} table</li>
     *   <li>Stores a hashed password in the {@code credential} table</li>
     *   <li>Relies on database-level triggers for audit logging</li>
     * </ul>
     *
     * @param request user registration data: email, display name, and raw password
     * @throws EmailAlreadyExistsException if a user with the same email already exists
     */
    public void registerUser(RegisterUserRequest request) {
        // Check if email is already registered
        boolean exists = Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                    SELECT EXISTS (
                        SELECT 1 FROM "user" WHERE email = :email
                    )
                """, Map.of("email", request.getEmail()), Boolean.class));

        if (Boolean.TRUE.equals(exists)) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // Proceed with registration if email is unique
        String userId = UUID.randomUUID().toString();

        jdbcTemplate.update("""
        INSERT INTO "user" (user_id, user_type, email, display_name, updated_at)
        VALUES (:userId, 'local', :email, :displayName, now())
    """, Map.of(
                "userId", userId,
                "email", request.getEmail(),
                "displayName", request.getDisplayName()
        ));

        jdbcTemplate.update("""
        INSERT INTO credential (credential_id, credential_type, secret_value, user_id, created_at)
        VALUES (:credId, 'password', :hash, :userId, now())
    """, Map.of(
                "credId", UUID.randomUUID().toString(),
                "hash", passwordEncoder.encode(request.getPassword()),
                "userId", userId
        ));
    }

}
