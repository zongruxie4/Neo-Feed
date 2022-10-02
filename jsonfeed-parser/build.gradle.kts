import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-library`
}
val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions.jvmTarget = "1.8"

configurations.all {
    resolutionStrategy {
        failOnVersionConflict()

        force("com.squareup.okio:okio:3.2.0")
        force("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
        force("org.jetbrains.kotlin:kotlin-stdlib-common:1.7.10")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.10")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.7.10")
        force("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    api(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
    api("com.squareup.okhttp3:okhttp")
    api("com.squareup.moshi:moshi:1.12.0")
    api("com.squareup.moshi:moshi-kotlin:1.12.0")

    // tests
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.10")
    testImplementation("junit:junit:4.13.2")
}
