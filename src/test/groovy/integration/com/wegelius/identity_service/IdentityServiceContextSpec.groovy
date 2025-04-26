package integration.com.wegelius.identity_service

import com.wegelius.identity_service.IdentityServiceApplication
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = IdentityServiceApplication)
class IdentityServiceContextSpec extends Specification{

    def "context loads"() {
        expect:
        true
    }
}
