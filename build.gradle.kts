import groovy.json.JsonSlurper
import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.2.3"
    alias(libs.plugins.spotless)
    alias(libs.plugins.hangar)
    alias(libs.plugins.modrinth)
    alias(libs.plugins.pluginyml)
    alias(libs.plugins.publishdata)
    `maven-publish`
}

group = "de.sirywell.noisypatterns"
version = "1.0.2"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(platform("com.intellectualsites.bom:bom-newest:1.42")) // Ref: https://github.com/IntellectualSites/bom
    compileOnly(libs.bundles.fawe)
    compileOnly(libs.paper)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        target("**/*.java")
    }
}

publishData {
    addBuildData()
    useEldoNexusRepos()
    publishComponent("java")
}

publishing {
    publications.create<MavenPublication>("maven") {
        publishData.configurePublication(this)
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }

            setUrl(publishData.getRepository())
            name = "EldoNexus"
        }
    }
}

tasks {
    // copied from PlotSquared
    register("cacheLatestFaweArtifact") {
        val lastSuccessfulBuildUrl = uri("https://ci.athion.net/job/FastAsyncWorldEdit/lastSuccessfulBuild/api/json").toURL()
        val artifact = ((JsonSlurper().parse(lastSuccessfulBuildUrl) as Map<*, *>)["artifacts"] as List<*>)
                .map { it as Map<*, *> }
                .map { it["fileName"] as String }
                .firstOrNull { it.contains("Bukkit") }
        project.ext["faweArtifact"] = artifact
    }

    runServer {
        dependsOn(getByName("cacheLatestFaweArtifact"))
        jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
        downloadPlugins {
            url("https://ci.athion.net/job/FastAsyncWorldEdit/lastSuccessfulBuild/artifact/artifacts/${project.ext["faweArtifact"]}")
        }
        minecraftVersion("1.20.4")
    }
    test {
        useJUnitPlatform()
    }
}

hangarPublish {
    publications.register("plugin") {
        version.set(publishData.getVersion())
        id = "NoisyPatterns"
        channel = System.getenv("HANGAR_CHANNEL")

        apiKey = System.getenv("HANGAR_KEY")

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.jar.flatMap { it.archiveFile })
                platformVersions.set(listOf("1.16.5-1.20.4"))
                this.dependencies {
                    hangar("FastAsyncWorldEdit") {
                        required = true
                    }
                }
            }
        }

    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("QeTYqlgP")
    versionNumber.set(publishData.getVersion())
    versionType.set(System.getenv("MODRINTH_CHANNEL"))
    uploadFile.set(tasks.jar)
    gameVersions.addAll(listOf("1.16.5", "1.17.1", "1.18.2", "1.19.4", "1.20.4"))
    loaders.addAll(listOf("paper", "spigot"))
    dependencies {
        required.project("FastAsyncWorldEdit")
    }
    syncBodyFrom = rootProject.file("README.md").reader().readText()
}

bukkit {
    name = "NoisyPatterns"
    author = "SirYwell"
    version = publishData.getVersion(true)
    main = "de.sirywell.noisypatterns.NoisyPatternsPlugin"
    depend = listOf("FastAsyncWorldEdit")
    apiVersion = "1.16"

    commands {
        register("noisypatterns") {
            description = "Noisy pattern info command"
        }
    }
}
