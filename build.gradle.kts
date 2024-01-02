import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(universe.plugins.kotlin.multiplatform)
    `maven-publish`
    signing
    alias(universe.plugins.gradle.nexus.publish.plugin)
    alias(universe.plugins.qoomon.git.versioning)
    alias(universe.plugins.kotlin.power.assert)
}

group = "com.jamesward"

kotlin {
    @Suppress("OPT_IN_USAGE")
    wasmJs {
        binaries.library()
        browser()

        compilations.getByName("test") {
            binaries.executable(compilations.getByName(KotlinCompilation.TEST_COMPILATION_NAME))
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
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
        // Kotlin/JS doesn't like versions that aren't semver
        version = "0.0.0-\${commit}"
    }
}

tasks.withType<Sign> {
    onlyIf { System.getenv("GPG_PRIVATE_KEY") != null && System.getenv("GPG_PASSPHRASE") != null }
}

@OptIn(ExperimentalWasmDsl::class)
val testCompilation = kotlin.wasmJs().compilations.getByName("test")

tasks.create<KotlinWebpack>("wasmJsBrowserTestRun", testCompilation).apply {
    val binary = testCompilation.binaries.find { it.mode == KotlinJsBinaryMode.DEVELOPMENT } as org.jetbrains.kotlin.gradle.targets.js.ir.Executable

    dependsOn(binary.linkSyncTask)

    inputFilesDirectory = binary.linkSyncTask.flatMap { it.destinationDirectory }

    outputDirectory = binary.distribution.outputDirectory

    entryModuleName = binary.linkTask.flatMap { it.compilerOptions.moduleName }

    esModules = true

    mainOutputFileName = "looperkt-test.js"

    args.add(0, "serve")

    devServer = KotlinWebpackConfig.DevServer(
        open = true,
        static = mutableListOf(
            testCompilation.npmProject.dist.normalize().toString()
        ),
        client = KotlinWebpackConfig.DevServer.Client(
            KotlinWebpackConfig.DevServer.Client.Overlay(
                errors = true,
                warnings = false
            )
        )
    )

    watchOptions = KotlinWebpackConfig.WatchOptions(
        ignored = arrayOf("*.kt")
    )

    outputs.upToDateWhen { false }
}

configure<com.bnorm.power.PowerAssertGradleExtension> {
    functions = listOf("kotlin.test.assertTrue")
}

tasks.withType<AbstractTestTask> {
    testLogging {
        showStandardStreams = true
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
        )
    }
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