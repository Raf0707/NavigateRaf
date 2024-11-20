package com.example.navigateraf


import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.navigateraf.util.SharedPreferencesUtils

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        setNightMode()
    }

    fun setNightMode() {
        val nightMode: Int = SharedPreferencesUtils.getInteger(this, "nightMode", 1)
        //val nightIcon: Int =
            //SharedPreferencesUtils.getInteger(this, "nightIcon", R.drawable.vectornightpress)
        val mode = intArrayOf(
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
        )

        AppCompatDelegate.setDefaultNightMode(mode[nightMode])
    }

    companion object {
        var instance: App? = null

        fun getInstance(): App? {
            if (instance == null) instance = App()
            return instance
        }
    }
}