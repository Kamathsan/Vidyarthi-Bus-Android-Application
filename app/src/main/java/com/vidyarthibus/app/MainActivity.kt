package com.vidyarthibus.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.vidyarthibus.app.databinding.ActivityMainBinding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // MANGALURU CAMPUS LOCATIONS
    private val campusCoordinates = mapOf(
        0 to GeoPoint(12.8735, 74.8465), 1 to GeoPoint(13.0108, 74.7943),
        2 to GeoPoint(13.3525, 74.7928), 3 to GeoPoint(12.8158, 74.9341),
        4 to GeoPoint(12.8020, 74.8770), 5 to GeoPoint(12.8660, 74.9250),
        6 to GeoPoint(12.9050, 74.8980), 7 to GeoPoint(13.1800, 74.9350)
    )

    // CAMPUS -> VALID BUS NUMBERS
    private val campusBuses = mapOf(
        0 to listOf("13", "14A", "15", "17", "31"), 1 to listOf("2", "2A", "15", "15A", "41A", "45"),
        2 to listOf("Express", "Service"), 3 to listOf("51", "55", "Express"),
        4 to listOf("42", "43", "44A"), 5 to listOf("10A", "10B", "30A", "30B"),
        6 to listOf("3", "3A", "3B", "3D", "12A", "22"), 7 to listOf("Karkala Express", "Nitte Direct")
    )

    // DEMO MODE: MANUAL STOP SELECTIONS BASED ON SELECTED CAMPUS
    // EXPANDED DEMO MODE: MANUAL STOP SELECTIONS BASED ON SELECTED CAMPUS
    private val demoStops = mapOf(
        0 to mapOf("State Bank" to GeoPoint(12.8647, 74.8360), "Ladyhill" to GeoPoint(12.8850, 74.8350), "KSRTC" to GeoPoint(12.8875, 74.8373), "Lalbagh" to GeoPoint(12.8810, 74.8380), "Jyothi" to GeoPoint(12.8700, 74.8480)),
        1 to mapOf("State Bank" to GeoPoint(12.8647, 74.8360), "Kuloor" to GeoPoint(12.9150, 74.8150), "KSRTC" to GeoPoint(12.8875, 74.8373), "Lalbagh" to GeoPoint(12.8810, 74.8380), "Baikampady" to GeoPoint(12.9450, 74.8080)),
        2 to mapOf("Udupi Bus Stand" to GeoPoint(13.3409, 74.7421), "Tiger Circle" to GeoPoint(13.3500, 74.7850), "Syndicate Circle" to GeoPoint(13.3460, 74.7810)),
        3 to mapOf("State Bank" to GeoPoint(12.8647, 74.8360), "Pumpwell" to GeoPoint(12.8631, 74.8569), "Thokkottu" to GeoPoint(12.8100, 74.8550), "Kankanady" to GeoPoint(12.8660, 74.8550), "Deralakatte" to GeoPoint(12.8020, 74.8770)),
        4 to mapOf("State Bank" to GeoPoint(12.8647, 74.8360), "Pumpwell" to GeoPoint(12.8631, 74.8569), "Thokkottu" to GeoPoint(12.8100, 74.8550), "Kankanady" to GeoPoint(12.8660, 74.8550)),
        5 to mapOf("State Bank" to GeoPoint(12.8647, 74.8360), "Pumpwell" to GeoPoint(12.8631, 74.8569), "Mangalore Junction" to GeoPoint(12.8636, 74.8785), "Jyothi" to GeoPoint(12.8700, 74.8480), "Padil" to GeoPoint(12.8730, 74.8820)),
        6 to mapOf("State Bank" to GeoPoint(12.8647, 74.8360), "KSRTC" to GeoPoint(12.8875, 74.8373), "Mallikatte" to GeoPoint(12.8750, 74.8600), "Bikkarnakatta" to GeoPoint(12.8800, 74.8650), "Kulshekar" to GeoPoint(12.8880, 74.8760)),
        7 to mapOf("State Bank" to GeoPoint(12.8647, 74.8360), "Moodabidri" to GeoPoint(13.0650, 74.9900), "Karkala" to GeoPoint(13.2000, 74.9950), "Gurupura" to GeoPoint(12.9250, 74.9450))
    )


    private var selectedCampusIndex = 0
    private var isAutoRoute = false

    // Multi-bus tracker variables
    private var currentDisplayBus = ""
    private var followingDisplayBus = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference.child("routes")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        selectedCampusIndex = intent.getIntExtra("CAMPUS_INDEX", 0)
        isAutoRoute = intent.getBooleanExtra("AUTO_ROUTE", false)

        setupInitialMap()
        setupLocationSpinner()

        // Pick the actual real-world bus numbers for this campus
        val validBuses = campusBuses[selectedCampusIndex] ?: listOf("15", "19", "27")
        currentDisplayBus = validBuses.random()
        followingDisplayBus = validBuses.filter { it != currentDisplayBus }.randomOrNull() ?: currentDisplayBus

        binding.tvBusNumber.text = currentDisplayBus
        binding.tvBusTime.text = "Arriving in ${(2..12).random()} mins"
        binding.tvBusFollowing.text = "Following bus ($followingDisplayBus) in ${(15..30).random()} mins"

        binding.tvNextBusStatusHeader.text = "BUS $currentDisplayBus CROWD METER (Next Arrival)"
        binding.tvFollowingBusStatusHeader.text = "BUS $followingDisplayBus CROWD METER (Following)"

        // Start Firebase Tracking for BOTH buses!
        listenToCrowdData("campus_route_$selectedCampusIndex", currentDisplayBus, true)
        listenToCrowdData("campus_route_$selectedCampusIndex", followingDisplayBus, false)

        binding.tvLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }

        binding.btnReportEmpty.setOnClickListener { checkLocationAndReport("empty") }
        binding.btnReportSeated.setOnClickListener { checkLocationAndReport("seated") }
        binding.btnReportFull.setOnClickListener { checkLocationAndReport("full") }
    }

    private fun setupLocationSpinner() {
        if (isAutoRoute) {
            binding.tvLocationLabel.text = "SELECT YOUR CURRENT STOP (DEMO MODE)"
        } else {
            binding.tvLocationLabel.text = "SELECT YOUR DROPOFF DESTINATION"
        }

        val availableStops = demoStops[selectedCampusIndex] ?: mapOf("State Bank" to GeoPoint(12.8647, 74.8360))
        val stopNames = availableStops.keys.toList()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stopNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLocation.adapter = adapter

        binding.spinnerLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStopGeo = availableStops[stopNames[position]] ?: GeoPoint(12.8647, 74.8360)
                val campusGeo = campusCoordinates[selectedCampusIndex] ?: GeoPoint(12.8735, 74.8465)

                Toast.makeText(this@MainActivity, "Routing...", Toast.LENGTH_SHORT).show()

                if (isAutoRoute) {
                    fetchOSRMRoute(selectedStopGeo, campusGeo) // Route TO campus
                } else {
                    fetchOSRMRoute(campusGeo, selectedStopGeo) // Route FROM campus
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchOSRMRoute(startPoint: GeoPoint, endPoint: GeoPoint) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "https://routing.openstreetmap.de/routed-car/route/v1/driving/${startPoint.longitude},${startPoint.latitude};${endPoint.longitude},${endPoint.latitude}?overview=full&geometries=geojson"
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val routes = jsonResponse.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val geometry = routes.getJSONObject(0).getJSONObject("geometry")
                        val coordinates = geometry.getJSONArray("coordinates")

                        val routePoints = ArrayList<GeoPoint>()
                        for (i in 0 until coordinates.length()) {
                            val point = coordinates.getJSONArray(i)
                            val lon = point.getDouble(0)
                            val lat = point.getDouble(1)
                            routePoints.add(GeoPoint(lat, lon))
                        }

                        withContext(Dispatchers.Main) {
                            drawDynamicRoute(routePoints, startPoint, endPoint)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun drawDynamicRoute(points: ArrayList<GeoPoint>, startPoint: GeoPoint, endPoint: GeoPoint) {
        val map = binding.mapView
        map.overlays.clear()

        if (points.size > 1) {
            val routePolyline = Polyline()
            routePolyline.setPoints(points)
            routePolyline.outlinePaint.color = android.graphics.Color.parseColor("#1976D2")
            routePolyline.outlinePaint.strokeWidth = 14f
            map.overlays.add(routePolyline)
        }

        val startMarker = Marker(map)
        startMarker.position = startPoint
        startMarker.title = if (isAutoRoute) "You are here" else "Campus"
        startMarker.icon = resources.getDrawable(android.R.drawable.ic_menu_mylocation, null)
        map.overlays.add(startMarker)

        val endMarker = Marker(map)
        endMarker.position = endPoint
        endMarker.title = "Destination"
        map.overlays.add(endMarker)

        // MOCK LIVE BUS MAP PINGS!
        // Places a mock bus icon roughly 30% and 60% along the generated route to make the map look alive!
        if (points.size > 20) {
            val bus1Marker = Marker(map)
            bus1Marker.position = points[points.size / 3]
            bus1Marker.title = "Bus $currentDisplayBus Approaching"
            bus1Marker.icon = resources.getDrawable(android.R.drawable.ic_dialog_map, null)
            map.overlays.add(bus1Marker)

            val bus2Marker = Marker(map)
            bus2Marker.position = points[(points.size * 2) / 3]
            bus2Marker.title = "Following Bus $followingDisplayBus"
            bus2Marker.icon = resources.getDrawable(android.R.drawable.ic_dialog_map, null)
            map.overlays.add(bus2Marker)
        }

        map.controller.setZoom(13.0)
        val midLat = (startPoint.latitude + endPoint.latitude) / 2.0
        val midLng = (startPoint.longitude + endPoint.longitude) / 2.0
        map.controller.setCenter(GeoPoint(midLat, midLng))

        map.invalidate()
    }

    private fun setupInitialMap() {
        val map = binding.mapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
        map.setMultiTouchControls(true)

        map.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    private fun checkLocationAndReport(status: String) {
        val validBuses = campusBuses[selectedCampusIndex] ?: listOf("15", "19", "27")
        val options = validBuses.toTypedArray()

        // Bus-Specific Reporting!
        android.app.AlertDialog.Builder(this)
            .setTitle("Which Bus are you travelling in?")
            .setItems(options) { _, index ->
                val selectedBusToReport = options[index]
                submitReport(status, selectedBusToReport)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitReport(status: String, busNumber: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "demo_student_${System.currentTimeMillis()}"
        val reportData = mapOf("status" to status, "timestamp" to System.currentTimeMillis())

        database.child("campus_route_$selectedCampusIndex").child(busNumber).child("reports").child(userId).setValue(reportData)
            .addOnSuccessListener {
                Toast.makeText(this, "Status Broadcasted for Bus $busNumber!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenToCrowdData(routeId: String, busNumber: String, isNextBus: Boolean) {
        database.child(routeId).child(busNumber).child("reports").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var validReportsCount = 0
                var totalScore = 0.0
                val currentTime = System.currentTimeMillis()

                for (reportSnapshot in snapshot.children) {
                    val timestamp = reportSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    val status = reportSnapshot.child("status").getValue(String::class.java) ?: ""

                    if (currentTime - timestamp <= 900000) {
                        validReportsCount++
                        when (status) {
                            "empty" -> totalScore += 0.0
                            "seated" -> totalScore += 50.0
                            "full" -> totalScore += 100.0
                        }
                    } else {
                        reportSnapshot.ref.removeValue()
                    }
                }
                updateSpecificCrowdMeter(validReportsCount, totalScore, isNextBus)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateSpecificCrowdMeter(reportCount: Int, totalScore: Double, isNextBus: Boolean) {
        val averagePercentage = if (reportCount == 0) 0 else (totalScore / reportCount).toInt()

        val labelView = if (isNextBus) binding.tvNextBusCrowdLabel else binding.tvFollowingBusCrowdLabel
        val meterView = if (isNextBus) binding.nextBusCrowdMeter else binding.followingBusCrowdMeter

        if (reportCount == 0) {
            labelView.text = "Awaiting Data"
            labelView.setTextColor(android.graphics.Color.parseColor("#9E9E9E"))
            meterView.progress = 0
            return
        }

        meterView.progress = averagePercentage

        when {
            averagePercentage < 33 -> {
                labelView.text = "SEATS AVAILABLE"
                labelView.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                meterView.setIndicatorColor(android.graphics.Color.parseColor("#4CAF50"))
            }
            averagePercentage < 66 -> {
                labelView.text = "GETTING FULL"
                labelView.setTextColor(android.graphics.Color.parseColor("#E65100"))
                meterView.setIndicatorColor(android.graphics.Color.parseColor("#FF9800"))
            }
            else -> {
                labelView.text = "BUS IS FULL"
                labelView.setTextColor(android.graphics.Color.parseColor("#C62828"))
                meterView.setIndicatorColor(android.graphics.Color.parseColor("#F44336"))
            }
        }
    }
}
