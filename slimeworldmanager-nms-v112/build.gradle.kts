

dependencies {
    compileOnly("org.spigotmc:spigot:1.12.2-R0.1-SNAPSHOT")
    compileOnly(project(":slimeworldmanager-nms-common"))
    compileOnly(project(":slimeworldmanager-api"))
    compileOnly(project(":slimeworldmanager-classmodifierapi"))

    implementation("com.flowpowered:flow-nbt:2.0.2")
}

tasks {
    assemble {

    }
}

description = "slimeworldmanager-nms-v112"
configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        version = "2.7.1"
        groupId = "com.grinderwolf.swm"
        artifactId = "slimeworldmanager-nms-v112"
    }
}