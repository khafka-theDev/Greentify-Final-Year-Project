plugins {
    id("com.android.application")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "org.oss.greentify"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.oss.greentify"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.firebase:firebase-storage:21.0.1")
    implementation("com.google.firebase:firebase-firestore:25.1.4")
    implementation("androidx.gridlayout:gridlayout:1.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.getkeepsafe.taptargetview:taptargetview:1.13.3")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // Retrofit & Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // ✅ Corrected Fused Location Provider
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("androidx.gridlayout:gridlayout:1.0.0")

    //cloudinary image storage
    implementation ("com.squareup.okhttp3:okhttp:4.12.0") // HTTP client
    implementation ("org.json:json:20231013") // For building JSON body
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.cloudinary:cloudinary-android:2.3.1")
    //bumptech
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation ("com.airbnb.android:lottie:6.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")


    //image loading
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.google.zxing:core:3.4.0")
}
