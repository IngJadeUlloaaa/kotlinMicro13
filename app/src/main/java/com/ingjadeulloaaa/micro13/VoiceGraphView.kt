// VoiceGraphView.kt
package com.ingjadeulloaaa.micro13

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class VoiceGraphView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint: Paint = Paint()
    private val dataPoints: MutableList<Float> = mutableListOf()

    init {
        paint.color = Color.BLUE
        paint.strokeWidth = 5f
        paint.isAntiAlias = true
    }

    fun addDataPoint(value: Float) {
        dataPoints.add(value)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dataPoints.size < 2) {
            return
        }

        val width = width.toFloat()
        val height = height.toFloat()
        val deltaX = width / (dataPoints.size - 1)

        for (i in 1 until dataPoints.size) {
            val startX = (i - 1) * deltaX
            val startY = height - dataPoints[i - 1] / 100 * height

            val endX = i * deltaX
            val endY = height - dataPoints[i] / 100 * height

            canvas.drawLine(startX, startY, endX, endY, paint)
        }
    }

    fun clearData() {
        dataPoints.clear()
        invalidate()
    }
}
