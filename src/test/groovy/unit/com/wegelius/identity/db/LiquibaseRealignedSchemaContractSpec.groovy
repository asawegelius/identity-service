package unit.com.wegelius.identity.db

import spock.lang.Specification

class LiquibaseRealignedSchemaContractSpec extends Specification {

    def "identity master applies the realignment migrations in sequence"() {
        given:
        def master = loadClasspathText("db/changelog/master.yaml")

        expect:
        ordered(master,
                "009-add-unique-constraint-on-user-email.yaml",
                "010-align-user-table-with-account-state-model.yaml",
                "011-align-credential-table-with-identity-password-semantics.yaml",
                "012-correct-identity-audit-and-index-design.yaml")
    }

    def "user alignment migration captures lifecycle field assumptions"() {
        given:
        def migration = loadClasspathText("db/changelog/010-align-user-table-with-account-state-model.yaml")

        expect:
        migration.contains("name: status")
        migration.contains("name: failed_attempts")
        migration.contains("name: created_at")
        migration.contains("ADD CONSTRAINT chk_user_status")
        migration.contains("status IN ('PENDING_ACTIVATION', 'ACTIVE', 'LOCKED', 'DISABLED')")
        migration.contains("columnName: user_type")
        migration.contains("name: status")
        migration.contains("name: failed_attempts")
        migration.contains("name: created_at")
    }

    def "credential alignment migration captures password-only ownership assumptions"() {
        given:
        def migration = loadClasspathText("db/changelog/011-align-credential-table-with-identity-password-semantics.yaml")

        expect:
        migration.contains("DELETE FROM credential")
        migration.contains("WHERE user_id IS NULL OR client_id IS NOT NULL")
        migration.contains("oldColumnName: secret_value")
        migration.contains("newColumnName: secret_hash")
        migration.contains("name: updated_at")
        migration.contains("columnName: user_id")
        migration.contains("columnDataType: UUID")
        migration.contains("ADD CONSTRAINT chk_credential_type")
        migration.contains("constraintName: uq_credential_user_id_type")
        migration.contains("columnName: client_id")
        migration.contains("columnName: expires_at")
    }

    private static boolean ordered(String text, String... entries) {
        def positions = entries.collect { text.indexOf(it) }
        positions.every { it >= 0 } && positions == positions.sort()
    }

    private static String loadClasspathText(String path) {
        def stream = LiquibaseRealignedSchemaContractSpec.classLoader.getResourceAsStream(path)
        assert stream != null: "Missing classpath resource: $path"
        stream.getText("UTF-8")
    }
}
