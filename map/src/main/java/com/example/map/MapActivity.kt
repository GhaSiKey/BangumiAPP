package com.example.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import com.example.map.data.LitePoint
import com.example.map.databinding.ActivityMapsBinding
import com.example.map.utils.PointListSingleton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Created by gaoshiqi
 * on 2025/7/6 16:15
 * email: gaoshiqi@bilibili.com
 */
class MapActivity: AppCompatActivity(), OnMapReadyCallback{

    private var mMap: GoogleMap? = null
    private lateinit var mBinding: ActivityMapsBinding
    private var mPoints: List<LitePoint>? = null
    private val mMarkers = mutableListOf<Marker>()

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.root.setOnApplyWindowInsetsListener { view, insets ->
            val topInset = insets.getInsets(WindowInsets.Type.statusBars()).top
            val bottomInset = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            view.setPadding(0, topInset, 0, bottomInset)
            insets
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mPoints = PointListSingleton.getPointList()
        PointListSingleton.clear()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        mPoints?.forEach {
            val geo = it.geo
            if (geo.size == 2) {
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(geo[0], geo[1]))
                        .title(it.name)
                        .alpha(0.75f)
                )
                marker?.let {
                    mMarkers.add(it)
                }
            }
        }
        mMarkers.firstOrNull().let {
            if (it?.position != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it.position, 15f))
                it.showInfoWindow()
            }
        }
    }
}