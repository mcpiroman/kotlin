plugins {
    kotlin("jvm")
    id("jps-compatible")
    id("org.jetbrains.kotlinx.benchmark") version "0.4.8"
}

project.configureJvmToolchain(JdkMajorVersion.JDK_17_0)

dependencies {
    implementation(project(":compiler:bir"))
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.8")
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
        register("compilation") {
            mode = "AverageTime"
            outputTimeUnit = "ms"
            iterationTime = 2

            warmups = 5
            iterations = 20

            include("CompilationBenchmark")
        }

        register("iteration") {
            mode = "AverageTime"
            outputTimeUnit = "ms"
            iterationTime = 1

            warmups = 3
            iterations = 15

            include("SimpleIterationBenchmark")
            include("IterationFinding2ClassesBenchmark")
        }
    }
}