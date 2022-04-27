description = "slimeworldmanager-classmodifierapi"

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        version = "2.7.1"
        groupId = "com.grinderwolf.swm"
        artifactId = "slimeworldmanager-classmodifierapi"
    }
}