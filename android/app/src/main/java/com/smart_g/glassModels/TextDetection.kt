package com.smart_g.glassModels

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextDetection(context: Context) {
  private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

  fun detect(context: Context, image: InputImage, callback: (Text) -> Unit) {
    recognizer.process(image)
      .addOnSuccessListener { visionText ->
        callback(visionText)
      }
      .addOnFailureListener { e ->
        Log.d("TAG", "Error while detecting text: $e")
      }
  }
}