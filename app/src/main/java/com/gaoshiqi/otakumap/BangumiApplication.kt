package com.gaoshiqi.otakumap

import android.app.Application
import com.gaoshiqi.room.AnimeMarkRepository

class BangumiApplication: Application() {
    lateinit var animeMarkRepository: AnimeMarkRepository

    override fun onCreate() {
        super.onCreate()
        animeMarkRepository = AnimeMarkRepository(this)
    }
}