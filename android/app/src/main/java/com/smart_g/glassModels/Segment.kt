package com.smart_g.glassModels

import android.content.Context
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter.ImageSegmenterOptions
import org.tensorflow.lite.task.vision.segmenter.OutputType
import org.tensorflow.lite.task.vision.segmenter.Segmentation





class Segment(context: Context) {
  private var options = ImageSegmenterOptions.builder()
    .setBaseOptions(BaseOptions.builder().build())
    .setOutputType(OutputType.CONFIDENCE_MASK)
    .build()

  private var modelFile = "deeplabv3.tflite"

  private var imageSegmenter = ImageSegmenter.createFromFileAndOptions(context, modelFile, options)

  fun segmentFloor(tensorImage: TensorImage?) {
    val results: List<Segmentation> = imageSegmenter.segment(tensorImage)

    Log.d("TAG", "Here: ${results[0]}")

    return
  }
}