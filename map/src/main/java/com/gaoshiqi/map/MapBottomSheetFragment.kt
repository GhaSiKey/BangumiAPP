package com.gaoshiqi.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.gaoshiqi.map.data.LitePoint
import com.gaoshiqi.map.databinding.FragmentMapBottomSheetBinding
import com.gaoshiqi.map.utils.GoogleMapUtils
import com.gaoshiqi.map.utils.LitePointHolder
import com.gaoshiqi.room.SavedPointEntity
import com.gaoshiqi.room.SavedPointRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

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

    // 完整的地点信息（用于收藏功能）
    private var litePoint: LitePoint? = null
    private var isSaved: Boolean = false
    private var isSaveOperating: Boolean = false

    private val repository: SavedPointRepository by lazy {
        SavedPointRepository(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            location = LatLng(args.getDouble(ARG_LAT), args.getDouble(ARG_LNG))
            name = args.getString(ARG_NAME)
        }
        // 从 Holder 获取完整的 LitePoint（取后自动清除）
        litePoint = LitePointHolder.getAndClear()
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

        setupOpenMapsButton()
        setupSaveButton()

        (dialog as? BottomSheetDialog)?.findViewById<View>(
            R.id.design_bottom_sheet
        )?.setBackgroundResource(android.R.color.transparent)

        // 延迟初始化地图，让弹窗先显示出来
        view.post { initMapView() }
    }

    private fun initMapView() {
        binding.map.onCreate(null)
        binding.map.onResume()
        binding.map.getMapAsync(this)
    }

    private fun setupOpenMapsButton() {
        binding.btnOpenMaps.setOnClickListener {
            val loc = location ?: return@setOnClickListener
            GoogleMapUtils.openInGoogleMaps(requireContext(), loc.latitude, loc.longitude, name)
        }
    }

    private fun setupSaveButton() {
        val point = litePoint
        if (point == null || point.subjectId == 0) {
            // 没有完整信息，隐藏收藏按钮
            binding.btnSave.visibility = View.GONE
            return
        }

        // 检查是否已收藏
        lifecycleScope.launch {
            isSaved = repository.isSaved(point.subjectId, point.id)
            updateSaveButtonState()
        }

        binding.btnSave.setOnClickListener {
            if (isSaveOperating) return@setOnClickListener
            toggleSaveState()
        }
    }

    private fun toggleSaveState() {
        val point = litePoint ?: return

        lifecycleScope.launch {
            isSaveOperating = true
            binding.btnSave.isEnabled = false

            try {
                if (isSaved) {
                    // 取消收藏
                    val id = SavedPointEntity.generateId(point.subjectId, point.id)
                    repository.removePointById(id)
                    isSaved = false
                    Toast.makeText(requireContext(), com.gaoshiqi.map.R.string.point_unsaved, Toast.LENGTH_SHORT).show()
                } else {
                    // 添加收藏
                    val entity = SavedPointEntity(
                        id = SavedPointEntity.generateId(point.subjectId, point.id),
                        subjectId = point.subjectId,
                        subjectName = point.subjectName,
                        subjectCover = point.subjectCover,
                        pointId = point.id,
                        pointName = point.name,
                        pointNameCn = point.cn ?: "",
                        pointImage = point.image,
                        lat = point.lat(),
                        lng = point.lng(),
                        episode = point.ep,
                        timeInEpisode = point.s
                    )
                    repository.savePoint(entity)
                    isSaved = true
                    Toast.makeText(requireContext(), com.gaoshiqi.map.R.string.point_saved, Toast.LENGTH_SHORT).show()
                }
                updateSaveButtonState()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), com.gaoshiqi.map.R.string.point_save_failed, Toast.LENGTH_SHORT).show()
            } finally {
                isSaveOperating = false
                binding.btnSave.isEnabled = true
            }
        }
    }

    private fun updateSaveButtonState() {
        binding.btnSave.setImageResource(
            if (isSaved) com.gaoshiqi.map.R.drawable.ic_bookmark_filled
            else com.gaoshiqi.map.R.drawable.ic_bookmark_border
        )
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
         * 显示地图弹窗（简单模式，无收藏功能）
         */
        fun show(fragmentManager: FragmentManager, lat: Double, lng: Double, name: String? = null) {
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

        /**
         * 显示地图弹窗（完整模式，支持收藏功能）
         */
        fun show(fragmentManager: FragmentManager, point: LitePoint) {
            if (fragmentManager.findFragmentByTag(TAG) != null) return

            // 通过 Holder 传递完整数据
            LitePointHolder.set(point)

            val fragment = MapBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putDouble(ARG_LAT, point.lat())
                    putDouble(ARG_LNG, point.lng())
                    putString(ARG_NAME, point.displayName())
                }
            }
            fragment.show(fragmentManager, TAG)
        }
    }
}