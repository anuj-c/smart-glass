package com.smart_g.glassModels

import android.content.Context
import android.graphics.Bitmap


class Currency(val context: Context) {
  private val INPUT_SIZE = 224
  private val IMAGE_MEAN = 128
  private val IMAGE_STD = 128.0f
  private val INPUT_NAME = "input"
  private val OUTPUT_NAME = "final_result"

  private val MODEL_FILE = "file:///android_asset/graph.pb"
  private val LABEL_FILE = "file:///android_asset/labels.txt"
  private val classifier:Classifier =
  TensorFlowImageClassifier.create(
  context.assets,
  MODEL_FILE,
  LABEL_FILE,
  INPUT_SIZE,
  IMAGE_MEAN,
  IMAGE_STD,
  INPUT_NAME,
  OUTPUT_NAME);
  fun detectCurrency(bitmap: Bitmap) {
    println(bitmap)
    val results: List<Classifier.Recognition?>? = classifier.recognizeImage(bitmap)
    println(results)
    val currResult = if (results?.isNotEmpty() == true) {
      results[0]!!.getTitle1() + " Rupees"
    } else {
      "No Note Found"
    }
    println(currResult)
  }
}