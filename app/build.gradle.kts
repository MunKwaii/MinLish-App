import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)                // plugin Ksp để biên dịch Room
}

val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    val fis = FileInputStream(localPropertiesFile)
    localProperties.load(fis)
    fis.close()
}
val smtpEmail = localProperties.getProperty("SMTP_EMAIL") ?: ""
val smtpPassword = localProperties.getProperty("SMTP_PASSWORD") ?: ""
val jwtSecret = localProperties.getProperty("JWT_SECRET") ?: "default_secret_key"
val googleWebClientId = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: ""

android {
    namespace = "vn.edu.hcmute.minlish"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "vn.edu.hcmute.minlish"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SMTP_EMAIL", "\"$smtpEmail\"")
        buildConfigField("String", "SMTP_PASSWORD", "\"$smtpPassword\"")
        buildConfigField("String", "JWT_SECRET", "\"$jwtSecret\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
    }

    signingConfigs {
        create("debugShared") {
            storeFile = file("../debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debugShared")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-spec.html"
            excludes += "META-INF/mailcap.default"
            excludes += "META-INF/mimetypes.default"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ROOM LIBRARY
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler) // Dùng ksp

    // NAVIGATION
    implementation(libs.androidx.navigation.compose)

    // ICONS
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material.icons.core)

    // CREDENTIAL MANAGER (Google Sign-In via OAuth2)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.id)

    // JAVAMAIL FOR ANDROID (SMTP EMAIL SENDING)
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // BCRYPT PASSWORD HASHING
    implementation("org.mindrot:jbcrypt:0.4")

    // DATASTORE PREFERENCES
    implementation(libs.datastore.preferences)

    // BIOMETRIC AUTHENTICATION
    implementation(libs.androidx.biometric)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}