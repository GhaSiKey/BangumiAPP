package com.example.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Created by gaoshiqi
 * on 2025/7/6 16:24
 * email: gaoshiqi@bilibili.com
 */
class MapBottomSheetFragment: BottomSheetDialogFragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var location: LatLng? = null
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            location = LatLng(it.getDouble(ARG_LAT), it.getDouble(ARG_LNG))
            name = it.getString(ARG_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_bottom_sheet, container, false)
        mapView = view.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialog = dialog as? BottomSheetDialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.isDraggable = false
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        location?.let {
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(name)
            )
            marker?.showInfoWindow()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    // MapView生命周期方法
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    companion object {
        private const val ARG_LAT = "latitude"
        private const val ARG_LNG = "longitude"
        private const val ARG_NAME = "name"

        fun newInstance(lat: Double, lng: Double, name: String? = null): MapBottomSheetFragment {
            val fragment = MapBottomSheetFragment()
            val args = Bundle().apply {
                putDouble(ARG_LAT, lat)
                putDouble(ARG_LNG, lng)
                putString(ARG_NAME, name)
            }
            fragment.arguments = args
            return fragment
        }
    }
}