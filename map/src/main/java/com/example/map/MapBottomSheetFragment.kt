package com.example.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.map.databinding.FragmentMapBottomSheetBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Created by gaoshiqi
 * on 2025/7/6 16:24
 * email: gaoshiqi@bilibili.com
 */
class MapBottomSheetFragment : BottomSheetDialogFragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBottomSheetBinding? = null
    private val binding get() = _binding!!

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
    ): View {
        _binding = FragmentMapBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = name ?: ""
        binding.btnClose.setOnClickListener { dismiss() }

        (dialog as? BottomSheetDialog)?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )?.setBackgroundResource(android.R.color.transparent)

        // 延迟初始化地图，让弹窗先显示出来
        view.post {
            binding.map.onCreate(null)
            binding.map.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        binding.loadingContainer.visibility = View.GONE

        location?.let {
            googleMap.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(name)
            )?.showInfoWindow()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    override fun onResume() {
        super.onResume()
        _binding?.map?.onResume()
    }

    override fun onPause() {
        super.onPause()
        _binding?.map?.onPause()
    }

    override fun onDestroyView() {
        _binding?.map?.onDestroy()
        _binding = null
        super.onDestroyView()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        _binding?.map?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.map?.onSaveInstanceState(outState)
    }

    companion object {
        private const val TAG = "MapBottomSheetFragment"
        private const val ARG_LAT = "latitude"
        private const val ARG_LNG = "longitude"
        private const val ARG_NAME = "name"

        /**
         * 显示地图弹窗，自动防止重复显示
         */
        fun show(fragmentManager: FragmentManager, lat: Double, lng: Double, name: String? = null) {
            // 防止重复显示
            if (fragmentManager.findFragmentByTag(TAG) != null) return

            val fragment = MapBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putDouble(ARG_LAT, lat)
                    putDouble(ARG_LNG, lng)
                    putString(ARG_NAME, name)
                }
            }
            fragment.show(fragmentManager, TAG)
        }
    }
}