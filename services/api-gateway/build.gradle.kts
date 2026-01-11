plugins {
    java
    id("io.quarkus")
}

dependencies {
    implementation(enforcedPlatform("${rootProject.extra["quarkusPlatformGroupId"]}:${rootProject.extra["quarkusPlatformArtifactId"]}:${rootProject.extra["quarkusVersion"]}"))
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson")
    implementation("io.quarkus:quarkus-arc")
    implementation(project(":shared:common-domain"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

tasks.test {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
