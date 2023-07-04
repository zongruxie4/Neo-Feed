val vRoom = "2.5.2"
val vCompose = "1.5.0-beta03"
val vCoil = "2.4.0"
val vKoin = "3.4.2"
val vAccompanist = "0.31.4-beta"
val vComposeCompiler = "1.4.8"
val vKotlin = "1.8.22"
val vMaterial = "1.5.0-beta02"
val vMaterial3 = "1.2.0-alpha03"
val vRome = "2.1.0"

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.parcelize")
    kotlin("plugin.serialization") version ("1.8.22")
    id("com.google.devtools.ksp") version ("1.8.22-1.0.11")
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        }
    }
}

android {
    val name = "1.6.1"
    val code = 53

    namespace = "com.saulhdev.feeder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.saulhdev.neofeed"
        minSdk = 26
        targetSdk = 33
        versionCode = code
        versionName = name
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                ksp {
                    arg("room.schemaLocation", "$projectDir/schemas")
                    arg("room.incremental", "true")
                }
            }
        }
    }

    applicationVariants.all { variant ->
        variant.outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Neo_Feed_v" + variant.versionName + "_Build_" + variant.versionCode + ".apk"
        }
        true
    }
    buildTypes {
        named("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".dev"
            signingConfig = signingConfigs.getByName("debug")
        }
        named("release") {
            isMinifyEnabled = false
            /*isShrinkResources = true*/
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            aidl.srcDirs("src/main/aidl")
            assets.srcDirs("src/main/assets")
            res.srcDirs("src/main/res")
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("primary") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = compileOptions.sourceCompatibility.toString()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = compileOptions.sourceCompatibility.toString()
        }
    }

    buildFeatures {
        compose = true
        dataBinding = true
        viewBinding = true
        buildConfig = true
        aidl = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = vComposeCompiler
    }

    packaging {
        resources.pickFirsts.add("rome-utils-2.1.0.jar")
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("com.google.devtools.ksp:symbol-processing-api:$vKotlin-1.0.11")
    implementation(kotlin("stdlib", vKotlin))

    //Core
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.11.0-beta02")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("com.google.android.material:material:1.9.0")

    //Compose
    implementation("androidx.compose.material3:material3:$vMaterial3")
    implementation("androidx.compose.ui:ui:$vCompose")
    implementation("androidx.compose.ui:ui-tooling:$vCompose")
    implementation("androidx.compose.ui:ui-tooling-preview:$vCompose")
    implementation("androidx.navigation:navigation-compose:2.7.0-beta02")
    implementation("com.google.accompanist:accompanist-webview:$vAccompanist")

    //Room
    implementation("androidx.room:room-runtime:$vRoom")
    implementation("androidx.room:room-ktx:$vRoom")
    implementation("androidx.room:room-paging:$vRoom")
    kapt("androidx.room:room-compiler:$vRoom")

    //Squareup
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.9")
    implementation("com.squareup.retrofit2:retrofit:2.9.0") { exclude(module = "okhttp") }
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //Coil
    implementation("io.coil-kt:coil:$vCoil")
    implementation("io.coil-kt:coil-compose:$vCoil")

    //Koin
    implementation("io.insert-koin:koin-android:$vKoin")
    ksp("io.insert-koin:koin-ksp-compiler:1.2.2")

    //Libs
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")
    implementation("com.rometools:rome:$vRome") { exclude(module = "rome-utils") }
    implementation("com.rometools:rome-modules:$vRome")
    implementation("net.dankito.readability4j:readability4j:1.0.8")
    implementation("org.ccil.cowan.tagsoup:tagsoup:1.2.1")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.kodein.di:kodein-di-framework-android-x:7.20.2")
    implementation("com.github.kenglxn.qrgen:android:2.6.0")
    implementation("org.slf4j:slf4j-android:1.7.36")
}