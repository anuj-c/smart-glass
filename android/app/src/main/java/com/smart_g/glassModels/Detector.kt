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

  fun describeObjects(map: Map<String, Int>): String {
    val parts = map.map { (key, value) ->
      "$value ${if (value == 1) key else "${key}s"}"
    }

    return parts.joinToString(", ")
  }

}