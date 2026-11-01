// Android library module ":peerova" — Kotlin wrapper + Java JNI binding + native libs.
// settings.gradle.kts of your app:   include(":peerova"); project(":peerova").projectDir = file("<sdk>/bindings/kotlin-android/peerova")
// app/build.gradle.kts:              dependencies { implementation(project(":peerova")) }
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.peerova"
    compileSdk = 35
    ndkVersion = "28.2.13676358"   // any r23+ works

    defaultConfig {
        minSdk = 21
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64") }
        externalNativeBuild { cmake { arguments += listOf("-DANDROID_STL=none") } }
        consumerProguardFiles("consumer-rules.pro")
    }

    // libpeerova_jni.so is built by CMake; libpeerova.so (prebuilt, from Peerova) goes in src/main/jniLibs/<abi>/.
    externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt"); version = "3.22.1" } }

    sourceSets["main"].java.srcDirs("src/main/kotlin", "../../java/src")  // dev.peerova.Peerova (Java)

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
    // Only needed for PeerovaFlow.kt (eventFlow / startAsync). Remove both if unused.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
