package com.example.task.di

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.Places.initialize
import dagger.hilt.android.HiltAndroidApp
import java.util.*

@HiltAndroidApp
class App:Application() {
//    if (!Places.isInitialized()) {
//        initialize(getApplicationContext(), getString(R.string.google_api_key), Locale.US);
//    }
}