package com.aguacero.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aguacero.app.util.crearCanalNotificaciones
import com.aguacero.app.workers.TrabajoNuevosEventos
import java.util.concurrent.TimeUnit

/**
 * Aplicación de Aguacero: crea el canal de notificaciones y programa
 * la revisión periódica de eventos nuevos (cada 6 horas).
 */
class AguaceroApp : Application() {

    override fun onCreate() {
        super.onCreate()
        crearCanalNotificaciones(this)

        // El trabajo se encola siempre; dentro doWork() se comprueba
        // si el usuario activó o no las notificaciones.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            TrabajoNuevosEventos.NOMBRE_TRABAJO,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<TrabajoNuevosEventos>(6, TimeUnit.HOURS)
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build()
                )
                .build()
        )
    }
}
