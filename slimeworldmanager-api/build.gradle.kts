plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    implementation("com.flowpowered:flow-nbt:2.0.2")
    implementation("org.jetbrains:annotations:20.1.0")
    compileOnly("com.destroystokyo.paper:paper-api:1.12.2-R0.1-SNAPSHOT")
}

description = "slimeworldmanager-api"

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        version = "2.7.1"
        groupId = "com.grinderwolf.swm"
        artifactId = "slimeworldmanager-api"
    }
}
