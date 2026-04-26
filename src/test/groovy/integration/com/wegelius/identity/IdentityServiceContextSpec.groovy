package integration.com.wegelius.identity

import com.wegelius.identity.IdentityServiceApplication
import org.springframework.test.context.ActiveProfiles
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@ActiveProfiles("test")
@SpringBootTest(classes = IdentityServiceApplication)
class IdentityServiceContextSpec extends Specification{

    def "context loads"() {
        expect:
        true
    }
}
