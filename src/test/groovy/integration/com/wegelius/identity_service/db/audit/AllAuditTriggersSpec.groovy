package integration.com.wegelius.identity_service.db.audit

import com.wegelius.identity_service.IdentityServiceApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest(
        classes = [IdentityServiceApplication],
        properties = ["spring.profiles.active=test"]
)
class AllAuditTriggersSpec extends Specification {
    @Autowired
    JdbcTemplate jdbcTemplate

    @Unroll
    def "audit table #auditTable correctly records operations on #mainTable"() {
        given: "Insert a new record into the main table"
        UUID newId = UUID.randomUUID()

        when:
        jdbcTemplate.update(insertSql, newId)

        then: "A corresponding audit record is created"
        def auditCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ${auditTable} WHERE ${idColumn} = ?", Integer, newId
        )
        assert auditCount > 0

        where:
        mainTable                         | auditTable                            | idColumn                         | insertSql
        "user"                            | "user_audit"                          | "user_id"                        | """INSERT INTO "user" (user_id, user_type, email, updated_at) VALUES (?, 'STANDARD', 'bulk@example.com', now())"""
        "system_client"                   | "system_client_audit"                 | "client_id"                      | """INSERT INTO system_client (client_id, client_name, description, registered_at) VALUES (?, 'BulkTestClient', 'bulk description', now())"""
        "credential"                      | "credential_audit"                    | "credential_id"                  | """INSERT INTO credential (credential_id, credential_type, secret_value, client_id, created_at) VALUES (?, 'PASSWORD', 'bulk-secret', '00000000-0000-0000-0000-000000000002', now())"""
        "client_application"              | "client_application_audit"            | "application_id"                 | """INSERT INTO client_application (application_id, client_id, name, created_at) VALUES (?, '00000000-0000-0000-0000-000000000002', 'Bulk App', now())"""
        "client_application_redirect_uri" | "client_application_redirect_uri_audit" | "id"                          | """INSERT INTO client_application_redirect_uri (id, application_id, redirect_uri) VALUES (?, '00000000-0000-0000-0000-000000000004', 'https://bulk.com/redirect')"""
        "client_application_grant_type"   | "client_application_grant_type_audit" | "id"                             | """INSERT INTO client_application_grant_type (id, application_id, grant_type) VALUES (?, '00000000-0000-0000-0000-000000000006', 'authorization_code')"""
    }
}
