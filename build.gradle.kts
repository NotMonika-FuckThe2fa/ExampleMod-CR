@file:Suppress("UnstableApiUsage", "PropertyName")

plugins {
    java
}
buildscript{
    repositories{
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.polyfrost.org/releases") // Adds the Polyfrost maven repository to get Polyfrost Gradle Toolkit
    }
    dependencies{
        classpath("org.polyfrost:architectury-loom:1.6.polyfrost.6")
    }
}

apply{
    plugin("org.polyfrost:architectury-loom")
}

// Gets the mod name, version and id from the `gradle.properties` file.
val mod_name: String by project
val mod_version: String by project
val mod_id: String by project
val mod_archives_name: String by project
val cleanroom_version: String by project

// Sets the mod version to the one specified in `gradle.properties`. Make sure to change this following semver!
version = mod_version
// Sets the group, make sure to change this to your own. It can be a website you own backwards or your GitHub username.
// e.g. com.github.<your username> or com.<your domain>
group = "org.example"

// Sets the name of the output jar (the one you put in your mods folder and send to other people)
// It outputs all versions of the mod into the `versions/{mcVersion}/build` directory.
base {
    archivesName.set("$mod_archives_name")
}

// Configures Polyfrost Loom, our plugin fork to easily set up the programming environment.
loom {

    // Adds the tweak class if we are building legacy version of forge as per the documentation (https://docs.polyfrost.org)
    runConfigs {
        "client" {
            programArgs("--tweakClass", "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker")
            property("mixin.debug.export", "true") // Outputs all mixin changes to `versions/{mcVersion}/run/.mixin.out/class`
        }
    }
    // Configures the mixins if we are building for forge
    forge {
        mixinConfig("mixins.${mod_id}.json")
    }
    // Configures the name of the mixin "refmap"
    mixin.defaultRefmapName.set("mixins.${mod_id}.refmap.json")
}

// Creates the shade/shadow configuration, so we can include libraries inside our mod, rather than having to add them separately.
val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}
val modShade: Configuration by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}

// Configures the output directory for when building from the `src/resources` directory.
sourceSets {
    main {
        output.setResourcesDir(java.classesDirectory)
    }
}

// Adds the Polyfrost maven repository so that we can get the libraries necessary to develop the mod.
repositories {
    maven("https://repo.polyfrost.org/releases")
    maven("https://maven.cleanroommc.com/")
    maven("https://repo.cleanroommc.com/snapshots")
    maven("https://maven.outlands.top/releases/")
}

// Configures the libraries/dependencies for your mod.
dependencies {
    forgeUniversal("com.cleanroommc:cleanroom:${cleanroom_version}")
    forgeUserdev("com.cleanroommc:cleanroom:${cleanroom_version}")
}

tasks {
    // Processes the `src/resources/mcmod.info`, `fabric.mod.json`, or `mixins.${mod_id}.json` and replaces
    // the mod id, name and version with the ones in `gradle.properties`
    processResources {
        inputs.property("id", mod_id)
        inputs.property("name", mod_name)
        val java = 21
        val compatLevel = "JAVA_${java}"
        inputs.property("java", java)
        inputs.property("java_level", compatLevel)
        inputs.property("version", mod_version)
        inputs.property("mcVersionStr", "1.12.2")
        filesMatching(listOf("mcmod.info", "mixins.${mod_id}.json")) {
            expand(
                mapOf(
                    "id" to mod_id,
                    "name" to mod_name,
                    "java" to java,
                    "java_level" to compatLevel,
                    "version" to mod_version,
                    "mcVersionStr" to "1.12.2"
                )
            )
        }
    }

    // Configures our shadow/shade configuration, so we can
    // include some dependencies within our mod jar file.

    jar {
        manifest.attributes += mapOf(
            "ForceLoadAsMod" to true, // We want to load this jar as a mod, so we force Forge to do so.
            "TweakOrder" to "0",
            "MixinConfigs" to "mixins.${mod_id}.json" // We want to use our mixin configuration, so we specify it here.
        )
        archiveClassifier.set("")
        enabled = false
    }
}
