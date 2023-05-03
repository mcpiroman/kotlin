plugins {
    kotlin("jvm")
    id("jps-compatible")
    id("org.jetbrains.kotlinx.benchmark") version "0.4.6"
}

project.configureJvmToolchain(JdkMajorVersion.JDK_17_0)

dependencies {
    implementation(project(":compiler:bir"))
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.6")
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
    kotlinOptions {
        languageVersion = "2.0"
        allWarningsAsErrors = false
    }
}

benchmark {
    targets {
        register("main")
    }

    configurations {
        named("main") {
            mode = "AverageTime"
            outputTimeUnit = "ms"
            iterationTime = 2

            warmups = 8
            iterations = 8

            include("CompilationBenchmark")
        }
    }
}