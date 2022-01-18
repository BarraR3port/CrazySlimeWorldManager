plugins {
    id("io.papermc.paperweight.userdev") version "1.2.0"
}

dependencies {
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.8.6")
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly(project(":slimeworldmanager-nms-common"))
    compileOnly(project(":slimeworldmanager-api"))
    compileOnly(project(":slimeworldmanager-classmodifier"))
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}

description = "slimeworldmanager-nms-v117-1"
