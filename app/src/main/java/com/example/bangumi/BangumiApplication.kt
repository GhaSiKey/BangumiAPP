package com.example.bangumi

import android.app.Application
import com.example.room.AnimeMarkRepository

class BangumiApplication: Application() {
    lateinit var animeMarkRepository: AnimeMarkRepository

    override fun onCreate() {
        super.onCreate()
        animeMarkRepository = AnimeMarkRepository(this)
    }
}