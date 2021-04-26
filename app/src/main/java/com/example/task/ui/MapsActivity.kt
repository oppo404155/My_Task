package com.example.task.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.task.ui.adapters.MapAdapter
import com.example.task.R
import com.example.task.data.Data
import com.example.task.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.*

@AndroidEntryPoint
class MapsActivity : AppCompatActivity() {
    private val viewmodel: MapViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var destinationSelectedObject: Data
    private var LocationselecedObject: Data? = null
    private var driversList = ArrayList<Data>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        binding.menuIcon.setOnClickListener {
            val mdrawer = binding.drawerLayout
            if (!mdrawer.isDrawerOpen(Gravity.LEFT))
                mdrawer.openDrawer(Gravity.LEFT)
            else mdrawer.closeDrawer(Gravity.LEFT)

        }

//        // Initialize the AutocompleteSupportFragment.
        Places.initialize(
            getApplicationContext(),
            API_KEY,
            Locale.US
        )


        val fields = listOf(Place.Field.ID, Place.Field.NAME)

        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(this)


        getcurrentLocation()
        val madapter = MapAdapter()
        binding.apply {
            recycler.apply {
                layoutManager = LinearLayoutManager(this@MapsActivity)
                adapter = madapter
            }
            sourceEdt.editText?.apply {

                setOnTouchListener { v, event ->
                    recycler.isVisible = true
                    false
                }
                setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        recycler.isVisible = false
                    }
                }
            }

            madapter.onItemClick = {
                sourceEdt.editText?.setText(it.name)
                LocationselecedObject = it
                recycler.isVisible = false
            }
            //fetch the nearest driver name and distance from location
            searchGoBtn.setOnClickListener {

//                  if(sourceEdt.editText?.text?.isNotEmpty()!!){
                val map = findnearestDriver(LocationselecedObject!!)
                val name = map.toList()[0].first
                val distance = map.toList()[0].second.toInt()
                val seconedDistance = map.toList()[1].second.toInt()
                val secondName = map.toList()[1].first
                Toast.makeText(
                    this@MapsActivity,
                    "$name is the nearest driver with distance $distance/Km " +
                            "and the next nearest one is" +
                            "  $secondName with distance $seconedDistance/Km",
                    Toast.LENGTH_LONG
                ).show()
//                   }
//                Toast.makeText(
//                    this@MapsActivity,
//                    "select the location first to order a driver",
//                    Toast.LENGTH_LONG
//                ).show()


            }
            // start google maps location intent with overlay view

            destinationEdt.editText?.setOnTouchListener { v, event ->
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
                recycler.isVisible = false
                false
            }

        }

        // filling with data recycler adapter
        viewmodel.apply {
            //setNewDestItem(Data("Arizona", 45.758, -90.5646))
            getCurrentSources().observe(this@MapsActivity) {
                madapter.submitList(it)
            }
            getAllDrivers().observe(this@MapsActivity) {
                driversList = it
            }
        }


    }
    //return user current location with marker

    private fun getcurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val task = fusedLocationClient.lastLocation
            task.addOnSuccessListener { location ->
                if (location != null) {
                    mapFragment.getMapAsync { googlemap ->
                        val lating = LatLng(location.latitude, location.longitude)
                        val option = MarkerOptions().position(lating).title("i'm here ")
                        googlemap.animateCamera(CameraUpdateFactory.newLatLngZoom(lating, 10f))
                        googlemap.addMarker(option)

                    }
                }


            }

        } else {
            ActivityCompat.requestPermissions(
                this@MapsActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE
            )
        }

    }
    // maps runtime permission

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getcurrentLocation()
        }
    }

    //GOOGLE maps place API result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        destinationSelectedObject = Data(
                            place.name.toString(),
                            place.latLng!!.latitude,
                            place.latLng!!.longitude
                        )

                        viewmodel.setNewDestItem(destinationSelectedObject)

                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i("TAG", status.statusMessage)
                    }
                }
                Activity.RESULT_CANCELED -> {

                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // fetch the nearest drivers name and distance in an ordered map

    private fun findnearestDriver(source: Data): Map<String, Double> {
        val allDistances = HashMap<String, Double>()

        driversList.forEach {
            Log.d("driver", it.name)
            val distance = find_distance(
                source.latitude,
                source.longitude,
                it.latitude, it.longitude
            )
            allDistances[it.name] = distance
        }


        return allDistances.toList().sortedBy {
            it.second
        }.toMap()


    }

    // find distance between two geo points on earth

    fun find_distance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {

        val Rlng1 = Math.toRadians(lng1)
        val Rlng2 = Math.toRadians(lng2)
        val Rlat1 = Math.toRadians(lat1)
        val Rlat2 = Math.toRadians(lat2)
        // Haversine formula
        val dlon = Rlng2 - Rlng1
        val dlat = Rlat2 - Rlat1
        val a = (sin(dlat / 2).pow(2.0)
                + (cos(lat1) * cos(lat2)
                * sin(dlon / 2).pow(2.0)))
        val c = 2 * asin(sqrt(a))
        val r = 6371.0

        return c * r
    }


    companion object {
        private const val REQUEST_CODE = 44
        private const val AUTOCOMPLETE_REQUEST_CODE = 1
        private const val API_KEY = "AIzaSyBOvpFHU9PrhkoPUmkufK4pIHMyHV8g374"
    }


}