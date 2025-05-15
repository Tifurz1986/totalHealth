package com.miempresa.totalhealth // Asegúrate de que esta sea la primera línea y coincida con la ubicación

import android.app.Application
import android.util.Log // Importación para Log.d
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck // Importación para FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory // Importación para DebugAppCheckProviderFactory
// Si usas Firebase.initialize en algún otro sitio (generalmente no es necesario si FirebaseApp.initializeApp se llama aquí):
// import com.google.firebase.ktx.Firebase
// import com.google.firebase.ktx.initialize

class TotalHealthApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar Firebase.
        // Es buena práctica llamarlo aquí para asegurar que esté listo.
        FirebaseApp.initializeApp(this)

        // Instalar el proveedor de depuración de App Check.
        // Esto es crucial para probar en emuladores y dispositivos de depuración.
        // En producción, configurarías Play Integrity u otro proveedor.
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        // Este log te ayudará a encontrar el token de depuración en Logcat si aún no lo has añadido a la consola de Firebase
        Log.d("TotalHealthApplication", "FirebaseApp and DebugAppCheckProvider initialized. Check Logcat for App Check debug token if needed.")
    }
}
