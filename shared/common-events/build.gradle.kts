plugins {
    `java-library`
}

dependencies {
    implementation(project(":shared:common-domain"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.0")
}
