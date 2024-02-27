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

  fun detect(context: Context, image: InputImage): Task<Text> {
    val result = recognizer.process(image)
      .addOnSuccessListener { visionText ->
        Log.d("TAG", "Inside function: $visionText")
        val resultText = visionText.text
        Log.d("TAG", "Inside function: $resultText")
//        for (block in visionText.textBlocks) {
//          val blockText = block.text
//          for (line in block.lines) {
//            val lineText = line.text
//            val lineCornerPoints = line.cornerPoints
//            val lineFrame = line.boundingBox
//            for (element in line.elements) {
//              val elementText = element.text
//              val elementCornerPoints = element.cornerPoints
//              val elementFrame = element.boundingBox
//            }
//          }
//        }
      }
      .addOnFailureListener { e ->
        Log.d("TAG", "Error while detecting text: $e")
      }

    Log.d("TAG", "$result")

    return result
  }
}