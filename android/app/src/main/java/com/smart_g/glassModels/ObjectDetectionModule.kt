package com.smart_g.glassModels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.mlkit.vision.common.InputImage
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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
  private val segmentor = Segment(reactApplicationContext)
  private val textDetector = TextDetection(reactApplicationContext)

  @ReactMethod
  fun saveImageFromUri(uri: String, outputFileName: String) {
    val imageUri = Uri.parse(uri)
    val context = reactApplicationContext
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
    val outputDir = context.getExternalFilesDir(null) // Change this to your desired output directory
    val outputFile = File(outputDir, outputFileName)

    inputStream.use { input ->
      FileOutputStream(outputFile).use { output ->
        val buffer = ByteArray(4 * 1024) // buffer size
        var read: Int
        while (input?.read(buffer).also { read = it ?: -1 } != -1) {
          output.write(buffer, 0, read)
        }
        output.flush()
      }
    }
  }

  // Utility function to directly return tensor image from uri in string format
  @RequiresApi(Build.VERSION_CODES.P)
  private fun uriToTensor(uri: String): TensorImage? {
    val imageUri = Uri.parse(uri)
    val source = ImageDecoder.createSource(reactApplicationContext.contentResolver, imageUri)
    image = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
      decoder.setTargetSize(480, 640)
      decoder.setTargetColorSpace(android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB))
      decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
      decoder.setOnPartialImageListener { _ -> true }
    }

    image = if (image!!.config == Bitmap.Config.ARGB_8888) {
      image
    } else {
      image!!.copy(Bitmap.Config.ARGB_8888, true)
    }

    image = image?.let { Bitmap.createBitmap(it, 0, 0, image!!.width, image!!.height, matrix, true) }
    return TensorImage.fromBitmap(image)
  }

  // Detect Objects function
  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun detectObjects(uri: String, promise: Promise) {
    try {
      tensorImage = uriToTensor(uri)

      val results = detector.detectObjects(tensorImage)

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

      promise.resolve(ret)
    } catch (e: Exception) {
      Log.e("TAG", "Error in detecting objects", e)
      promise.reject(e)
    }
  }

  // Floor Segmentation function which currently is not running because of model issues
  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun segmentFloor(uri: String, promise: Promise) {
    try{
      tensorImage = uriToTensor(uri)

      val results = segmentor.segmentFloor(tensorImage)

    }catch(e: Exception) {
      Log.e("TAG", "Error in detecting objects", e)
      promise.reject(e)
    }
  }

  // Text Detection function which takes in uri in string format
  @ReactMethod
  fun detectText(uri: String, promise: Promise) {
    try {
      val imageUri = Uri.parse(uri)
      val originalBitmap = BitmapFactory.decodeStream(reactApplicationContext.contentResolver.openInputStream(imageUri))
      val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
      val rotatedInputImage = InputImage.fromBitmap(rotatedBitmap, 0)
      val results = textDetector.detect(reactApplicationContext, rotatedInputImage)

      Log.d("TAG", "$results")
      
      promise.resolve("Done")
    }catch(e: Exception) {
      promise.reject(e)
    }
  }
}
