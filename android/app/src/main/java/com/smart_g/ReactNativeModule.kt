package com.smart_g

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableArray
import com.google.mlkit.vision.common.InputImage
import com.smart_g.glassModels.Audio
import com.smart_g.glassModels.Currency2
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
  private val currencyUtil = Currency2(reactApplicationContext)

  private var image: Bitmap? = null
  private var tensorImage: TensorImage? = null
  private val matrix = Matrix().apply {
    postRotate(90f)
  }

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun detectObjects(uri: String, promise: Promise) {
    try {
      tensorImage = helper.uriToTensor(uri)
      val tensorImage2 = helper.uriToTensor(uri)
      val results = detector.detectObjects(tensorImage)

      val data = mutableMapOf<String, Int>()
      for (result in results) {
        var maxConf = 0.0f
        var label = ""
        for (cats in result.categories) {
          if(cats.score > maxConf) {
            maxConf = cats.score
            label = cats.label
          }
        }
        helper.drawBoxAndSaveImage(tensorImage2!!.bitmap, helper.rectF2Rect(result.boundingBox),
          "${label}.jpg"
        )
        if (data.containsKey(label)) {
          data[label] = data[label]!! + 1
        } else {
          data[label] = 1
        }
      }

      var strToSpeak = detector.describeObjects(data)
      strToSpeak = "Objects detected are $strToSpeak"
      audioClass.speakText(strToSpeak)

      val ret = Arguments.createArray()
      for ((key, value) in data) {
        ret.pushString("$key-$value")
      }

      image?.recycle()
      image = null

      tensorImage = null

      promise.resolve(ret)
    } catch (e: Exception) {
      Log.e("TAG", "Error in detecting objects", e)
      promise.reject(e)
    }
  }

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun locateObject(uri: String, objectName: String, promise: Promise) {
    try{
      tensorImage = helper.uriToTensor(uri)
      val results = detector.detectObjects2(tensorImage, listOf(objectName))
      var strToSpeak = ""
      for (result in results) {
        val bBoxes = result.boundingBox
        strToSpeak += "Object is located at ${detector.getObjectPosition(bBoxes)}. "
      }
      strToSpeak = "${results.size} instances of $objectName detected. $strToSpeak."
      promise.resolve(strToSpeak)
    }catch(e: Exception) {
      Log.e("TAG", "$e")
    }
  }

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun findTheObject(uri: String, objectName: String, promise: Promise) {
    try{
      tensorImage = helper.uriToTensor(uri)
      helper.saveBitmapToFile(tensorImage!!.bitmap)
      val results = detector.detectObjects2(tensorImage, listOf(objectName))
      println(results.size)
      promise.resolve(results.size)
    }catch(e: Exception) {
      Log.e("TAG", "$e")
    }
  }

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun findNumPeople(uri: String, promise: Promise) {
    try{
      tensorImage = helper.uriToTensor(uri)
      val results = detector.detectObjects2(tensorImage, listOf("person"))
      val cntObjects = results.size
      val strToSpeak = if(cntObjects <= 1) "$cntObjects person detected."
      else "$cntObjects people detected."
      audioClass.speakText(strToSpeak)
      promise.resolve(strToSpeak)
    }catch(e: Exception) {
      Log.e("TAG", "$e")
    }
  }

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun detectObjectsForTime(uri: String, objectNames: ReadableArray, promise: Promise) {
    try {
      tensorImage = helper.uriToTensor(uri)
      val tensorImage2 = helper.uriToTensor(uri)
      val objectNamesRet: WritableArray = Arguments.createArray()
      for (i in 0 until objectNames.size()) {
        objectNamesRet.pushString(objectNames.getString(i))
      }

      val results = detector.detectObjects(tensorImage)

      val data = mutableMapOf<String, Int>()
      for (result in results) {
        var maxConf = 0.0f
        var label = ""
        for (cats in result.categories) {
          if(cats.score > maxConf) {
            maxConf = cats.score
            label = cats.label
          }
        }
        if(helper.containsElement(objectNames, label)) continue
        helper.drawBoxAndSaveImage(tensorImage2!!.bitmap, helper.rectF2Rect(result.boundingBox), "$label.jpg")
        if (data.containsKey(label)) {
          data[label] = data[label]!! + 1
        } else {
          data[label] = 1
        }
      }

      val strToSpeak = detector.describeObjects(data)
      audioClass.speakText(strToSpeak)

      while(audioClass.isSpeaking()!!) {}

      data.map {(key) ->
        objectNamesRet.pushString(key)
      }

      promise.resolve(objectNamesRet)
    }catch(e: Exception) {
      Log.e("TAG", "Error in detecting object from video")
    }
  }

  @ReactMethod
  fun detectText(uri: String, promise: Promise) {
    try {
      val imageUri = Uri.parse(uri)
      val originalBitmap = BitmapFactory.decodeStream(reactApplicationContext.contentResolver.openInputStream(imageUri))
      val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
      val rotatedInputImage = InputImage.fromBitmap(rotatedBitmap, 0)
      textDetector.detect(rotatedInputImage) {resText ->
        val allText = resText.text
        audioClass.speakText("Text detected: $allText")
        promise.resolve(allText)
      }
    }catch(e: Exception) {
      val str = "Error while detecting large text: $e"
      Log.e("TAG", str)
      promise.reject(e)
    }
  }

  @ReactMethod
  fun detectMedicineName(uri: String, promise: Promise) {
    try {
      textDetector.detectLargestText(uri) { largestText ->
        audioClass.speakText("Medicine name detected is: $largestText")
        promise.resolve("Medicine name detected is: $largestText")
      }
    }catch(e: Exception) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun detectHeadline(uri: String, promise: Promise) {
    try {
      textDetector.detectLargestText(uri) { largestText ->
        audioClass.speakText("Detected headline is: $largestText")
        promise.resolve("Detected headline is: $largestText")
      }
    }catch(e: Exception) {
      promise.reject(e)
    }
  }

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun detectCurrency(uri: String, promise: Promise) {
    currencyUtil.detectCurrency(uri) {
      audioClass.speakText(it)
      promise.resolve(it)
    }
  }

  @ReactMethod
  fun detectExpiry(uri: String, promise: Promise) {
    try {
      val imageUri = Uri.parse(uri)
      val originalBitmap = BitmapFactory.decodeStream(reactApplicationContext.contentResolver.openInputStream(imageUri))
      val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
      val rotatedInputImage = InputImage.fromBitmap(rotatedBitmap, 0)

      textDetector.detectExpiry(rotatedInputImage) {resText ->
        promise.resolve(resText)
      }
    }catch(e: Exception) {
      val str = "Error while detecting large text: $e"
      Log.e("TAG", str)
      promise.reject(e)
    }
  }

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun detectFaces(uri: String, promise: Promise){
    faceDetector.detectFaces(uri) {tag, msg, detectedNames ->
      if(tag == "error") {
        audioClass.speakText(msg)
        promise.resolve(msg)
      }else{
        val numFaceRecognised = detectedNames.size

        if(msg.toInt() == 0) {
          audioClass.speakText("No faces detected. Please try again.")
          promise.resolve("No faces detected. Please try again.")
        }else {
          var strToSpeak = ""
          strToSpeak += "$numFaceRecognised people recognised. "
          if (numFaceRecognised != msg.toInt())
            strToSpeak += "${msg.toInt() - numFaceRecognised} unknown faces detected. "
          if (detectedNames.size > 0) {
            strToSpeak += if (detectedNames.size == 1)
              "The detected person is: "
            else
              "The detected people are: "
          }
          detectedNames.forEachIndexed { _, predictedName ->
            if (predictedName.isNotEmpty()) {
              strToSpeak += "$predictedName, "
            }
          }
          audioClass.speakText(strToSpeak)
          promise.resolve(strToSpeak)
        }
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.P)
  @ReactMethod
  fun saveFace(uri: String, name: String, promise: Promise){
    tensorImage = helper.uriToTensor(uri)
    if(tensorImage == null){
      promise.resolve("Image is not supported\n")
    }else {
      faceDetector.runFaceDetector(tensorImage!!.bitmap) { faces ->
        if (faces.isEmpty()) {
          audioClass.speakText("No face detected. Please try again.")
          promise.resolve("No face detected. Please try again.")
          return@runFaceDetector
        }
        if (faces.size > 1) {
          audioClass.speakText("More than once face detected. To avoid confusion please try again with one face.")
          promise.resolve("More than once face detected. To avoid confusion please try again with one face.")
          return@runFaceDetector
        }

        val detectedFacesPosition = mutableListOf<Rect>()
        detectedFacesPosition.add(faces[0].boundingBox)

        val allFacesInDb = databaseUtil.getAllFaces()

        val extractedFaces = faceDetector.extractAllImages(tensorImage!!.bitmap, detectedFacesPosition)
        extractedFaces.forEach {
          val faceDataArray = faceDetector.getFaceNetEmbedding(it)
          val savedName = faceDetector.isFaceInDB(faceDataArray, allFacesInDb)
          if(savedName == name || savedName == "") {
            val currFaceData = FaceDB(name, faceDataArray)
            databaseUtil.addOneFace(currFaceData)
            audioClass.speakText("$name: is saved successfully.")
            promise.resolve("$name: is saved successfully.")
          }else {
            audioClass.speakText("Person previously saved as: $savedName")
            promise.resolve("Person previously saved as: $savedName. New name: $name.")
          }
        }
      }
    }
  }

  @ReactMethod
  fun deleteFace(name: String, promise: Promise) {
    try{
      databaseUtil.deleteFaceOf(name)
      audioClass.speakText("Deleted data of $name successfully")
      promise.resolve("Deleted data of $name successfully")
    } catch (e: Exception) {
      println("Error occurred: $e")
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
    audioClass.stopSpeaking()
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

  @ReactMethod
  fun checkUsbConnection(promise: Promise) {
    val usbManager = reactApplicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
    val connectedDevices = usbManager.deviceList
    if (connectedDevices.isNotEmpty()) {
      audioClass.speakText("Starting the camera.")
      promise.resolve(true)
    } else {
      audioClass.speakText("Camera not found. Please attach the smart glass.")
      promise.resolve(false)
    }
  }
}
