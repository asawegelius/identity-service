package unit.com.wegelius.identity.db

import spock.lang.Specification

class LiquibaseAuditAndIndexDesignSpec extends Specification {

    def "identity master includes the audit and index cleanup migration"() {
        given:
        def master = loadClasspathText("db/changelog/master.yaml")

        expect:
        master.contains("012-correct-identity-audit-and-index-design.yaml")
    }

    def "auth-provider transition changelog includes the audit and index cleanup migration"() {
        given:
        def authProvider = loadClasspathText("db/changelog/auth-provider-owned-transition.yaml")

        expect:
        authProvider.contains("013-correct-auth-provider-audit-and-index-design.yaml")
    }

    def "identity cleanup migration removes redundant indexes and preserves old values in audit rows"() {
        given:
        def cleanup = loadClasspathText("db/changelog/012-correct-identity-audit-and-index-design.yaml")

        expect:
        cleanup.contains("DROP INDEX IF EXISTS idx_user_user_id")
        cleanup.contains("CASE WHEN TG_OP = 'INSERT' THEN NULL ELSE OLD.user_id END")
        cleanup.contains("CASE WHEN TG_OP = 'INSERT' THEN NULL ELSE OLD.email END")
        cleanup.contains("CASE WHEN TG_OP = 'INSERT' THEN NULL ELSE OLD.credential_id END")
        cleanup.contains("CASE WHEN TG_OP = 'INSERT' THEN NULL ELSE OLD.secret_hash END")
        cleanup.contains("RETURN COALESCE(NEW, OLD)")
    }

    def "auth-provider cleanup migration removes redundant primary-key indexes and preserves old values in audit rows"() {
        given:
        def cleanup = loadClasspathText("db/changelog/013-correct-auth-provider-audit-and-index-design.yaml")

        expect:
        cleanup.contains("DROP INDEX IF EXISTS idx_system_client_client_id")
        cleanup.contains("DROP INDEX IF EXISTS idx_session_session_id")
        cleanup.contains("DROP INDEX IF EXISTS idx_client_application_id")
        cleanup.contains("DROP INDEX IF EXISTS idx_client_application_redirect_uri_id")
        cleanup.contains("DROP INDEX IF EXISTS idx_grant_type_id")
        cleanup.contains("DROP INDEX IF EXISTS idx_grant_grant_id")
        cleanup.contains("CASE WHEN TG_OP = 'INSERT' THEN NULL ELSE OLD.client_id END")
        cleanup.contains("CASE WHEN TG_OP = 'INSERT' THEN NULL ELSE OLD.application_id END")
        cleanup.contains("CASE WHEN TG_OP = 'INSERT' THEN NULL ELSE OLD.redirect_uri END")
        cleanup.contains("CASE WHEN TG_OP = 'INSERT' THEN NULL ELSE OLD.grant_type END")
        cleanup.contains("RETURN COALESCE(NEW, OLD)")
    }

    private static String loadClasspathText(String path) {
        def stream = LiquibaseAuditAndIndexDesignSpec.classLoader.getResourceAsStream(path)
        assert stream != null: "Missing classpath resource: $path"
        stream.getText("UTF-8")
    }
}
