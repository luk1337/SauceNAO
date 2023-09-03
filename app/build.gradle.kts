plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {

    compileSdk = 34
    defaultConfig {
        applicationId = "com.luk.saucenao"
        minSdk = 21
        targetSdk = 33
        versionCode = 20
        versionName = "1.18"
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

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    lint {
        disable += "GoogleAppIndexingWarning"
    }
    namespace = "com.luk.saucenao"
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")

    // AndroidX
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.foundation:foundation-layout:1.5.0")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0")
    implementation("androidx.compose.material3:material3:1.2.0-alpha06")

    // Other
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("org.jsoup:jsoup:1.16.1")
}
