package unit.com.wegelius.identity.logging

import com.wegelius.identity.logging.LogFactory
import com.wegelius.identity.logging.SecurityLogEventLogger
import org.slf4j.Logger
import spock.lang.Specification

class SecurityLogEventLoggerSpec extends Specification {

    def logger = Mock(Logger)
    def logFactory = Mock(LogFactory) {
        getSecurityLogger() >> logger
    }
    def secLogger = new SecurityLogEventLogger(logFactory)

    def "logLoginSuccess logs SEC-IDENTITY-001 event"() {
        when:
        secLogger.logLoginSuccess("user123", "127.0.0.1")

        then:
        1 * logger.info("SEC-IDENTITY-001 | Login success | userId={} ip={}", "user123", "127.0.0.1")
    }

    def "logLoginFailure logs SEC-IDENTITY-002 event"() {
        when:
        secLogger.logLoginFailure("email@example.com", "Invalid password", "192.168.1.1")

        then:
        1 * logger.info("SEC-IDENTITY-002 | Login failure | email={} reason={} ip={}", "email@example.com", "Invalid password", "192.168.1.1")
    }

    def "logTokenIssued logs SEC-IDENTITY-003 event"() {
        when:
        secLogger.logTokenIssued("user@example.com", "openid profile", "10.0.0.1")

        then:
        1 * logger.info("SEC-IDENTITY-003 | Token issued | email={} scopes={} ip={}", "user@example.com", "openid profile", "10.0.0.1")
    }

    def "logTokenRefresh logs SEC-IDENTITY-004 event"() {
        when:
        secLogger.logTokenRefresh("user@example.com", "10.1.2.3")

        then:
        1 * logger.info("SEC-IDENTITY-004 | Token refresh attempted | email={} ip={}", "user@example.com", "10.1.2.3")
    }

    def "logCredentialRotation logs SEC-IDENTITY-005 event"() {
        when:
        secLogger.logCredentialRotation("user", "email@example.com", "user-id-abc")

        then:
        1 * logger.info("SEC-IDENTITY-005 | Credential rotation | actorType={} actorId={} userId={}", "user", "email@example.com", "user-id-abc")
    }

    def "logClientLoginFailure logs SEC-IDENTITY-006 event"() {
        when:
        secLogger.logClientLoginFailure("client@example.com", "Unknown client", "172.16.0.1")

        then:
        1 * logger.info("SEC-IDENTITY-006 | Client login failure | email={} reason={} ip={}", "client@example.com", "Unknown client", "172.16.0.1")
    }
}
