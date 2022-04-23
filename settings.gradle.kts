pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }

}


rootProject.name = "slimeworldmanager"
include(":slimeworldmanager-api")
include(":slimeworldmanager-classmodifier")
include(":slimeworldmanager-classmodifierapi")
include(":slimeworldmanager-plugin")
include(":slimeworldmanager-importer")
include(":slimeworldmanager-nms-v112")
include(":slimeworldmanager-nms-v116")
include(":slimeworldmanager-nms-v117")
include(":slimeworldmanager-nms-v118")
include(":slimeworldmanager-nms-common")
