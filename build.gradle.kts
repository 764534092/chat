
plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.chapter13"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.chapter13"
        minSdk = 28
        targetSdk = 33
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
    implementation ("androidx.work:work-runtime:2.7.1") // Jetpack workManager
    implementation ("com.google.code.gson:gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.github.bumptech.glide:glide:4.13.1")
    implementation ("io.socket:socket.io-client:1.0.1")
//    implementation files("tyrus-standalone-client-1.17.jar")



    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(files("/Users/sunouyang/AndroidStudioProjects/MyApplication/chapter13/libs/tyrus-standalone-client-1.17.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}