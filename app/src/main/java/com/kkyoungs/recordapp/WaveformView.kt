package com.kkyoungs.recordapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.time.Duration

class WaveformView @JvmOverloads constructor(
    context : Context,
    attrs: AttributeSet?= null,
    defStyleAttr : Int = 0
) : View(context, attrs, defStyleAttr) {
    private val rectWidth = 15f

    private val ampList = mutableListOf<Float>()
    private val rectList = mutableListOf<RectF>()

    private var tick = 0
    private val redPaint = Paint().apply{
        color = Color.YELLOW
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for(rectF in rectList){
            canvas.drawRect(rectF, redPaint)
        }
    }

    fun addAmplitude(maxAmplitude : Float){
        val amplitude = (maxAmplitude / Short.MAX_VALUE) * this.height * 0.8f

        ampList.add(amplitude)
        rectList.clear()

        val maxRect = (this.width/rectWidth).toInt()
        val amps = ampList.takeLast(maxRect)
        for ((i,amp) in amps.withIndex()){
            val rectF = RectF()
            rectF.top = (this.height / 2) - amp / 2 -3f
            rectF.bottom = rectF.top + amp +3f

            rectF.left = i * rectWidth
            rectF.right = rectF.left + rectWidth-5f
            rectList.add(rectF)
        }
        // ondraw 다시 부르는 함수
        invalidate()
    }

    fun replayAmplitude(){
        rectList.clear()

        val maxRect = (this.width/rectWidth).toInt()
        val amps = ampList.take(tick).takeLast(maxRect)
        for ((i,amp) in amps.withIndex()){
            val rectF = RectF()
            rectF.top = (this.height / 2) - amp / 2 -3f
            rectF.bottom = rectF.top + amp+3f
            rectF.left = i * rectWidth
            rectF.right = rectF.left + rectWidth -5f  // 여백을 위해 5 더줌

            rectList.add(rectF)
        }
        tick ++
        invalidate()

    }

    fun clearData(){
        ampList.clear()
    }
    fun clearWave(){
        rectList.clear()
        tick = 0
        invalidate()
    }
}