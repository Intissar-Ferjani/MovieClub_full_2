plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)


}

android {
    namespace = "com.example.movieclub"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.movieclub"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.compose.theme.adapter)
    implementation(libs.firebase.database)
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    implementation(libs.volley)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase Authentication library
    implementation("com.google.firebase:firebase-auth")

    implementation ("com.google.firebase:firebase-database:20.0.6")


    implementation ("com.github.Dimezis:BlurView:version-2.0.3")
    implementation ("com.google.android.gms:play-services-auth:20.2.0")
    annotationProcessor(libs.room.compiler)

    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    implementation ("com.google.firebase:firebase-appcheck:17.0.0")

    implementation ("com.google.firebase:firebase-appcheck-debug")

    implementation ("de.hdodenhof:circleimageview:2.2.0")
    implementation ("com.squareup.picasso:picasso:2.5.2")

    implementation("com.vanniktech:android-image-cropper:4.6.0")

}