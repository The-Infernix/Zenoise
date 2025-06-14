package com.example.noisemap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.noisemap.utils.LocationHelper
import com.example.noisemap.utils.NoiseMeter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class MainActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapWebView: WebView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var decibelText: TextView
    private lateinit var manualRecordButton: Button
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private val handler = Handler(Looper.getMainLooper())

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            Toast.makeText(this, "Permissions are required.", Toast.LENGTH_LONG).show()
        } else {
            startAutoRecording()
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 2000L // 2 seconds interval
            fastestInterval = 1000L // min interval between updates
        }
    }



    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val lat = location.latitude
                    val lon = location.longitude
                    // Update WebView on UI thread
                    runOnUiThread {
                        mapWebView.evaluateJavascript("updateUserLocation($lat, $lon);", null)
                    }
                }
            }
        }
    }


    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        decibelText = findViewById(R.id.decibelText)
        manualRecordButton = findViewById(R.id.manualRecordButton)

        manualRecordButton.setOnClickListener {
            recordNoise(manual = true)
        }

        mapWebView = findViewById(R.id.mapWebView)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)

        mapWebView.settings.javaScriptEnabled = true
        mapWebView.settings.domStorageEnabled = true
        mapWebView.loadUrl("file:///android_asset/heatmap.html")

        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener {
                firestore = FirebaseFirestore.getInstance()
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                checkAndRequestPermissions()

            }

            .addOnFailureListener {
                Toast.makeText(this, "Firebase sign-in failed", Toast.LENGTH_SHORT).show()
            }

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                val escapedQuery = query.replace("'", "\\'")
                mapWebView.evaluateJavascript("searchLocation('$escapedQuery');", null)
            } else {
                Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserLocationOnce() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                mapWebView.evaluateJavascript("updateUserLocation($lat, $lon);", null)
            } else {
                // If last location is null, request current location or show error
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionsNeeded.isNotEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            createLocationRequest()
            setupLocationCallback()
            startLocationUpdates()
            startAutoRecording()
        }
    }


    private fun startAutoRecording() {
        handler.post(object : Runnable {
            override fun run() {
                try {
                    val decibel = NoiseMeter.getDecibel()

                    runOnUiThread {
                        decibelText.text = "dB: $decibel"
                    }

                    // Upload only if > 60 dB
                    if (decibel > 65) {
                        recordNoiseWithDecibel(decibel)
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error reading microphone",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                handler.postDelayed(this, 2000) // check every 2 seconds
            }
        })
    }

    private fun recordNoiseWithDecibel(decibel: Int) {
        LocationHelper.getLocation(this, fusedLocationClient) { lat, lon ->
            mapWebView.post {
                mapWebView.evaluateJavascript("updateUserLocation($lat, $lon);", null)
            }

            val data = hashMapOf(
                "decibel" to decibel,
                "latitude" to lat,
                "longitude" to lon,
                "timestamp" to FieldValue.serverTimestamp(),
                "device" to android.os.Build.MODEL,
                "type" to "auto"
            )

            firestore.collection("readings")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Noise > 65 dB uploaded", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun recordNoise(manual: Boolean = false) {
        try {
            val decibel = NoiseMeter.getDecibel()

            runOnUiThread {
                decibelText.text = "dB: $decibel"
            }

            LocationHelper.getLocation(this, fusedLocationClient) { lat, lon ->
                mapWebView.post {
                    mapWebView.evaluateJavascript("updateUserLocation($lat, $lon);", null)
                }

                val data = hashMapOf(
                    "decibel" to decibel,
                    "latitude" to lat,
                    "longitude" to lon,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "device" to android.os.Build.MODEL,
                    "type" to if (manual) "manual" else "auto"
                )

                firestore.collection("readings")
                    .add(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Noise data uploaded ✅", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Upload failed ❌", Toast.LENGTH_SHORT).show()
                    }
            }

        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission denied for recording audio.", Toast.LENGTH_SHORT)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error recording noise data.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        handler.removeCallbacksAndMessages(null)
    }
}