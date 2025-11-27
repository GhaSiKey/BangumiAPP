package com.gaoshiqi.otakumap

import android.app.Application
import com.gaoshiqi.room.AnimeMarkRepository
import com.gaoshiqi.room.SavedPointRepository

class BangumiApplication: Application() {
    lateinit var animeMarkRepository: AnimeMarkRepository
    lateinit var savedPointRepository: SavedPointRepository

    override fun onCreate() {
        super.onCreate()
        animeMarkRepository = AnimeMarkRepository(this)
        savedPointRepository = SavedPointRepository(this)
    }
}