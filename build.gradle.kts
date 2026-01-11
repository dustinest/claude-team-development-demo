plugins {
    java
    id("io.quarkus") version "3.6.4" apply false
}

val quarkusVersion: String by project
val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project

allprojects {
    group = "com.trading.platform"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    extra["quarkusVersion"] = quarkusVersion
    extra["quarkusPlatformGroupId"] = quarkusPlatformGroupId
    extra["quarkusPlatformArtifactId"] = quarkusPlatformArtifactId
}

subprojects {
    apply(plugin = "java")

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
        val implementation by configurations
        val testImplementation by configurations

        // Logging
        implementation("org.slf4j:slf4j-api:2.0.9")

        // Testing - Spock/Groovy
        testImplementation("org.spockframework:spock-core:2.4-M1-groovy-4.0")
        testImplementation("org.apache.groovy:groovy-all:4.0.15")
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Task to generate OpenAPI YAML files from running services
tasks.register<Exec>("generateOpenApiDocs") {
    group = "documentation"
    description = "Generate OpenAPI YAML files from running services"

    doFirst {
        val outputDir = file("docs/openapi")
        outputDir.mkdirs()

        println("Generating OpenAPI documentation for 10 services...")
    }

    commandLine("bash", "-c", """
        set -e
        mkdir -p docs/openapi

        services="api-gateway:8080 securities-pricing-service:8081 currency-exchange-service:8082 fee-service:8083 user-signup-service:8084 user-service:8085 wallet-service:8086 trading-service:8087 portfolio-service:8088 transaction-history-service:8089"

        for service_port in ${'$'}services; do
            service=${'$'}{service_port%%:*}
            port=${'$'}{service_port##*:}

            echo "Fetching OpenAPI spec from ${'$'}service (port ${'$'}port)..."

            if curl -s -f -o "docs/openapi/${'$'}service-openapi.yaml" "http://localhost:${'$'}port/openapi"; then
                echo "  ✓ Saved to docs/openapi/${'$'}service-openapi.yaml"
            else
                echo "  ✗ Failed to fetch from ${'$'}service"
            fi
        done

        echo ""
        echo "OpenAPI documentation generated in docs/openapi/"
        ls -lh docs/openapi/*.yaml
    """.trimIndent())
}
