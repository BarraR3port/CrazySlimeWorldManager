dependencies {
    implementation("com.flowpowered:flow-nbt:2.0.2")
    implementation("com.github.luben:zstd-jni:1.4.9-5")
    compileOnly("com.destroystokyo.paper:paper-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly(project(":slimeworldmanager-api"))
}

description = "slimeworldmanager-nms-common"
configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        version = "2.7.1"
        groupId = "com.grinderwolf.swm"
        artifactId = "slimeworldmanager-nms-common"
    }
}