dependencies {
    implementation(project(":slimeworldmanager-api"))
    implementation(project(":slimeworldmanager-nms-common"))
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.google.guava:guava:26.0-jre")
    implementation("com.github.luben:zstd-jni:1.4.9-5")
    implementation("com.github.tomas-langer:chalk:1.0.2")
    implementation("com.flowpowered:flow-nbt:2.0.2")
}

description = "slimeworldmanager-importer"
configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        version = "2.7.1"
        groupId = "com.grinderwolf.swm"
        artifactId = "slimeworldmanager-importer"
    }
}