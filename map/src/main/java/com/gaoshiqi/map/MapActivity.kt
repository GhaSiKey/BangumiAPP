package com.gaoshiqi.map

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gaoshiqi.map.R
import com.gaoshiqi.map.data.LitePoint
import com.gaoshiqi.map.databinding.ActivityMapsBinding
import com.gaoshiqi.map.utils.PointListSingleton
import com.gaoshiqi.map.widget.LitePointAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.math.abs

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

        mPoints?.let {
            val adapter = LitePointAdapter(it)
            mBinding.viewPager.adapter = adapter
            mBinding.viewPager.offscreenPageLimit = 1
            mBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position < mMarkers.size) {
                        val marker = mMarkers[position]
                        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 15f))
                        marker.showInfoWindow()
                    }
                }
            })
            mBinding.viewPager.addItemDecoration(object: RecyclerView.ItemDecoration(){
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.left = 100
                    outRect.right = 100
                }
            })
            mBinding.viewPager.setPageTransformer { page, position ->
                page.translationX = -350 * position
                page.scaleX = 1 - abs(position) * 0.4f
                page.scaleY = 1 - abs(position) * 0.4f
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        mPoints?.forEachIndexed { index, it ->
            val geo = it.geo
            if (geo.size == 2) {
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(geo[0], geo[1]))
                        .title(it.name)
                        .alpha(0.75f)
                )
                marker?.let {
                    it.tag = index
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

        googleMap.setOnMarkerClickListener { marker ->
            val position = marker.tag as? Int
            position?.let {
                mBinding.viewPager.currentItem = it
            }
            true
        }
    }
}