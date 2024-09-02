    package com.example.picplace.services

    import android.app.NotificationManager
    import android.app.PendingIntent
    import android.app.Service
    import android.content.Context
    import android.content.Intent
    import android.os.Build
    import android.os.IBinder
    import android.util.Log
    import androidx.annotation.RequiresApi
    import androidx.core.app.NotificationCompat
    import com.example.picplace.MainActivity
    import com.example.picplace.R
    import com.example.picplace.models.place.PlaceFirebase
    import com.example.picplace.utils.DefaultLocationClient
    import com.example.picplace.utils.LocationClient
    import com.google.android.gms.location.LocationServices
    import com.google.firebase.firestore.FirebaseFirestore
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.SupervisorJob
    import kotlinx.coroutines.cancel
    import kotlinx.coroutines.delay
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import kotlin.math.pow
    import kotlin.math.sqrt

    class NearbyCheckService : Service() {

        private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private lateinit var notificationManager: NotificationManager
        private lateinit var firestore: FirebaseFirestore
        private lateinit var locationClient: LocationClient

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate() {
            super.onCreate()
            firestore = FirebaseFirestore.getInstance()
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            locationClient = DefaultLocationClient(
                applicationContext,
                LocationServices.getFusedLocationProviderClient(applicationContext)
            )
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            when (intent?.action) {
                ACTION_START -> startNearbyCheck()
                ACTION_STOP -> stopNearbyCheck()
            }
            return super.onStartCommand(intent, flags, startId)
        }

        private fun startNearbyCheck() {
            serviceScope.launch {
                while (true) {
                    checkNearbyObjects()
                    delay(60000L)
                }
            }
        }

        private suspend fun checkNearbyObjects() = withContext(Dispatchers.IO) {
            locationClient.getLocationUpdates(60000L).collect { location ->
                val currentLat = location.latitude
                val currentLong = location.longitude

                firestore.collection("places")
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val place = document.toObject(PlaceFirebase::class.java)
                            val latLng = place.latLng.toLatLng()

                            if (isNearby(currentLat, currentLong, latLng.latitude, latLng.longitude)) {
                                showNotification("Nearby place detected!", "Place at (${latLng.latitude}, ${latLng.longitude}) is nearby.")
                                break
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("NearbyCheckService", "Error fetching nearby objects", e)
                    }
            }
        }

        private fun isNearby(userLat: Double, userLong: Double, objectLat: Double, objectLong: Double): Boolean {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(userLat, userLong, objectLat, objectLong, results)
            return results[0] < 10 // 10 meters
        }

        private fun showNotification(title: String, content: String) {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, "nearbyCheck")
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_logoo)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(2, notification)
        }

        private fun stopNearbyCheck() {
            serviceScope.cancel()
            stopSelf()
        }

        override fun stopService(name: Intent?): Boolean {
            stopNearbyCheck()
            return super.stopService(name)
        }

        override fun onDestroy() {
            super.onDestroy()
            serviceScope.cancel()
        }

        companion object {
            const val ACTION_START = "ACTION_START"
            const val ACTION_STOP = "ACTION_STOP"
        }
    }
