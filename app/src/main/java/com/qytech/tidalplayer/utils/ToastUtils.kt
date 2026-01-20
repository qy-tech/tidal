package com.qytech.tidalplayer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.qytech.tidalplayer.R

@SuppressLint("StaticFieldLeak")
object ToastUtils {

    @Volatile
    private var toast: Toast? = null

    fun init(appContext: Context) {
        // 强制使用 applicationContext，防止误传
        context = appContext.applicationContext
    }

    private lateinit var context: Context

    @SuppressLint("StaticFieldLeak")
    fun show(
        message: String,
        duration: Int = Toast.LENGTH_SHORT,
        gravity: Int = Gravity.BOTTOM,
        yOffset: Int = 50
    ) {
        check(::context.isInitialized) {
            "ToastUtils not initialized. Call ToastUtils.init(applicationContext) first."
        }

        runOnMainThread {
            toast?.cancel()

            val view = LayoutInflater.from(context)
                .inflate(R.layout.layout_toast, null)

            view.findViewById<TextView>(R.id.message).text = message

            toast = Toast(context).apply {
                this.view = view
                this.duration = duration
                setGravity(gravity, 0, yOffset)
                show()
            }
        }
    }

    private fun runOnMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            Handler(Looper.getMainLooper()).post(block)
        }
    }
}
