package com.wegelius.identity.logging;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Structured security logger for traceable security events.
 * Each method corresponds to a defined event in the Security Traceability Matrix.
 * All messages are emitted with a unique event ID prefix and standard field structure.
 */
@Component
public class SecurityLogEventLogger {

    private final Logger logger;

    public SecurityLogEventLogger(LogFactory logFactory) {
        this.logger = logFactory.getSecurityLogger();
    }

    /**
     * Logs a successful user login event.
     *
     * @param userId ID of the authenticated user
     * @param ip     IP address of the request
     */
    public void logLoginSuccess(String userId, String ip) {
        logger.info("SEC-IDENTITY-001 | Login success | userId={} ip={}", userId, ip);
    }

    /**
     * Logs a failed login attempt.
     *
     * @param email  Email used during login
     * @param reason Failure reason (e.g., bad credentials)
     * @param ip     IP address of the attempt
     */
    public void logLoginFailure(String email, String reason, String ip) {
        logger.info("SEC-IDENTITY-002 | Login failure | email={} reason={} ip={}", email, reason, ip);
    }

    /**
     * Logs successful token issuance (access or refresh).
     *
     * @param email  Identity associated with the token
     * @param scopes Granted scopes
     * @param ip     IP address of the request
     */
    public void logTokenIssued(String email, String scopes, String ip) {
        logger.info("SEC-IDENTITY-003 | Token issued | email={} scopes={} ip={}", email, scopes, ip);
    }

    /**
     * Logs a token refresh attempt.
     *
     * @param email Identity attempting the refresh
     * @param ip    IP address of the request
     */
    public void logTokenRefresh(String email, String ip) {
        logger.info("SEC-IDENTITY-004 | Token refresh attempted | email={} ip={}", email, ip);
    }

    /**
     * Logs a credential rotation (password or client secret change).
     *
     * @param actorType Who initiated the rotation (e.g., user, admin, system)
     * @param actorId   ID of the actor (e.g., user email, system ID)
     * @param userId    Target user whose credentials were rotated
     */
    public void logCredentialRotation(String actorType, String actorId, String userId) {
        logger.info("SEC-IDENTITY-005 | Credential rotation | actorType={} actorId={} userId={}", actorType, actorId, userId);
    }

    /**
     * Logs a failed client login attempt.
     *
     * @param email  Supplied client email or ID
     * @param reason Failure reason
     * @param ip     IP address of the attempt
     */
    public void logClientLoginFailure(String email, String reason, String ip) {
        logger.info("SEC-IDENTITY-006 | Client login failure | email={} reason={} ip={}", email, reason, ip);
    }
}
