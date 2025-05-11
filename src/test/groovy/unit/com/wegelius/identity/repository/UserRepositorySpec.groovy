package unit.com.wegelius.identity.repository

import com.wegelius.identity.exception.EmailAlreadyExistsException
import com.wegelius.identity.model.RegisterUserRequest
import com.wegelius.identity.repository.UserRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

class UserRepositorySpec extends Specification {

    def jdbcTemplate = Mock(NamedParameterJdbcTemplate)
    def passwordEncoder = Mock(PasswordEncoder)
    def repository = new UserRepository(jdbcTemplate, passwordEncoder)

    def "registerUser inserts user and credential if email is not taken"() {
        given:
        def request = new RegisterUserRequest(email: "test@example.com", password: "s3cret", displayName: "Test User")
        passwordEncoder.encode("s3cret") >> "hashed_password"
        jdbcTemplate.queryForObject(_ as String, _ as Map<String, ?>, Boolean.class) >> false

        when:
        repository.registerUser(request)

        then:
        1 * jdbcTemplate.update({ it.contains('INSERT INTO "user"') }, _)
        1 * jdbcTemplate.update({ it.contains('INSERT INTO credential') }, _)
    }


    def "registerUser throws exception if email is already registered"() {
        given:
        def request = new RegisterUserRequest(email: "duplicate@example.com", password: "secret123", displayName: "Doppelgänger")
        jdbcTemplate.queryForObject(_ as String, _ as Map<String, ?>, Boolean.class) >> true

        when:
        repository.registerUser(request)

        then:
        thrown(EmailAlreadyExistsException)
    }

}
