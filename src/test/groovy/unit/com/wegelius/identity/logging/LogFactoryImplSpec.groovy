package unit.com.wegelius.identity.logging
import com.wegelius.identity.logging.LogFactoryImpl
import org.slf4j.Logger
import spock.lang.Specification

class LogFactoryImplSpec extends Specification {

    def logFactory = new LogFactoryImpl()

    def "getLogger returns logger for specified class"() {
        when:
        Logger logger = logFactory.getLogger(String)

        then:
        logger != null
        logger.name == "java.lang.String"
    }

    def "getSecurityLogger returns logger named com.wegelius.identity.security"() {
        when:
        Logger logger = logFactory.getSecurityLogger()

        then:
        logger != null
        logger.name == "com.wegelius.identity.security"
    }
}
