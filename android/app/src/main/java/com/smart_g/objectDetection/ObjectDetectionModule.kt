package com.smart_g.objectDetection

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableNativeMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.task.vision.detector.Detection

class ObjectDetectionModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  override fun getName(): String {
    return "ObjectDetection"
  }

  @ReactMethod
  fun detectObjects(uri: String, promise: Promise) {
    Log.d("TAG", "Function call received")

    coroutineScope.launch {
      try {
        val detector = Detector<Any?>(reactApplicationContext)
        val results = detector.detectObjects(uri)
        Log.d("TAG", "Results: $results")

        val data = mutableListOf<Map<String, Any?>>()
        for(result in results) {
          val cates = result.categories
          for (cats in cates) {
            val label = cats.label

            val map = mapOf(
              "label" to label,
            )

            data.add(map)
          }
        }

        val ret = Arguments.createArray()
        for (item in data) {
          val map = Arguments.createMap()
          for ((key, value) in item) {
            map.putString(key, value.toString())
          }
          ret.pushMap(map)
        }
        promise.resolve(ret)
      } catch (e: Exception) {
        Log.e("TAG", "Error in detecting objects", e)
        promise.reject(e)
      }
    }

//    detector.detectObjects(uri, onSuccess = { detectedObjects ->
//      Log.d("TAG", "Processing detection")
//      val objectsList = mutableListOf<Map<String, Any>>()
//
//      for (detectedObject in detectedObjects) {
//        val detectedObjectMap = hashMapOf<String, Any>()
//        detectedObject.labels.forEach { label ->
//          Log.d("TAG", label.text)
//          detectedObjectMap["text"] = label.text
//          detectedObjectMap["confidence"] = label.confidence
//        }
//        detectedObject.boundingBox.let { box ->
//          detectedObjectMap["boundingBox"] = mapOf(
//            "left" to box.left,
//            "top" to box.top,
//            "right" to box.right,
//            "bottom" to box.bottom
//          )
//        }
//        objectsList.add(detectedObjectMap)
//      }
//      Log.d("TAG", objectsList.toString())
//      promise.resolve(objectsList)
//  }, onFailure = { e ->
//    promise.reject("Object Detection Error", e.message)
//  })
  }
  @Deprecated("Deprecated in Java")
  override fun onCatalystInstanceDestroy() {
    super.onCatalystInstanceDestroy()
    coroutineScope.cancel() // Cancel coroutines when the React instance is destroyed
  }
}
