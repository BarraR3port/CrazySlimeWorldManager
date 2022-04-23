

plugins {
    `java-library`
    `maven-publish`
}

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("io.freefair.gradle:lombok-plugin:6.3.0")
    }
}

val lydarkApiMavenPublicUrl = "https://repo.lydark.org/repository/lydark-central/"

allprojects {
    apply(plugin = "java")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "maven-publish")
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://repo.rapture.pw/repository/maven-releases/")
        maven("https://repo.glaremasters.me/repository/concuncan/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(16)
    }

    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }

    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(16))
        }
    }
}

subprojects{
    repositories {
        mavenCentral()
        maven(lydarkApiMavenPublicUrl)
    }
}


allprojects {
    publishing {
        repositories {
            maven("https://repo.lydark.org/repository/lymarket/") {
                name = "lymarket"
                credentials(PasswordCredentials::class)
            }
        }
    }
}


