package unit.com.wegelius.identity.service

import com.wegelius.identity.IdentityServiceApplication
import com.wegelius.identity.exception.EmailAlreadyExistsException
import com.wegelius.identity.model.RegisterUserRequest
import com.wegelius.identity.service.UserService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@ActiveProfiles("test")
@SpringBootTest(classes = IdentityServiceApplication)
class UserServiceRegistrationSpec extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    UserService userService

    String encodedPassword = "encoded-password"

    @SpringBean
    PasswordEncoder passwordEncoder = Stub() {
        encode(_ as CharSequence) >> { encodedPassword }
    }

    def setup() {
        recreateSchema()
        encodedPassword = "encoded-password"
    }

    def "registerUser persists user and credential together"() {
        given:
        encodedPassword = "encoded-success"
        def request = new RegisterUserRequest(email: "user@example.com", password: "secret123", displayName: "User Example")

        when:
        userService.registerUser(request)

        then:
        jdbcTemplate.queryForObject('SELECT COUNT(*) FROM "user" WHERE email = ?', Integer, request.email) == 1
        jdbcTemplate.queryForObject('SELECT status FROM "user" WHERE email = ?', String, request.email) == "PENDING_ACTIVATION"
        jdbcTemplate.queryForObject(
                '''SELECT COUNT(*)
                   FROM credential c
                   JOIN "user" u ON u.user_id = c.user_id
                   WHERE u.email = ? AND c.credential_type = 'PASSWORD' AND c.secret_hash = ?''',
                Integer,
                request.email,
                encodedPassword
        ) == 1
    }

    def "registerUser enforces unique email through the database-backed write path"() {
        given:
        def request = new RegisterUserRequest(email: "duplicate@example.com", password: "secret123", displayName: "Duplicate User")

        when:
        userService.registerUser(request)
        userService.registerUser(request)

        then:
        thrown(EmailAlreadyExistsException)
        jdbcTemplate.queryForObject('SELECT COUNT(*) FROM "user" WHERE email = ?', Integer, request.email) == 1
        jdbcTemplate.queryForObject('SELECT COUNT(*) FROM credential', Integer) == 1
    }

    def "registerUser rolls back user creation when credential write fails"() {
        given:
        recreateSchema("credential_type = 'PASSWORD' AND secret_hash = 'allowed-hash'")
        encodedPassword = "forbidden-hash"
        def request = new RegisterUserRequest(email: "rollback@example.com", password: "secret123", displayName: "Rollback User")

        when:
        userService.registerUser(request)

        then:
        thrown(DataIntegrityViolationException)
        jdbcTemplate.queryForObject('SELECT COUNT(*) FROM "user" WHERE email = ?', Integer, request.email) == 0
        jdbcTemplate.queryForObject('SELECT COUNT(*) FROM credential', Integer) == 0
    }

    private void recreateSchema(String credentialConstraint = "credential_type = 'PASSWORD'") {
        jdbcTemplate.execute("DROP ALL OBJECTS")

        jdbcTemplate.execute("""
            CREATE TABLE "user" (
              user_id UUID PRIMARY KEY,
              email VARCHAR(255) NOT NULL,
              display_name VARCHAR(255),
              status VARCHAR(32) NOT NULL,
              failed_attempts INT NOT NULL,
              created_at TIMESTAMP WITH TIME ZONE NOT NULL,
              updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
              CONSTRAINT uq_user_email UNIQUE (email),
              CONSTRAINT chk_user_status CHECK (status IN ('PENDING_ACTIVATION', 'ACTIVE', 'LOCKED', 'DISABLED'))
            )
        """)

        jdbcTemplate.execute("""
            CREATE TABLE credential (
              credential_id UUID PRIMARY KEY,
              credential_type VARCHAR(32) NOT NULL,
              secret_hash TEXT NOT NULL,
              user_id UUID NOT NULL,
              created_at TIMESTAMP WITH TIME ZONE NOT NULL,
              updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
              CONSTRAINT chk_credential_type CHECK (${credentialConstraint}),
              CONSTRAINT uq_credential_user_id_type UNIQUE (user_id, credential_type),
              CONSTRAINT fk_credential_user
                FOREIGN KEY (user_id) REFERENCES "user"(user_id)
                ON DELETE CASCADE
            )
        """)
    }
}
