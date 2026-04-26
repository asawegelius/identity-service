package unit.com.wegelius.identity.repository

import com.wegelius.identity.exception.EmailAlreadyExistsException
import com.wegelius.identity.logging.LogFactory
import com.wegelius.identity.model.AccountStatus
import com.wegelius.identity.model.RegisterUserRequest
import com.wegelius.identity.repository.UserRepository
import org.slf4j.Logger
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

class UserRepositorySpec extends Specification {

    def jdbcTemplate = Mock(NamedParameterJdbcTemplate)
    def passwordEncoder = Mock(PasswordEncoder)
    def appLogger = Mock(Logger)
    def securityLogger = Mock(Logger)
    def logFactory = Mock(LogFactory) {
        getLogger(_ as Class) >> appLogger
        getSecurityLogger() >> securityLogger
    }
    def repository = new UserRepository(jdbcTemplate, passwordEncoder, logFactory)

    def "registerUser inserts user and credential if email is not taken"() {
        given:
        def request = new RegisterUserRequest(email: "test@example.com", password: "secret123", displayName: "Test User")
        passwordEncoder.encode("secret123") >> "hashed_password"

        when:
        repository.registerUser(request)

        then:
        1 * jdbcTemplate.update(
                { sql -> sql.contains('INSERT INTO "user"') && sql.contains('status') && sql.contains('failed_attempts') },
                { params -> params.email == "test@example.com" && params.status == AccountStatus.PENDING_ACTIVATION.name() && params.failedAttempts == 0 }
        )
        1 * jdbcTemplate.update({ it.contains('INSERT INTO credential') }, _)
    }

    def "registerUser throws exception if email is already registered"() {
        given:
        def request = new RegisterUserRequest(email: "duplicate@example.com", password: "secret123", displayName: "Duplicate User")
        jdbcTemplate.update({ it.contains('INSERT INTO "user"') }, _ as Map<String, ?>) >> { throw new DuplicateKeyException("uq_user_email") }

        when:
        repository.registerUser(request)

        then:
        thrown(EmailAlreadyExistsException)
        0 * jdbcTemplate.update({ it.contains('INSERT INTO credential') }, _)
    }
}
