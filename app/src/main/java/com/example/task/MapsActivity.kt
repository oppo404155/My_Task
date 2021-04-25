package com.example.task

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.task.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapsActivity : AppCompatActivity() {
    private val viewmodel: MapViewModel by viewModels()

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment

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
        getcurrentLocation()
        val madapter = MapAdapter()
        binding.apply {
            recycler.apply {
                layoutManager = LinearLayoutManager(this@MapsActivity)
                adapter = madapter
            }
            sourceEdt.editText?.setOnClickListener {
                recycler.isVisible = true
            }
            sourceEdt.editText?.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    recycler.isVisible = false
                }
            }
            sourceEdt
            madapter.onItemClick = {
                sourceEdt.editText?.setText(it.name)
                recycler.isVisible = false
            }

        }


        viewmodel.apply {
            setNewDestItem(Data("ahmed", 45.0, 90.0))

            getCurrentSources().observe(this@MapsActivity) {
                madapter.submitList(it)
            }
        }


    }

    fun getcurrentLocation() {

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


    companion object {
        private const val REQUEST_CODE = 44
    }


}