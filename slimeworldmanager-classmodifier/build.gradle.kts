plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation("org.javassist:javassist:3.28.0-GA")
    implementation("org.yaml:snakeyaml:1.29")
    compileOnly(project(":slimeworldmanager-classmodifierapi"))
}

sourceSets {
    main {
        resources {
            include("**/*")
        }
    }
}

tasks {
    jar {
        manifest {
            attributes["Premain-Class"] = "com.grinderwolf.swm.clsm.NMSTransformer"
        }
    }

    shadowJar {
        archiveClassifier.set("")
    }

    assemble {
        dependsOn(shadowJar)
    }
}

description = "slimeworldmanager-classmodifier"
configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        version = "2.7.1"
        groupId = "com.grinderwolf.swm"
        artifactId = "slimeworldmanager-classmodifier"
    }
}