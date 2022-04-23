

dependencies {
    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-SNAPSHOT")
    compileOnly(project(":slimeworldmanager-nms-common"))
    compileOnly(project(":slimeworldmanager-api"))
    compileOnly(project(":slimeworldmanager-classmodifierapi"))

    implementation("com.flowpowered:flow-nbt:2.0.2")
}

tasks {
    assemble {

    }
}

description = "slimeworldmanager-nms-v116"
