plugins {
    kotlin("jvm")
    id("jps-compatible")

    // xxx: This project is being inappropriately 'run' when some/any other 'application' project is run.
    //application
}

val runtimeOnly by configurations
val compileOnly by configurations
runtimeOnly.extendsFrom(compileOnly)

dependencies {
    implementation(project(":generators"))
    implementation(project(":core:compiler.common"))
    implementation("com.squareup:kotlinpoet:1.13.0")

    compileOnly(intellijCore())
    compileOnly(commonDependency("org.jetbrains.intellij.deps:trove4j"))

    runtimeOnly(commonDependency("org.jetbrains.intellij.deps:jdom"))
}

/*application {
    mainClass.set("org.jetbrains.kotlin.bir.generator.MainKt")
}*/

sourceSets {
    "main" {
        projectDefault()
    }
    "test" {}
}
