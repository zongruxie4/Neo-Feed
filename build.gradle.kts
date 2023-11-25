buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath(libs.gradle)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    }
}

tasks.register("clean", Delete::class.java) {
    delete(layout.buildDirectory)
}