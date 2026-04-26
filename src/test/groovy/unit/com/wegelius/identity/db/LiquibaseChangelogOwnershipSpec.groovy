package unit.com.wegelius.identity.db

import spock.lang.Specification

class LiquibaseChangelogOwnershipSpec extends Specification {

    def "identity master excludes auth-provider owned tables that are no longer bootstrapped here"() {
        given:
        def master = loadClasspathText("db/changelog/master.yaml")

        expect:
        master.contains("001-create-user-with-audit.yaml")
        master.contains("003-create-credential-with-audit.yaml")
        master.contains("009-add-unique-constraint-on-user-email.yaml")
        master.contains("010-align-user-table-with-account-state-model.yaml")
        master.contains("011-align-credential-table-with-identity-password-semantics.yaml")

        and:
        !master.contains("002-create-system-client-with-audit.yaml")
        !master.contains("004-create-session-table.yaml")
        !master.contains("005-create-client-application-with-audit.yaml")
        !master.contains("006-create-client-application-redirect-uri-with-audit.yaml")
        !master.contains("007-create-client-application-grant-type-with-audit.yaml")
        !master.contains("008-create-authorization-grant-table.yaml")
    }

    def "auth-provider transition changelog tracks the extracted auth-owned tables"() {
        given:
        def authProvider = loadClasspathText("db/changelog/auth-provider-owned-transition.yaml")

        expect:
        authProvider.contains("002-create-system-client-with-audit.yaml")
        authProvider.contains("004-create-session-table.yaml")
        authProvider.contains("005-create-client-application-with-audit.yaml")
        authProvider.contains("006-create-client-application-redirect-uri-with-audit.yaml")
        authProvider.contains("007-create-client-application-grant-type-with-audit.yaml")
        authProvider.contains("008-create-authorization-grant-table.yaml")
    }

    private static String loadClasspathText(String path) {
        def stream = LiquibaseChangelogOwnershipSpec.classLoader.getResourceAsStream(path)
        assert stream != null: "Missing classpath resource: $path"
        stream.getText("UTF-8")
    }
}
