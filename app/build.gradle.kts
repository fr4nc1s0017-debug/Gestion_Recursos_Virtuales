
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "sv.edu.sv.gestion_recursos_virtuales"

    compileSdk {
        version = release(37) // No cambiar
    }

    defaultConfig {
        applicationId = "sv.edu.sv.gestion_recursos_virtuales"

        minSdk = 37 // No cambiar
        targetSdk = 37 // No cambiar

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"https://6ae68216877d34ccafc2d6b5.mockapi.io/\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf(
                    "androidx.**",
                    "kotlin.**",
                    "kotlinx.**"
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)

    // Credential Manager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    // Firebase Authentication
    implementation(
        platform("com.google.firebase:firebase-bom:34.16.0")
    )
    implementation("com.google.firebase:firebase-auth")

    // Google Sign-In
    implementation(
        "com.google.android.libraries.identity.googleid:googleid:1.1.1"
    )

    // Corrutinas para Firebase Tasks
    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2"
    )

    // Material Design
    implementation(libs.material)

    // Retrofit y Gson para MockAPI
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // Glide para imágenes
    implementation("com.github.bumptech.glide:glide:5.0.9")

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
