package com.smart_g

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableNativeMap
import com.google.mlkit.vision.common.InputImage
import com.smart_g.glassModels.Audio
import com.smart_g.glassModels.Database
import com.smart_g.glassModels.Detector
import com.smart_g.glassModels.FaceDB
import com.smart_g.glassModels.FaceData
import com.smart_g.glassModels.Helpers
import com.smart_g.glassModels.TextDetection
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ReactNativeModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  override fun getName(): String {
    return "ObjectDetection"
  }

  private val detector = Detector(reactApplicationContext)
  private val textDetector = TextDetection(reactApplicationContext)
  private val faceDetector = FaceData(reactApplicationContext)
  private val audioClass = Audio(reactApplicationContext)
  private val databaseUtil = Database(reactApplicationContext, "face.db", 1)
  private val helper = Helpers(reactApplicationContext)

  private val matrix = Matrix().apply {
    postRotate(90f)
  }

  private var image: Bitmap? = null
  private var tensorImage: TensorImage? = null

  @ReactMethod
  fun saveImageFromUri(uri: String, outputFileName: String) {
    val imageUri = Uri.parse(uri)
    val context = reactApplicationContext
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
    val outputDir = context.getExternalFilesDir(null) // Change this to your desired output directory
    val outputFile = File(outputDir, outputFileName)

    inputStream.use { input ->
      FileOutputStream(outputFile).use { output ->
        val buffer = ByteArray(4 * 1024) // buffer size
        var read: Int
        while (input?.read(buffer).also { read = it ?: -1 } != -1) {
          output.write(buffer, 0, read)
        }
        output.flush()
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun detectObjects(uri: String, promise: Promise) {
    try {
      tensorImage = helper.uriToTensor(uri)

      val results = detector.detectObjects(tensorImage)

      val data = mutableMapOf<String, Int>()
      for (result in results) {
        val cates = result.categories
        for (cats in cates) {
          val label = cats.label
          if (data.containsKey(label)) {
            data[label] = data[label]!! + 1
          } else {
            data[label] = 1
          }
        }
      }

      val strToSpeak = helper.describeObjects(data)
      audioClass.speakText(strToSpeak)

      val ret = Arguments.createArray()
      for ((key, value) in data) {
        ret.pushString("$key-$value")
      }

      image?.recycle()
      image = null

      tensorImage?.bitmap?.recycle()
      tensorImage = null

      promise.resolve(ret)
    } catch (e: Exception) {
      Log.e("TAG", "Error in detecting objects", e)
      promise.reject(e)
    }
  }

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun detectObjectFromRecording(uri: String, promise: Promise) {
    try {
      val videoUri = Uri.parse(uri)
      val dataArr = mutableListOf<Map<String, Int>>()
      for(i in 1..10){
        val frameTime = i*200000L
        tensorImage = helper.getTensorImageFromVideoUri(videoUri, frameTime)

//        tensorImage = helper.uriToTensor(tensorUri)
        val results = detector.detectObjects(tensorImage)
        val data = mutableMapOf<String, Int>()
        for (result in results) {
          val cates = result.categories
          for (cats in cates) {
            val label = cats.label
            println(label)
            if (data.containsKey(label)) {
              data[label] = data[label]!! + 1
            } else {
              data[label] = 1
            }
          }
        }

        dataArr.add(data)
      }
      val processedObjects = helper.filterByThreshold(dataArr)
      val strToSpeak = helper.describeObjects(processedObjects)
      audioClass.speakText(strToSpeak)
      val retArr = Arguments.createArray()

      for ((key, value) in processedObjects) {
        retArr.pushString("$key-$value")
      }

      promise.resolve(retArr)
    }catch(e: Exception) {
      Log.e("TAG", "Error in detecting object from video")
    }
  }

  // Text Detection function which takes in uri in string format
  @ReactMethod
  fun detectText(uri: String, promise: Promise) {
    try {
      val imageUri = Uri.parse(uri)
      val originalBitmap = BitmapFactory.decodeStream(reactApplicationContext.contentResolver.openInputStream(imageUri))
      val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
      val rotatedInputImage = InputImage.fromBitmap(rotatedBitmap, 0)
      textDetector.detect(rotatedInputImage) {resText ->
        val resultText = resText.text
        for ((it1, block) in resText.textBlocks.withIndex()) {
          val blockText = block.text
          val blockCornerPoints = block.cornerPoints
          val blockFrame = block.boundingBox
          helper.drawBoxAndSaveImage(rotatedBitmap, blockFrame, "myimage$it1.jpg")
          for ((it2, line) in block.lines.withIndex()) {
            val lineText = line.text
            val lineCornerPoints = line.cornerPoints
            val lineFrame = line.boundingBox
            helper.drawBoxAndSaveImage(rotatedBitmap, lineFrame, "myimage$it1$it2.jpg")
            for ((it3, element) in line.elements.withIndex()) {
              val elementText = element.text
              val elementCornerPoints = element.cornerPoints
              val elementFrame = element.boundingBox
              helper.drawBoxAndSaveImage(rotatedBitmap, elementFrame, "myimage$it1$it2$it3.jpg")
              for((it4, symbol) in element.symbols.withIndex()) {
                val symbolText = symbol.text
                val symbolCornerPoints = symbol.cornerPoints
                val symbolFrame = symbol.boundingBox
                helper.drawBoxAndSaveImage(rotatedBitmap, symbolFrame, "myimage$it1$it2$it3$it4.jpg")
              }
            }
          }
        }
        Log.d("TAG", "Inside function: $resultText")
        promise.resolve(resultText)
      }
    }catch(e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun callSpeaker(text: String) {
    audioClass.speakText(text)
    Log.d("TAG", "Called it")
  }

  @ReactMethod
  fun callListener(promise: Promise) {
    audioClass.setTextRecognitionCallback { recognizedText ->
      Log.d("TAG", "Recognized text: $recognizedText")
      promise.resolve(recognizedText)
    }
    audioClass.startListening()
  }

  @ReactMethod
  fun stopSpeaker() {
    audioClass.stopSpeaking()
  }

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun detectFaces(uri: String, promise: Promise){
    faceDetector.detectFaces(uri, promise)
  }


  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun saveFace(uri: String, name: String, promise: Promise){
    tensorImage = helper.uriToTensor(uri)
    if(tensorImage == null){
      promise.resolve("Image is not supported\n")
    }else {
      faceDetector.runFaceDetector(tensorImage!!.bitmap) { faces ->
        val detectedFacesPosition = mutableListOf<Rect>()
        faces.forEach {
          detectedFacesPosition.add(it.boundingBox)
        }

        if (faces.isEmpty()) {
          audioClass.speakText("No face detected. Please try again.")
        }
        if (faces.size > 1) {
          audioClass.speakText("More than once face detected. To avoid confusion please try again with one face.")
        }

        val extractedFaces = faceDetector.extractAllImages(tensorImage!!.bitmap, detectedFacesPosition)
        extractedFaces.forEach {
          val faceDataArray = faceDetector.getFaceNetEmbedding(it)
          val currFaceData = FaceDB(name, faceDataArray)
          databaseUtil.addOneFace(currFaceData)
        }
        audioClass.speakText("Person information saved successfully.")
      }
    }
  }

  @ReactMethod
  fun deleteFace(name: String, promise: Promise) {
    try{
      databaseUtil.deleteFaceOf(name)
      promise.resolve("Deleted Successfully")
    } catch (e: Exception) {
      println("Error occurred: $e")
      promise.reject(e)
    }
  }
}
