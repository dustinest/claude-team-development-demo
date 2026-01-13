plugins {
    java
    id("io.quarkus")
}

dependencies {
    implementation(enforcedPlatform("${rootProject.extra["quarkusPlatformGroupId"]}:${rootProject.extra["quarkusPlatformArtifactId"]}:${rootProject.extra["quarkusVersion"]}"))
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-arc")
    implementation(project(":shared:common-domain"))
    implementation(project(":shared:common-events"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

tasks.test {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
