plugins {
    java
    id("io.quarkus")
}

dependencies {
    // Quarkus BOM
    implementation(enforcedPlatform("${rootProject.extra["quarkusPlatformGroupId"]}:${rootProject.extra["quarkusPlatformArtifactId"]}:${rootProject.extra["quarkusVersion"]}"))

    // Quarkus extensions
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-arc")

    // Shared modules
    implementation(project(":shared:common-domain"))
    implementation(project(":shared:common-events"))

    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

tasks.test {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
