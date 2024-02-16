package com.smart_g.objectDetection

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class Detector<Detection>(private val context: Context) {
  // Initialization
  private var options: ObjectDetector.ObjectDetectorOptions = ObjectDetector.ObjectDetectorOptions.builder()
    .setBaseOptions(BaseOptions.builder().useGpu().build())
    .setMaxResults(1)
    .build()

  private var modelFile = "mobilenetssd.tflite"
  private var detector: ObjectDetector = ObjectDetector.createFromFileAndOptions(
    context, modelFile, options
  )

//  private fun addBoundingBoxAndSaveImage(context: Context, imageUri: Uri, box: RectF): Uri? {
//    val bitmap = if (Build.VERSION.SDK_INT < 28) {
//      MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
//    } else {
//      val source = ImageDecoder.createSource(context.contentResolver, imageUri)
//      ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
//        decoder.setTargetColorSpace(android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB))
//        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
//        decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
//        decoder.setOnPartialImageListener { _ -> true }
//      }
//    }
//    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//    val canvas = Canvas(mutableBitmap)
//
//    val paint = Paint().apply {
//      color = Color.RED
//      style = Paint.Style.STROKE
//      strokeWidth = 10f
//    }
//
//    canvas.drawRect(box, paint)
//    return saveImageToGallery(context, mutableBitmap)
//  }
//
  private fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri? {
    val values = ContentValues().apply {
      put(MediaStore.Images.Media.TITLE, "Image_with_BoundingBox")
      put(MediaStore.Images.Media.DISPLAY_NAME, "Image_with_BoundingBox.jpg")
      put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    uri?.let {
      context.contentResolver.openOutputStream(it).use { out ->
        if (out != null) {
          bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
      }
    }

    return uri
  }

  private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix().apply {
      postRotate(angle)
    }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
  }

  suspend fun detectObjects(uri: String): MutableList<org.tensorflow.lite.task.vision.detector.Detection> = withContext(Dispatchers.IO) {
    withTimeoutOrNull(2000) {
      Log.d("TAG", "In detectObjects")
      val imageUri = Uri.parse(uri)
      val image = if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
      } else {
        val source = ImageDecoder.createSource(context.contentResolver, imageUri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
          decoder.setTargetColorSpace(android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB))
          decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
          decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
          decoder.setOnPartialImageListener { _ -> true }
        }
      }

      Log.d("TAG", "${image.width}, ${image.height}")

      val argbImage = if (image.config == Bitmap.Config.ARGB_8888) {
        image
      } else {
        image.copy(Bitmap.Config.ARGB_8888, true)
      }

      val rotatedImage = rotateBitmap(argbImage, 90f)
      val savedUri = saveImageToGallery(context, rotatedImage)

      Log.d("TAG", "saved uri: $savedUri")

      val tensorImage = TensorImage.fromBitmap(rotatedImage)

      val results = detector.detect(tensorImage)

      Log.d("TAG", "res: $results")

      results ?: mutableListOf<org.tensorflow.lite.task.vision.detector.Detection>()
    } ?: run {
      mutableListOf<org.tensorflow.lite.task.vision.detector.Detection>()
    }
  }
}
//class Detector(private val context: Context) {
//  private val localModel = LocalModel.Builder()
//    .setAssetFilePath("1meta.tflite")
//    .build()
//
//  private val customObjectDetectorOptions =
//    CustomObjectDetectorOptions.Builder(localModel)
//      .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
//      .enableMultipleObjects()
//      .enableClassification()
//      .setClassificationConfidenceThreshold(0.5f)
//      .setMaxPerObjectLabelCount(3)
//      .build()
//
//  private val objectDetector =
//    ObjectDetection.getClient(customObjectDetectorOptions)
//
//  fun detectObjects(uri: String, onSuccess: (List<DetectedObject>) -> Unit, onFailure: (Exception) -> Unit) {
//    Log.d("TAG", uri)
//    Log.d("TAG", "$localModel")
//    Log.d("TAG", "$customObjectDetectorOptions")
//    Log.d("TAG", "$objectDetector")
//
//    val image: InputImage = InputImage.fromFilePath(context, android.net.Uri.parse(uri))
//
//    objectDetector.process(image)
//      .addOnSuccessListener(OnSuccessListener { detectedObjects ->
//        Log.d("TAG", "Detected something: $detectedObjects")
//        onSuccess(detectedObjects)
//      })
//      .addOnFailureListener(OnFailureListener { e ->
//        Log.d("TAG", "$e")
//        onFailure(e)
//      })
//  }
//}