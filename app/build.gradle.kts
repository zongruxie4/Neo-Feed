plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        }
    }
}

android {
    val name = "1.6.3"
    val code = 57

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
        multiDexEnabled = true

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
        jvmTarget = "17"
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
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
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

    implementation(libs.symbol.processing.api)
    implementation(libs.stdlib)

    //Core
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.multidex)
    implementation(libs.swiperefreshlayout)
    implementation(libs.work.runtime.ktx)
    implementation(libs.material)

    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle.common.java8)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.service)
    implementation(libs.lifecycle.viewmodel.ktx)

    //Compose
    implementation(libs.browser)
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.navigation.compose)
    implementation(libs.accompanist.webview)
    implementation(libs.accompanist.systemuicontroller)

    //Room
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)

    //Squareup
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit) { exclude(module = "okhttp") }
    implementation(libs.retrofit.converter.gson)

    //Coil
    implementation(libs.coil)
    implementation(libs.coil.compose)

    //Koin
    implementation(libs.koin.android)
    implementation(libs.koin.android.compat)
    implementation(libs.koin.compose)
    implementation(libs.koin.navigation)
    implementation(libs.koin.workmanager)
    ksp(libs.koin.ksp.compiler)

    //Libs
    implementation(libs.threetenabp)
    implementation(libs.rome) { exclude(module = "rome-utils") }
    implementation(libs.rome.modules)
    implementation(libs.readability4j)
    implementation(libs.tagsoup)
    implementation(libs.jsoup)
    implementation(libs.kodein)
    implementation(libs.slf4j)
}