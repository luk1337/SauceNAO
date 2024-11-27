plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = 34
    namespace = "com.luk.saucenao"

    defaultConfig {
        applicationId = "com.luk.saucenao"
        minSdk = 21
        targetSdk = 34
        versionCode = 26
        versionName = "1.24"
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
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.0.21"))

    // AndroidX
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.foundation:foundation-layout:1.7.5")
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.5")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.5")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Other
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.jsoup:jsoup:1.18.2")
}
