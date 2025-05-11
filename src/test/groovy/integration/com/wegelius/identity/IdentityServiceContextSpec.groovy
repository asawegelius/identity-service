package integration.com.wegelius.identity

import com.wegelius.identity.IdentityServiceApplication
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = IdentityServiceApplication)
class IdentityServiceContextSpec extends Specification{

    def "context loads"() {
        expect:
        true
    }
}
