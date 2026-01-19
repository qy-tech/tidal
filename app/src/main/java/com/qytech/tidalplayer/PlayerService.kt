package com.qytech.tidalplayer

import android.app.Service
import android.content.Intent
import android.os.IBinder

class PlayerService : Service() {

    private val binder = object : IPlayerInterface.Stub() {
        override fun launch(appName: String?): Boolean {
            val context = this@PlayerService
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(intent)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }
}