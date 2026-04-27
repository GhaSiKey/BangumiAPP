package com.gaoshiqi.map

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gaoshiqi.map.data.LitePoint
import com.gaoshiqi.map.databinding.ActivityMapsBinding
import com.gaoshiqi.map.utils.PointListSingleton
import com.gaoshiqi.map.widget.CustomInfoView
import com.gaoshiqi.map.widget.LitePointAdapter
import com.gaoshiqi.room.SavedPointEntity
import com.gaoshiqi.room.SavedPointRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import kotlin.math.abs

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private lateinit var mBinding: ActivityMapsBinding
    private var mPoints: List<LitePoint>? = null
    private val mMarkers = mutableListOf<Marker>()
    private val mMarkerImageMap = mutableMapOf<Int, String>()
    private var mAdapter: LitePointAdapter? = null

    private val repository: SavedPointRepository by lazy {
        SavedPointRepository(this)
    }
    private val savedStates = mutableMapOf<Int, Boolean>()

    // 集数到颜色索引的映射
    private val episodeColorMap = mutableMapOf<String, Int>()

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

        // 初始化集数颜色映射
        initEpisodeColorMap()

        mBinding.btnBack.setOnClickListener { finish() }

        setupViewPager()
        loadBookmarkStates()
    }

    private fun setupViewPager() {
        val points = mPoints ?: return
        val adapter = LitePointAdapter(points)
        mAdapter = adapter

        adapter.onBookmarkClick = { point, position ->
            toggleBookmark(point, position)
        }

        // 传递集数颜色映射给 Adapter
        adapter.setEpisodeColorMap(episodeColorMap)

        mBinding.viewPager.adapter = adapter
        mBinding.viewPager.offscreenPageLimit = 1
        mBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                highlightMarker(position)
            }
        })
        mBinding.viewPager.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.left = 50
                outRect.right = 50
            }
        })
        mBinding.viewPager.setPageTransformer { page, position ->
            val pageWidth = page.width
            // 减小 translationX 系数，从 0.25 降到 0.15，增加卡片间距
            page.translationX = -(pageWidth * 0.15f) * position
            page.scaleX = 1 - abs(position) * 0.15f
            page.scaleY = 1 - abs(position) * 0.15f
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        setupInfoWindowAdapter(googleMap)

        mPoints?.forEachIndexed { index, point ->
            val geo = point.geo
            if (geo.size == 2) {
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(geo[0], geo[1]))
                        .title(point.displayName())
                        .snippet(point.subjectName)
                        .icon(BitmapDescriptorFactory.defaultMarker(getMarkerHueByEpisode(point.ep)))
                        .alpha(0.6f)
                )
                marker?.let {
                    it.tag = index
                    mMarkers.add(it)
                    mMarkerImageMap[index] = point.image
                }
            }
        }

        highlightMarker(0)

        googleMap.setOnMarkerClickListener { marker ->
            val position = marker.tag as? Int
            position?.let {
                mBinding.viewPager.currentItem = it
            }
            true
        }
    }

    private fun setupInfoWindowAdapter(googleMap: GoogleMap) {
        val customInfoView = CustomInfoView(this)
        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? = null
            override fun getInfoContents(marker: Marker): View {
                val index = marker.tag as? Int
                val imageUrl = index?.let { mMarkerImageMap[it] }
                customInfoView.setMarker(marker, imageUrl)
                return customInfoView
            }
        })
    }

    private fun highlightMarker(position: Int) {
        mMarkers.forEach { it.alpha = 0.6f }
        if (position < mMarkers.size) {
            val marker = mMarkers[position]
            marker.alpha = 1.0f
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 15f))
            marker.showInfoWindow()
        }
    }

    private fun getMarkerHue(index: Int): Float {
        val hues = floatArrayOf(
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_AZURE,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_YELLOW,
            BitmapDescriptorFactory.HUE_ROSE,
            BitmapDescriptorFactory.HUE_BLUE,
        )
        return hues[index % hues.size]
    }

    /**
     * 初始化集数到颜色索引的映射
     * 收集所有不同的集数，按顺序分配颜色索引
     */
    private fun initEpisodeColorMap() {
        val points = mPoints ?: return

        // 收集所有不同的集数，使用 normalizeEpisode 统一格式
        val episodes = points.map { normalizeEpisode(it.ep) }.distinct()

        // 按集数排序：数字集数按数值排序，"其他"排最后
        val sortedEpisodes = episodes.sortedWith(compareBy {
            when {
                it == "其他" -> Int.MAX_VALUE
                it.matches("\\d+".toRegex()) -> it.toIntOrNull() ?: Int.MAX_VALUE
                else -> 1000 + it.hashCode()
            }
        })

        // 为每个集数分配颜色索引
        sortedEpisodes.forEachIndexed { index, episode ->
            episodeColorMap[episode] = index
        }
    }

    /**
     * 根据集数获取标记颜色
     */
    private fun getMarkerHueByEpisode(ep: String?): Float {
        val normalizedEp = normalizeEpisode(ep)
        val colorIndex = episodeColorMap[normalizedEp] ?: 0
        return getMarkerHue(colorIndex)
    }

    /**
     * 统一集数格式
     */
    private fun normalizeEpisode(ep: String?): String {
        return when {
            ep.isNullOrBlank() || ep == "null" -> "其他"
            ep.matches("\\d+".toRegex()) -> ep
            else -> ep
        }
    }

    private fun loadBookmarkStates() {
        val points = mPoints ?: return
        lifecycleScope.launch {
            points.forEachIndexed { index, point ->
                if (point.subjectId != 0) {
                    savedStates[index] = repository.isSaved(point.subjectId, point.id)
                }
            }
            mAdapter?.setBookmarkStates(savedStates)
        }
    }

    private fun toggleBookmark(point: LitePoint, position: Int) {
        if (point.subjectId == 0) return

        lifecycleScope.launch {
            try {
                val isSaved = savedStates[position] == true
                if (isSaved) {
                    val id = SavedPointEntity.generateId(point.subjectId, point.id)
                    repository.removePointById(id)
                    savedStates[position] = false
                    Toast.makeText(this@MapActivity, R.string.point_unsaved, Toast.LENGTH_SHORT).show()
                } else {
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
                    savedStates[position] = true
                    Toast.makeText(this@MapActivity, R.string.point_saved, Toast.LENGTH_SHORT).show()
                }
                mAdapter?.updateBookmarkState(position, savedStates[position] == true)
            } catch (e: Exception) {
                Toast.makeText(this@MapActivity, R.string.point_save_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

