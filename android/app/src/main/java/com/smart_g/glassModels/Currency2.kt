package com.smart_g.glassModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import com.google.mlkit.vision.common.InputImage

class Currency2(val context: Context) {
  private val matrix = Matrix().apply {
    postRotate(90f)
  }
  private val revCurrencyMap = hashMapOf(0 to "10", 1 to "20", 2 to "50", 3 to "100", 4 to "200", 5 to "500", 6 to "2000")
  private val currencyMap = hashMapOf("10" to 0,"20" to 1,"50" to 2,"100" to 3, "200" to 4, "500" to 5,"2000" to 6)
  private val textDetector = TextDetection(context)

  fun detectCurrency(uri: String, callback: (String) -> Unit){
    val imageUri = Uri.parse(uri)
    val originalBitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
    val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
    val rotatedInputImage = InputImage.fromBitmap(rotatedBitmap, 0)
    textDetector.detect(rotatedInputImage) {results ->
      val statusArray = Array(7) { false }
      var found = false
      val currencyPosition = mutableListOf<Rect?>()

      for(blocks in results.textBlocks){
        for(lines in blocks.lines){
          for(words in lines.elements){
            if(currencyMap.containsKey(words.text)){
              found = true
              statusArray[currencyMap[words.text]!!] = true
              currencyPosition.add(words.boundingBox)
            }
          }
        }
      }
      var strToSpeak = ""
      for(i in 0..6){
        if(statusArray[i])
          strToSpeak += "Found bill of ${revCurrencyMap[i]} rupees."
      }

      if(!found)
        strToSpeak += "No bills found"
      callback(strToSpeak)
    }
  }
}