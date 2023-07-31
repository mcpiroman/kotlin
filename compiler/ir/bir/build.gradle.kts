plugins {
    kotlin("jvm")
    id("jps-compatible")
    application
}

project.configureJvmToolchain(JdkMajorVersion.JDK_17_0)

dependencies {
    api(project(":compiler:bir:tree"))
    api(project(":compiler:bir:backend"))
    api(project(":compiler:ir.backend.common"))
    api(project(":compiler:ir.tree"))
    implementation(project(":compiler:util"))
    api(project(":compiler:ir.serialization.js"))
    runtimeOnly(project(":compiler:cli-common"))

    implementation(project(":compiler:backend.wasm"))

    compileOnly(kotlinStdlib())
    implementation(intellijCore())
    runtimeOnly(commonDependency("org.jetbrains.intellij.deps", "trove4j"))
    runtimeOnly(commonDependency("org.jetbrains.intellij.deps.fastutil:intellij-deps-fastutil"))
    runtimeOnly(commonDependency("org.jetbrains.kotlin:kotlin-reflect")) { isTransitive = false }

    // Those are needed by for runs from IJ, dunno why does it not pick them up from gradle
    runtimeOnly(project(":core:builtins"))

    // Those might be(come) needed:
    //runtimeOnly(commonDependency("one.util:streamex"))
    //runtimeOnly(jpsModel()) { isTransitive = false }
    //runtimeOnly(jpsModelImpl()) { isTransitive = false }
    //runtimeOnly(intellijJavaRt())
    //runtimeOnly(toolsJar())
    //runtimeOnly(commonDependency("net.java.dev.jna:jna"))
    //api(project(":compiler:backend-common"))
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

application {
    mainClass.set("org.jetbrains.kotlin.bir.MainKt")
    applicationDefaultJvmArgs = listOf("-XX:+UnlockDiagnosticVMOptions", "-XX:+DebugNonSafepoints", "-Xms4000m", "-Xmx6000m")
}
