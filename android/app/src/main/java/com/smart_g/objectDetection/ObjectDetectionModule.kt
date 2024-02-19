package com.smart_g.objectDetection

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import org.tensorflow.lite.support.image.TensorImage

class ObjectDetectionModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  override fun getName(): String {
    return "ObjectDetection"
  }

  private val matrix = Matrix().apply {
    postRotate(90f)
  }

  private var image: Bitmap? = null
  private var tensorImage: TensorImage? = null
  private val detector = Detector(reactApplicationContext)

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun detectObjects(uri: String, promise: Promise) {
    try {
      val imageUri = Uri.parse(uri)
      val source = ImageDecoder.createSource(reactApplicationContext.contentResolver, imageUri)
      image = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
        decoder.setTargetSize(480, 640)
        decoder.setTargetColorSpace(android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB))
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        decoder.setOnPartialImageListener { _ -> true }
      }

//      Log.d("TAG", "${image!!.width}, ${image!!.height}")

      image = if (image!!.config == Bitmap.Config.ARGB_8888) {
        image
      } else {
        image!!.copy(Bitmap.Config.ARGB_8888, true)
      }

      image = image?.let { Bitmap.createBitmap(it, 0, 0, image!!.width, image!!.height, matrix, true) }
      tensorImage = TensorImage.fromBitmap(image)


      val results = detector.detectObjects(tensorImage)
//      Log.d("TAG", "Here: $results")

      val data = mutableListOf<String>()
      for (result in results) {
        val cates = result.categories
        for (cats in cates) {
          val label = cats.label
          data.add(label)
        }
      }

      val ret = Arguments.createArray()
      for (label in data) {
        ret.pushString(label)
      }

      image?.recycle()
      image = null

      tensorImage?.bitmap?.recycle()
      tensorImage = null

//      Log.d("TAG", "Returning")
      promise.resolve(ret)
    } catch (e: Exception) {
      Log.e("TAG", "Error in detecting objects", e)
      promise.reject(e)
    }
  }
}
