plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation("org.javassist:javassist:3.28.0-GA")
    implementation("org.yaml:snakeyaml:1.26")
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper:1.17.1-R0.1-SNAPSHOT")
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
