import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = 36
    namespace = "com.luk.saucenao"

    defaultConfig {
        applicationId = "com.luk.saucenao"
        minSdk = 21
        targetSdk = 36
        versionCode = 27
        versionName = "1.25"
    }

    buildTypes {
        named("release") {
            // Enables code shrinking, obfuscation, and optimization.
            isMinifyEnabled = true

            // Includes the default ProGuard rules files.
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }

    flavorDimensions += "version"

    productFlavors {
        create("github") {
            dimension = "version"
            buildConfigField("String", "SAUCENAO_HIDE", "\"0\"")
        }

        create("googlePlay") {
            dimension = "version"
            buildConfigField("String", "SAUCENAO_HIDE", "\"3\"")
        }
    }

    compileOptions {
        encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    androidResources {
        generateLocaleConfig = true
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    lint {
        disable += "GoogleAppIndexingWarning"
    }
}

dependencies {
    // Kotlin
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.3.0"))

    // AndroidX
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.foundation:foundation-layout:1.9.5")
    implementation("androidx.compose.ui:ui:1.9.5")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.5")
    debugImplementation("androidx.compose.ui:ui-tooling:1.9.5")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Other
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.jsoup:jsoup:1.22.1")
}
