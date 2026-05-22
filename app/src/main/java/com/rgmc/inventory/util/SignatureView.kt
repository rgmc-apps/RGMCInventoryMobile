package com.rgmc.inventory.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.ByteArrayOutputStream

class SignatureView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paths = mutableListOf<Path>()
    private var currentPath = Path()
    private val paint = Paint().apply {
        color = Color.rgb(0, 0, 200)
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val bgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        for (path in paths) canvas.drawPath(path, paint)
        canvas.drawPath(currentPath, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { currentPath = Path(); currentPath.moveTo(event.x, event.y) }
            MotionEvent.ACTION_MOVE -> currentPath.lineTo(event.x, event.y)
            MotionEvent.ACTION_UP -> { paths.add(currentPath); currentPath = Path() }
        }
        invalidate()
        return true
    }

    fun clear() { paths.clear(); currentPath = Path(); invalidate() }

    fun getSignatureBytes(): ByteArray? {
        if (paths.isEmpty()) return null
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        for (path in paths) canvas.drawPath(path, paint)
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        return out.toByteArray()
    }
}
