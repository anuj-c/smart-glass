package com.smart_g.objectDetection

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class Detector(context: Context) {
  // Initialization
  private var options: ObjectDetector.ObjectDetectorOptions = ObjectDetector.ObjectDetectorOptions.builder()
    .setBaseOptions(BaseOptions.builder().build())
    .setMaxResults(5)
    .setScoreThreshold(0.4f)
    .build()

  private var modelFile = "model.tflite"
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
  fun detectObjects(tensorImage: TensorImage?): MutableList<org.tensorflow.lite.task.vision.detector.Detection> {
//    if (isMemoryAvailable(context)) {
    val results = detector.detect(tensorImage)

    tensorImage?.bitmap?.recycle()

    return results ?: mutableListOf<org.tensorflow.lite.task.vision.detector.Detection>()
//    }

//    return mutableListOf<org.tensorflow.lite.task.vision.detector.Detection>()
  }
}