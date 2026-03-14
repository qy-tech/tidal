package com.qytech.tidalplayer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.qytech.hifiplayer.submit.IPlayerSubmit
import timber.log.Timber

class PlayerService : Service() {

    private val binder = object : IPlayerSubmit.Stub() {
        override fun launch(packageName: String?): Boolean {
            val context = this@PlayerService
            val myPackage = context.packageName
            if (packageName == myPackage) {
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    startActivity(intent)
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                    Timber.d("报错：${e.message}")
                    return false
                }
            } else{
                Timber.d("packageName不同：$packageName")
                return false
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }
}