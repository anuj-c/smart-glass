package com.smart_g.glassModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class FaceData(val context: Context) {
  private val faceDetectionImgSize = 160

  private val faceDetectorOptions : FaceDetectorOptions = FaceDetectorOptions.Builder()
    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
    .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
    .build()

  private val helper: Helpers = Helpers(context)
  private val faceDetector: FaceDetector = FaceDetection.getClient(faceDetectorOptions)
  private var tfliteInterpreter : Interpreter
  private var imageTensorProcessor : ImageProcessor
  private val databaseUtil = Database(context, "face.db", 1)
  private val audioClass = Audio(context)

  init {
    val interpreterOptions = Interpreter.Options().apply {
      numThreads = 4
    }
    tfliteInterpreter = Interpreter(FileUtil.loadMappedFile(context, "facenet.tflite") , interpreterOptions )

    imageTensorProcessor = ImageProcessor.Builder()
      .add( ResizeOp( faceDetectionImgSize , faceDetectionImgSize , ResizeOp.ResizeMethod.BILINEAR ) )
      .add( NormalizeOp( 127.5f , 127.5f ) )
      .build()
  }

  fun extractAllImages(srcBitmap: Bitmap?, results: List<Rect>): List<Bitmap>{
    if(srcBitmap==null){
      Log.e("extractImgErr", "cannot extract images as src img is null")
      return emptyList()
    }
    val extractedBitmaps = mutableListOf<Bitmap>()
    results.forEach {
        rect->
      val croppedBitmapImg = Bitmap.createBitmap(srcBitmap, rect.left, rect.top, rect.width(), rect.height())
      extractedBitmaps.add(croppedBitmapImg)
    }
    return extractedBitmaps
  }

  fun runFaceDetector(bitmap: Bitmap?, callback: (List<Face>) -> Unit) {
    if (bitmap == null)
      return

    val inputImage = InputImage.fromBitmap(bitmap, 0)
    faceDetector.process(inputImage)
      .addOnSuccessListener { faces ->
        callback(faces)
      }.addOnFailureListener { error ->
        Log.e("FaceDetectionFail", "$error")
      }
  }

  fun getFaceNetEmbedding(bitmap: Bitmap) : FloatArray{
    val compatibleBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val imageTensor = imageTensorProcessor.process(TensorImage.fromBitmap(compatibleBitmap))
    val outputs = Array(1) { FloatArray(128 ) }
    tfliteInterpreter.run(imageTensor.buffer, outputs)

    return outputs[0]
  }

  fun isFaceInDB(currentFaceFloatArray: FloatArray, allFacesInDb: List<FaceDB>): String {
    val threshold = 0.6
    val avgScoreFromName = hashMapOf<String, Pair<Int, Double>>()

    allFacesInDb.forEach {
        activeFace->
      if(avgScoreFromName.containsKey(activeFace.getName())){
        val updatedScore = (avgScoreFromName[activeFace.getName()]!!.second * avgScoreFromName[activeFace.getName()]!!.first +
          activeFace.getCosineSimilarity(currentFaceFloatArray)) /
          (avgScoreFromName[activeFace.getName()]!!.first+1)
        val updatedCount = avgScoreFromName[activeFace.getName()]!!.first+1
        avgScoreFromName[activeFace.getName()] = Pair(updatedCount, updatedScore)
      }else
        avgScoreFromName[activeFace.getName()] = Pair(1, activeFace.getCosineSimilarity(currentFaceFloatArray))
    }

    var currentMax = 0.0
    var currMaxName = ""
    for((pName, avgScore) in avgScoreFromName){
      if(avgScore.second > currentMax){
        currMaxName = pName
        currentMax = avgScore.second
      }
    }

    Log.d("curr", currentMax.toString())
    if(currentMax > threshold){
      return currMaxName
    }
    return ""
  }

  @RequiresApi(Build.VERSION_CODES.P)
  fun detectFaces(uri: String, callback: (String, String, MutableList<String>) -> Unit) {
    val detectedFacesPosition = mutableListOf<Rect>()
    val detectedFacesPrediction = mutableListOf<String>()

    val srcBitmap = helper.uriToTensor(uri)?.bitmap
    if(srcBitmap==null){
      Log.e("ImageCaptureError", "Image capture returned null")
      callback("error", "Image not supported. Please try again.", detectedFacesPrediction)
      return
    }

    val allFacesInDb = databaseUtil.getAllFaces()
    if(allFacesInDb.isEmpty()){
      callback("error", "There are no saved persons in the database. Please save at least one face to perform detection.", detectedFacesPrediction)
      return
    }

    runFaceDetector(srcBitmap){results ->
      results.forEach {
        detectedFacesPosition.add(it.boundingBox)
      }

      val extractedFaces = extractAllImages(srcBitmap, detectedFacesPosition)

      extractedFaces.forEach {face->
        val currentFaceFloatArray = getFaceNetEmbedding(face)
        val curName = isFaceInDB(currentFaceFloatArray, allFacesInDb)
        detectedFacesPrediction.add(curName)
      }

      callback("success", "${extractedFaces.size}", detectedFacesPrediction)
    }
  }
}