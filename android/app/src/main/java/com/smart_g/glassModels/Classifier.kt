package com.smart_g.glassModels

import android.graphics.Bitmap
import android.graphics.RectF

interface Classifier {
  class Recognition(
    val id: String?,
    val title: String?, confidence: Float?, location: RectF?
  ) {
    val confidence: Float?
    private var location: RectF?
    private lateinit var title1: String

    init {
      this.confidence = confidence
      this.location = location
      if (title != null) {
        this.title1 = title
      }
    }

    fun getLocation(): RectF {
      return RectF(location)
    }

    fun getTitle1(): String {
      return title1
    }

    fun setLocation(location: RectF?) {
      this.location = location
    }

    override fun toString(): String {
      var resultString = ""
      if (id != null) {
        resultString += "[$id] "
      }
      if (title != null) {
        resultString += "$title "
      }
      if (confidence != null) {
        resultString += String.format("(%.1f%%) ", confidence * 100.0f)
      }
      if (location != null) {
        resultString += location.toString() + " "
      }
      return resultString.trim { it <= ' ' }
    }
  }

  fun recognizeImage(bitmap: Bitmap?): List<Recognition?>?
  fun enableStatLogging(debug: Boolean)
  val statString: String?

  fun close()
}
