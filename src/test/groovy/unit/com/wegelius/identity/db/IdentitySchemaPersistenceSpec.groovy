package unit.com.wegelius.identity.db

import spock.lang.Specification

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.UUID

class IdentitySchemaPersistenceSpec extends Specification {

    Connection connection

    def setup() {
        connection = DriverManager.getConnection("jdbc:h2:mem:${UUID.randomUUID()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE", "sa", "")
        recreateSchema()
    }

    def cleanup() {
        connection?.close()
    }

    def "user email uniqueness constraint rejects duplicate email"() {
        when:
        insertUser("duplicate@example.com", "ACTIVE")
        insertUser("duplicate@example.com", "PENDING_ACTIVATION")

        then:
        thrown(SQLException)
    }

    def "user status constraint rejects unsupported lifecycle values"() {
        when:
        insertUser("bad-status@example.com", "UNKNOWN")

        then:
        thrown(SQLException)
    }

    def "credential type constraint rejects non-password credentials"() {
        given:
        def userId = insertUser("credential-type@example.com", "ACTIVE")

        when:
        insertCredential(userId, "API_KEY", "hash-1")

        then:
        thrown(SQLException)
    }

    def "credential uniqueness constraint allows only one password credential per user"() {
        given:
        def userId = insertUser("single-password@example.com", "ACTIVE")

        when:
        insertCredential(userId, "PASSWORD", "hash-1")
        insertCredential(userId, "PASSWORD", "hash-2")

        then:
        thrown(SQLException)
    }

    def "credential row requires an owning user"() {
        when:
        insertCredential(UUID.randomUUID(), "PASSWORD", "orphan-hash")

        then:
        thrown(SQLException)
    }

    private void recreateSchema() {
        execute("""
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

        execute("""
            CREATE TABLE credential (
              credential_id UUID PRIMARY KEY,
              credential_type VARCHAR(32) NOT NULL,
              secret_hash TEXT NOT NULL,
              user_id UUID NOT NULL,
              created_at TIMESTAMP WITH TIME ZONE NOT NULL,
              updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
              CONSTRAINT chk_credential_type CHECK (credential_type = 'PASSWORD'),
              CONSTRAINT uq_credential_user_id_type UNIQUE (user_id, credential_type),
              CONSTRAINT fk_credential_user
                FOREIGN KEY (user_id) REFERENCES "user"(user_id)
                ON DELETE CASCADE
            )
        """)
    }

    private UUID insertUser(String email, String status) {
        def userId = UUID.randomUUID()
        execute("""
            INSERT INTO "user" (user_id, email, display_name, status, failed_attempts, created_at, updated_at)
            VALUES ('$userId', '$email', 'Test User', '$status', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """)
        userId
    }

    private void insertCredential(UUID userId, String credentialType, String secretHash) {
        execute("""
            INSERT INTO credential (credential_id, credential_type, secret_hash, user_id, created_at, updated_at)
            VALUES ('${UUID.randomUUID()}', '$credentialType', '$secretHash', '$userId', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """)
    }

    private void execute(String sql) {
        def statement = connection.createStatement()
        try {
            statement.execute(sql)
        } finally {
            statement.close()
        }
    }
}
