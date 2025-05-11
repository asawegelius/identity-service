package integration.com.wegelius.identity.db.audit

import com.wegelius.identity.IdentityServiceApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.DriverManager
import java.sql.ResultSet

/**
 * Integration test that verifies audit triggers are correctly recording changes
 * for all audit tables in the database.
 */
@SpringBootTest(classes = [IdentityServiceApplication])
class AllAuditTriggersSpec extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    /**
     * List of all audit tables and their corresponding main table and primary key column.
     * This is prepared once and shared across all test methods.
     */
    @Shared
    List<Map<String, String>> auditTables = fetchAuditTables()

    /**
     * Dynamically fetch all audit tables and determine their corresponding main table and primary key column.
     *
     * @return List of maps containing mainTable, auditTable, and idColumn keys.
     */
    static List<Map<String, String>> fetchAuditTables() {
        def url = "jdbc:postgresql://localhost:5433/identity_db"
        def user = "devuser"
        def password = "devpass"

        def conn = DriverManager.getConnection(url, user, password)

        // SQL to find all tables ending with '_audit' in the public schema
        def auditTablesSql = """
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_name LIKE '%_audit'
        """

        def stmt = conn.createStatement()
        def rs = stmt.executeQuery(auditTablesSql)

        def results = []

        while (rs.next()) {
            def auditTableName = rs.getString("table_name")
            def mainTableName = auditTableName.replaceFirst('_audit$', '')

            // Find the primary key column name of the main table
            def idColumn = findPrimaryKeyColumn(conn, mainTableName)

            if (idColumn == null) {
                throw new IllegalArgumentException("Could not find primary key for table: $mainTableName")
            }

            results << [
                    mainTable : mainTableName,
                    auditTable: auditTableName,
                    idColumn  : idColumn
            ]
        }

        conn.close()
        return results
    }

    /**
     * Find the primary key column for a given table.
     *
     * @param conn JDBC connection
     * @param tableName name of the main table
     * @return the primary key column name
     */
    static String findPrimaryKeyColumn(def conn, String tableName) {
        ResultSet pkRs = conn.metaData.getPrimaryKeys(null, "public", tableName)

        if (pkRs.next()) {
            return pkRs.getString("COLUMN_NAME")
        }
        return null
    }

    @Unroll
    def "audit table #auditTable correctly records operations on #mainTable"() {
        given: "A new ID for inserting a test record"
        UUID newId = UUID.randomUUID()

        and: "Prepare an INSERT SQL statement for the main table"
        String insertSql = generateInsertSql(mainTable as String, newId)

        when: "Insert a record into the main table"
        jdbcTemplate.update(insertSql)

        then: "Verify that a corresponding audit record exists"
        def auditCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ${auditTable} WHERE ${idColumn} = ?", Integer, newId
        )
        auditCount > 0

        where:
        [mainTable, auditTable, idColumn] << auditTables.collect { [it.mainTable, it.auditTable, it.idColumn] }
    }

    /**
     * Generate the correct INSERT SQL for a given main table.
     *
     * @param tableName name of the table to insert into
     * @param id UUID to use as the ID
     * @return a SQL string
     */
    String generateInsertSql(String tableName, UUID id) {
        switch (tableName) {
            case "user":
                return """INSERT INTO "user" (user_id, user_type, email, updated_at)
                          VALUES ('$id', 'STANDARD', 'bulk@example.com', now())"""
            case "system_client":
                return """INSERT INTO system_client (client_id, client_name, description, registered_at)
                          VALUES ('$id', 'BulkTestClient', 'bulk description', now())"""
            case "credential":
                return """INSERT INTO credential (credential_id, credential_type, secret_value, client_id, created_at)
                          VALUES ('$id', 'PASSWORD', 'bulk-secret', '00000000-0000-0000-0000-000000000002', now())"""
            case "client_application":
                return """INSERT INTO client_application (application_id, client_id, name, created_at)
                          VALUES ('$id', '00000000-0000-0000-0000-000000000002', 'Bulk App', now())"""
            case "client_application_redirect_uri":
                return """INSERT INTO client_application_redirect_uri (id, application_id, redirect_uri)
                          VALUES ('$id', '00000000-0000-0000-0000-000000000004', 'https://bulk.com/redirect')"""
            case "client_application_grant_type":
                return """INSERT INTO client_application_grant_type (id, application_id, grant_type)
                          VALUES ('$id', '00000000-0000-0000-0000-000000000006', 'authorization_code')"""
            default:
                throw new IllegalArgumentException("Don't know how to insert into table: $tableName")
        }
    }
}
