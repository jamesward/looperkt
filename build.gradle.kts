plugins {
    alias(universe.plugins.kotlin.multiplatform)
    `maven-publish`
    signing
    alias(universe.plugins.gradle.nexus.publish.plugin)
    alias(universe.plugins.qoomon.git.versioning)
}

group = "com.jamesward"

kotlin {
    @Suppress("OPT_IN_USAGE")
    wasmJs {
        binaries.library()
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
            }
        }
    }
}

gitVersioning.apply {
    refs {
        tag("v(?<version>.*)") {
            version = "\${ref.version}"
        }
    }

    rev {
        version = "\${commit}"
    }
}

tasks.withType<Sign> {
    onlyIf { System.getenv("GPG_PRIVATE_KEY") != null && System.getenv("GPG_PASSPHRASE") != null }
}

signing {
    sign(publishing.publications)
    useInMemoryPgpKeys(System.getenv("GPG_PRIVATE_KEY"), System.getenv("GPG_PASSPHRASE"))
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name = "LooperKt"
            description = "Wasm Canvas Looper"
            url = "https://github.com/jamesward/looperkt"

            scm {
                connection = "scm:git:https://github.com/jamesward/looperkt.git"
                developerConnection = "scm:git:git@github.com:jamesward/looperkt.git"
                url = "https://github.com/jamesward/looperkt"
            }

            licenses {
                license {
                    name = "Apache 2.0"
                    url = "https://opensource.org/licenses/Apache-2.0"
                }
            }

            developers {
                developer {
                    id = "jamesward"
                    name = "James Ward"
                    email = "james@jamesward.com"
                    url = "https://jamesward.com"
                }
            }
        }
    }
}

nexusPublishing.repositories {
    sonatype {
        username = System.getenv("SONATYPE_USERNAME")
        password = System.getenv("SONATYPE_PASSWORD")
    }
}