import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

// Version code is driven by CI (GitHub Actions run number) so every push to
// main produces a strictly increasing version the in-app updater can compare
// against. Locally it just falls back to 1.
val oasisVersionCode = (project.findProperty("oasisVersionCode") as String?)?.toIntOrNull() ?: 1
val oasisVersionName = (project.findProperty("oasisVersionName") as String?) ?: "0.$oasisVersionCode-dev"

// Release signing comes from environment variables in CI (populated from
// GitHub Actions secrets so the keystore never lives in this public repo).
// A local keystore/keystore.properties file (git-ignored) is supported too,
// for anyone who wants to build a signed release locally.
val keystorePropsFile = rootProject.file("keystore/keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}

fun signingProperty(envName: String, propName: String): String? =
    System.getenv(envName) ?: keystoreProps.getProperty(propName)

val releaseStoreFilePath = signingProperty("ANDROID_KEYSTORE_PATH", "storeFile")
val releaseStorePassword = signingProperty("ANDROID_KEYSTORE_PASSWORD", "storePassword")
val releaseKeyAlias = signingProperty("ANDROID_KEY_ALIAS", "keyAlias")
val releaseKeyPassword = signingProperty("ANDROID_KEY_PASSWORD", "keyPassword")
val hasReleaseSigningConfig = listOf(releaseStoreFilePath, releaseStorePassword, releaseKeyAlias, releaseKeyPassword).all { !it.isNullOrBlank() }

// Steam is an optional feature, not required for the app to build/install, so
// a missing key doesn't fail the build — the Steam screen just shows it's
// unconfigured. Never commit a real key to this public repo; it comes from
// the STEAM_API_KEY GitHub Actions secret in CI, or keystore/keystore.properties locally.
val steamApiKey = System.getenv("STEAM_API_KEY") ?: keystoreProps.getProperty("steamApiKey") ?: ""

android {
    namespace = "com.oasis.tracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.oasis.tracker"
        minSdk = 26
        targetSdk = 35
        versionCode = oasisVersionCode
        versionName = oasisVersionName

        buildConfigField("String", "GITHUB_OWNER", "\"ALF452\"")
        buildConfigField("String", "GITHUB_REPO", "\"Project-Oasis\"")
        buildConfigField("String", "STEAM_API_KEY", "\"$steamApiKey\"")
        buildConfigField("String", "STEAM_RETURN_URL", "\"oasis://steamcallback\"")
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigningConfig) {
                storeFile = rootProject.file(releaseStoreFilePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.browser:browser:1.8.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    implementation("io.coil-kt:coil-compose:2.7.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
