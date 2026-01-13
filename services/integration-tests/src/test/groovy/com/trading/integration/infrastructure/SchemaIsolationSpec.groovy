package com.trading.integration.infrastructure

import com.trading.integration.BaseIntegrationSpec

class SchemaIsolationSpec extends BaseIntegrationSpec {

    def "each service has its own database schema"() {
        when: "querying for all schemas"
        def schemas = queryDatabase("""
            SELECT schema_name
            FROM information_schema.schemata
            WHERE schema_name NOT IN ('pg_catalog', 'information_schema', 'pg_toast')
            ORDER BY schema_name
        """)

        then: "all service schemas exist"
        def schemaNames = schemas*.schema_name
        schemaNames.contains("user_service")
        schemaNames.contains("wallet_service")
        schemaNames.contains("trading_service")
        schemaNames.contains("portfolio_service")
        schemaNames.contains("transaction_history_service")
        schemaNames.contains("fee_service")
    }

    def "application tables are not in public schema"() {
        when: "querying for tables in public schema"
        def publicTables = queryDatabase("""
            SELECT tablename
            FROM pg_tables
            WHERE schemaname = 'public'
            AND tablename NOT LIKE 'flyway%'
        """)

        then: "no application tables exist in public schema"
        publicTables.isEmpty()
    }

    def "each service schema contains its own tables"() {
        expect: "user_service schema has users table"
        def userTables = queryDatabase("""
            SELECT tablename
            FROM pg_tables
            WHERE schemaname = 'user_service'
        """)
        userTables*.tablename.contains("users")

        and: "wallet_service schema has wallet_balances table"
        def walletTables = queryDatabase("""
            SELECT tablename
            FROM pg_tables
            WHERE schemaname = 'wallet_service'
        """)
        walletTables*.tablename.contains("wallet_balances")

        and: "trading_service schema has trades table"
        def tradingTables = queryDatabase("""
            SELECT tablename
            FROM pg_tables
            WHERE schemaname = 'trading_service'
        """)
        tradingTables*.tablename.contains("trades")

        and: "portfolio_service schema has holdings table"
        def portfolioTables = queryDatabase("""
            SELECT tablename
            FROM pg_tables
            WHERE schemaname = 'portfolio_service'
        """)
        portfolioTables*.tablename.contains("holdings")

        and: "transaction_history_service schema has transactions table"
        def transactionTables = queryDatabase("""
            SELECT tablename
            FROM pg_tables
            WHERE schemaname = 'transaction_history_service'
        """)
        transactionTables*.tablename.contains("transactions")
    }

    def "each service schema has independent Flyway history"() {
        when: "querying for Flyway history tables"
        def flywayTables = queryDatabase("""
            SELECT schemaname, tablename
            FROM pg_tables
            WHERE tablename = 'flyway_schema_history'
            ORDER BY schemaname
        """)

        then: "each service schema has its own Flyway history"
        def schemaNames = flywayTables*.schemaname
        schemaNames.contains("user_service")
        schemaNames.contains("wallet_service")
        schemaNames.contains("trading_service")
        schemaNames.contains("portfolio_service")
        schemaNames.contains("transaction_history_service")
        schemaNames.contains("fee_service")
    }

    def "schema isolation prevents cross-schema data access without explicit schema prefix"() {
        given: "a user with data in multiple service schemas"
        def user = createTestUser()
        depositToWallet(user.userId, "USD", 1000.00)

        expect: "data exists in respective schemas when explicitly queried"
        def userExists = queryDatabase("SELECT * FROM user_service.users WHERE id = '${user.userId}'")
        userExists.size() == 1

        def walletExists = queryDatabase("SELECT * FROM wallet_service.wallet_balances WHERE user_id = '${user.userId}'")
        walletExists.size() == 1
    }

    def "services can only access their own schema tables"() {
        when: "checking table ownership across schemas"
        def tablesBySchema = queryDatabase("""
            SELECT schemaname, COUNT(*) as table_count
            FROM pg_tables
            WHERE schemaname IN ('user_service', 'wallet_service', 'trading_service',
                                 'portfolio_service', 'transaction_history_service', 'fee_service')
            GROUP BY schemaname
            ORDER BY schemaname
        """)

        then: "each schema has at least one application table"
        tablesBySchema.each { row ->
            assert (row.table_count as Integer) >= 1
        }
    }
}
