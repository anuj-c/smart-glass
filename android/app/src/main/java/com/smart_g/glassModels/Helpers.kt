package com.smart_g.glassModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.mlkit.vision.common.InputImage
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream

class Helpers (val context: Context){
  private val matrix = Matrix().apply {
    postRotate(90f)
  }
  private var image: Bitmap? = null
  private val textDetector = TextDetection(context)
  @RequiresApi(Build.VERSION_CODES.P)
  fun uriToTensor(uri: String): TensorImage? {
    val imageUri = Uri.parse(uri)
    val source = ImageDecoder.createSource(context.contentResolver, imageUri)
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
  private fun saveBitmapToFile(bitmap: Bitmap?, filename: String = "myimage.jpg"): String {
    if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
      return ""
    }

    val picturesDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File(picturesDirectory, filename)

    var fileOutputStream: FileOutputStream? = null
    try {
      fileOutputStream = FileOutputStream(imageFile)
      bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
      fileOutputStream.flush()
      println("file://$picturesDirectory/$filename")
      return "file://$picturesDirectory/$filename"
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      try {
        fileOutputStream?.close()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    return ""
  }

  fun getTensorImageFromVideoUri(uri: Uri, frameTime: Long): TensorImage? {
    val retriever = MediaMetadataRetriever()
    return try {
      retriever.setDataSource(context, uri)
      val bitmap = retriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
//      return saveBitmapToFile(bitmap)
      return bitmap?.let { convertBitmapToTensorImage(it) }
    } catch (e: IllegalArgumentException) {
      e.printStackTrace()
      null
    } finally {
      retriever.release()
    }
  }

  fun filterByThreshold(listOfMaps: List<Map<String, Int>>, minOccurrences: Int = 1): Map<String, Int> {
    val keyValues = mutableMapOf<String, MutableList<Int>>()
    listOfMaps.forEach { map ->
      map.forEach { (key, value) ->
        keyValues.getOrPut(key) { mutableListOf() }.add(value)
      }
    }
    return keyValues.mapValues { (_, values) ->
      values.sortedDescending().let {
        if (it.size >= minOccurrences) it[minOccurrences - 1] else null
      }
    }
      .filter { (key, threshold) ->
        threshold != null && keyValues[key]!!.count { value -> value >= threshold } >= minOccurrences
      }
      .mapValues { it.value!! }
  }

  fun cropBitmap(original: Bitmap, cropBounds: Rect): Bitmap {
    val width = cropBounds.width().coerceAtMost(original.width - cropBounds.left)
    val height = cropBounds.height().coerceAtMost(original.height - cropBounds.top)

    if (width <= 0 || height <= 0) {
      throw IllegalArgumentException("Crop bounds are outside of the dimensions of the original Bitmap.")
    }

    return Bitmap.createBitmap(original, cropBounds.left, cropBounds.top, width, height)
  }

  private fun convertBitmapToTensorImage(bitmap: Bitmap): TensorImage {
    val compatibleBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
      bitmap.copy(Bitmap.Config.ARGB_8888, true)
    } else {
      bitmap
    }

    val targetWidth = 480
    val targetHeight = 640
    val scaleWidth = targetWidth.toFloat() / compatibleBitmap.width
    val scaleHeight = targetHeight.toFloat() / compatibleBitmap.height
    val matrix = Matrix().apply {
      postScale(scaleWidth, scaleHeight)
      postRotate(90f)
    }
    val resizedBitmap = Bitmap.createBitmap(compatibleBitmap, 0, 0, compatibleBitmap.width, compatibleBitmap.height, matrix, true)

    return TensorImage.fromBitmap(resizedBitmap)
  }

  fun describeObjects(map: Map<String, Int>): String {
    val parts = map.map { (key, value) ->
      "$value ${if (value == 1) key else "${key}s"}"
    }

    return "Objects found are ${parts.joinToString(", ")}"
  }

  fun drawBoxAndSaveImage(bitmap: Bitmap, faceBox: Rect?, imageName: String): String? {
    if(faceBox == null) return null
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
      color = Color.RED
      style = Paint.Style.STROKE
      strokeWidth = 1f
    }

    canvas.drawRect(faceBox, paint)

    return try {
      saveBitmapToFile(mutableBitmap, imageName)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun detectLargestText(uri: String, callback: (String) -> Unit) {
    try {
      val imageUri = Uri.parse(uri)
      val originalBitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
      val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
      val rotatedInputImage = InputImage.fromBitmap(rotatedBitmap, 0)
      textDetector.detect(rotatedInputImage) {resText ->
        val largestTextBlock = resText.textBlocks.maxByOrNull { block ->
          val firstLine = block.lines[0]
          firstLine.boundingBox?.height()?.times(firstLine.boundingBox!!.width()) ?:0
        }

        val largestText = largestTextBlock?.text ?: "Text not found"
        callback(largestText)
      }
    }catch(e: Exception) {
      val str = "Error while detecting large text: $e"
      Log.e("TAG", str)
      callback(str)
    }
  }

  fun rectF2Rect(bBox1: RectF?): Rect? {
    if (bBox1 == null) return null
    return Rect(
      bBox1.left.toInt(), bBox1.top.toInt(), bBox1.right.toInt(),
      bBox1.bottom.toInt()
    )
  }
}