package com.kzeta.presionmtb

import android.app.Application
import com.kzeta.presionmtb.data.AppDatabase
import com.kzeta.presionmtb.data.LibraryRepository
import com.kzeta.presionmtb.data.TrailConditions

class PresionApp : Application() {
    lateinit var library: LibraryRepository
        private set
    lateinit var conditions: TrailConditions
        private set

    override fun onCreate() {
        super.onCreate()
        library = LibraryRepository(this, AppDatabase.create(this))
        conditions = TrailConditions(this)
    }
}
