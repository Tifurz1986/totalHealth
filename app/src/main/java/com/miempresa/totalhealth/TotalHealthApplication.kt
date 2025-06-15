package com.miempresa.totalhealth
import android.app.Application
import android.util.Log // Importación para Log.d
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck // Importación para FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory // Importación para DebugAppCheckProviderFactory

// import com.google.firebase.ktx.Firebase
// import com.google.firebase.ktx.initialize

class TotalHealthApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar Firebase.
        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        Log.d("TotalHealthApplication", "FirebaseApp and DebugAppCheckProvider initialized. Check Logcat for App Check debug token if needed.")
    }
}
