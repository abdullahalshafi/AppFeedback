import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

//the server url and api key stay in local.properties so they never reach the public repo JitPack builds from
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.shafi.appfeedback"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shafi.appfeedback"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField(
            "String",
            "FEEDBACK_BASE_URL",
            "\"${localProperties.getProperty("feedback.base.url", "")}\""
        )
        buildConfigField(
            "String",
            "FEEDBACK_API_KEY",
            "\"${localProperties.getProperty("feedback.api.key", "")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //sample only: lets the R8 build be installed to test the library after shrinking
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    // a plain XML host on Java 8, like most of the apps that will use the library
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(project(":app-feedback"))

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    //other
    implementation(libs.hawk)
    implementation(libs.sdp)
    implementation(libs.ssp)
}
