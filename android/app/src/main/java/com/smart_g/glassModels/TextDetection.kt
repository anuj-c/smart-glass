package com.smart_g.glassModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class TextDetection(val context: Context) {
  private val matrix = Matrix().apply {
    postRotate(90f)
  }
  private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

  fun detect(image: InputImage, callback: (Text) -> Unit) {
    recognizer.process(image)
      .addOnSuccessListener { visionText ->
        callback(visionText)
      }
      .addOnFailureListener { e ->
        Log.d("TAG", "Error while detecting text: $e")
      }
  }

  fun detectLargestText(uri: String, callback: (String) -> Unit) {
    try {
      val imageUri = Uri.parse(uri)
      val originalBitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
      val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
      val rotatedInputImage = InputImage.fromBitmap(rotatedBitmap, 0)
      detect(rotatedInputImage) {resText ->
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

  fun detectExpiry(image: InputImage, callback: (String) -> Unit) {
    val datePattern = "\\b(0[1-9]|1[0-2])[/\\-.](202[0-9]|\\d{2})\\b"
    val regex = Regex(datePattern)
    val matches = mutableListOf<String>()

    detect(image) {resText ->
      for (block in resText.textBlocks) {
        for (line in block.lines) {
          println(line.text)
          val result = regex.findAll(line.text)
          result.forEach{
            println(it.value)
            matches.add(it.value)
          }
        }
      }
      println(matches)
      var strToSpeak = ""
      matches.forEach{
        strToSpeak += "$it, "
      }
      callback(strToSpeak)
    }
  }
}