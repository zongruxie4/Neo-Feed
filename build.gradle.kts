plugins {
    id("com.android.application").version("8.0.1") apply false
    id("com.android.library").version("8.0.1") apply false
    id("org.jetbrains.kotlin.android").version("1.8.21") apply false
    kotlin("plugin.parcelize").version("1.8.21") apply false
    kotlin("plugin.serialization").version("1.8.21") apply false
    kotlin("kapt").version("1.8.21") apply false
    id("com.google.devtools.ksp").version("1.8.20-1.0.11") apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.1")
    }
}


tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}