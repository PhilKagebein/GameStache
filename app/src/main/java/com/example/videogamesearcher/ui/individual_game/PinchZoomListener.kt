package com.example.videogamesearcher.ui.individual_game

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.core.view.ViewCompat

class PinchZoomListener(private val art: ImageView?): ScaleGestureDetector.SimpleOnScaleGestureListener() {

    private val mCurrentViewport = RectF(0.0F, 0.0F, 10.0F, 10.0F)
    private val mContentRect: Rect? = null

    private var scale = 1.0f
    var matrix = Matrix()

    private val viewportFocus = PointF()
    private var lastSpanX: Float = 0f
    private var lastSpanY: Float = 0f

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        val spanX: Float = detector?.currentSpanX!!
        val spanY: Float = detector.currentSpanY

        val newWidth: Float = lastSpanX / spanX * mCurrentViewport.width()
        val newHeight: Float = lastSpanY / spanY * mCurrentViewport.height()

        val focusX: Float = detector.focusX
        val focusY: Float = detector.focusY

        mContentRect?.apply {
            mCurrentViewport.set(
                viewportFocus.x - newWidth * (focusX - left) / width(),
                viewportFocus.y - newHeight * (bottom - focusY) / height(),
                0f,
                0f
            )
        }
        mCurrentViewport.right = mCurrentViewport.left + newWidth
        mCurrentViewport.bottom = mCurrentViewport.top + newHeight
        // Invalidates the View to update the display.
        if (art != null) {
            ViewCompat.postInvalidateOnAnimation(art)
        }

        lastSpanX = spanX
        lastSpanY = spanY

/*        val gestureFactor = detector.scaleFactor
        scale *= gestureFactor
        scale = Math.max(0.1f, Math.min(scale, 5.0f))
        matrix.setScale(scale, scale)
        art?.imageMatrix = matrix*/
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        lastSpanX = detector?.currentSpanX!!
        lastSpanY = detector.currentSpanY
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        super.onScaleEnd(detector)
    }


}