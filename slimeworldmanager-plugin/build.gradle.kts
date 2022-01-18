plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":slimeworldmanager-api"))
    implementation(project(":slimeworldmanager-nms-common"))
    implementation(project(":slimeworldmanager-nms-v117-1", "reobf"))

    implementation("com.github.luben:zstd-jni:1.4.9-5")
    implementation("com.zaxxer:HikariCP:3.3.1")
    implementation("org.mongodb:mongo-java-driver:3.11.0-rc0")
    implementation("io.lettuce:lettuce-core:6.1.1.RELEASE")
    implementation("org.spongepowered:configurate-yaml:3.7-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:1.5")
    implementation("commons-io:commons-io:2.8.0")
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        relocate("org.bstats", "com.grinderwolf.swm.internal.bstats")
        relocate("ninja.leaping.configurate", "com.grinderwolf.swm.internal.configurate")
        relocate("com.flowpowered.nbt", "com.grinderwolf.swm.internal.nbt")
        relocate("com.zaxxer.hikari", "com.grinderwolf.swm.internal.hikari")
        relocate("com.mongodb", "com.grinderwolf.swm.internal.mongodb")
        relocate("org.bson", "com.grinderwolf.swm.internal.bson")
    }

    assemble {
        dependsOn(shadowJar)
    }
}

description = "slimeworldmanager-plugin"
