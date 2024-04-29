package com.smart_g.glassModels

import android.app.ActivityManager
import android.content.Context
import android.graphics.RectF
import android.os.Build
import androidx.annotation.RequiresApi
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.*

class Detector(val context: Context) {
  // Initialization
  private var options: ObjectDetector.ObjectDetectorOptions = ObjectDetector.ObjectDetectorOptions.builder()
    .setBaseOptions(BaseOptions.builder().build())
    .setScoreThreshold(0.5f)
    .setNumThreads(4)
    .build()

  private var modelFile = "ssdmobilenetv1.tflite"
  private var detector: ObjectDetector = ObjectDetector.createFromFileAndOptions(
    context, modelFile, options
  )

  private fun isMemoryAvailable(context: Context): Boolean {
    val requiredMemoryInMB = 450L
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val availableMemoryInBytes = memoryInfo.availMem

//    Log.d("TAG", "Memory available: ${availableMemoryInBytes / (1024 * 1024)}")

    val requiredMemoryInBytes = requiredMemoryInMB * 1024 * 1024
    return availableMemoryInBytes >= requiredMemoryInBytes
  }

  @RequiresApi(Build.VERSION_CODES.P)
  fun detectObjects(tensorImage: TensorImage?): MutableList<Detection> {
    val results = detector.detect(tensorImage)
    tensorImage?.bitmap?.recycle()

    return results ?: mutableListOf<Detection>()
  }

  fun detectObjects2(tensorImage: TensorImage?, objects: List<String>): MutableList<Detection> {
    val options2: ObjectDetector.ObjectDetectorOptions = ObjectDetector.ObjectDetectorOptions.builder()
      .setBaseOptions(BaseOptions.builder().build())
      .setScoreThreshold(0.5f)
      .setLabelAllowList(objects)
      .build()
    val detector2: ObjectDetector = ObjectDetector.createFromFileAndOptions(
      context, modelFile, options2
    )


    val results = detector2.detect(tensorImage)
    tensorImage?.bitmap?.recycle()

    return results ?: mutableListOf<Detection>()
  }

  fun getObjectPosition(boundingBox: RectF): String {
    val centerX = boundingBox.centerX()
    val centerY = boundingBox.centerY()

    val horizontalThird = 480 / 3
    val verticalThird = 640 / 3

    val horizontalPosition = when {
      centerX < horizontalThird -> "left"
      centerX > 2 * horizontalThird -> "right"
      else -> "center"
    }

    val verticalPosition = when {
      centerY < verticalThird -> "top"
      centerY > 2 * verticalThird -> "bottom"
      else -> "middle"
    }

    return "$verticalPosition-$horizontalPosition"
  }
}