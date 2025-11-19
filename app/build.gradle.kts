plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.marwadiuniversity.wordcraze"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.marwadiuniversity.wordcraze"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.3"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            // proguardFiles(...)
        }
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Unit testing dependencies
    testImplementation("junit:junit:4.13.2")

    // Android instrumented test dependencies
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("nl.dionsegijn:konfetti-xml:2.0.2")
    implementation("de.hdodenhof:circleimageview:3.1.0")
}
