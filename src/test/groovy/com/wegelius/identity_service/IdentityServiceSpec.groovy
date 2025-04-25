package com.wegelius.identity_service

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class IdentityServiceSpec extends Specification{

    def "context loads"() {
        expect:
        true
    }
}
