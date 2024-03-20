package com.kkyoungs.simpleRecorder

import android.os.Handler
import android.os.Looper

class Timer(listener : OnTimerTickListener) {

    private var duration = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val runnable:Runnable = object  : Runnable {
        override fun run() {
            duration += 40L
            handler.postDelayed(this, 40L)
            listener.onTick(duration)
        }
    }

    fun start(){
        handler.postDelayed(runnable, 40L)
    }
    fun stop(pause: Boolean){
        if (!pause){
            duration = 0
        }
        handler.removeCallbacks(runnable)

    }

    fun pause (){
        handler.postDelayed(runnable, 100000000000000)
    }
}

interface OnTimerTickListener{
    fun onTick(duration : Long)
}