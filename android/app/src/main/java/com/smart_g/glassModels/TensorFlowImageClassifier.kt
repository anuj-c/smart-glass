package com.smart_g.glassModels

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.media.VolumeShaper
import android.media.VolumeShaper.Operation
import android.os.Trace
import android.util.Log
import com.smart_g.glassModels.Classifier.Recognition
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.PriorityQueue
import java.util.Vector
import kotlin.math.min

class TensorFlowImageClassifier private constructor() : Classifier {
  private var inputName: String? = null
  private var outputName: String? = null
  private var inputSize = 0
  private var imageMean = 0
  private var imageStd = 0f

  private val labels = Vector<String>()
  private lateinit var intValues: IntArray
  private lateinit var floatValues: FloatArray
  private lateinit var outputs: FloatArray
  private lateinit var outputNames: Array<String?>
  private var logStats = false
  private var inferenceInterface: TensorFlowInferenceInterface? = null
  override fun recognizeImage(bitmap: Bitmap?): List<Recognition?> {
    Trace.beginSection("recognizeImage")
    Trace.beginSection("preprocessBitmap")
    bitmap!!.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight())
    for (i in intValues.indices) {
      val `val` = intValues[i]
      floatValues[i * 3 + 0] = ((`val` shr 16 and 0xFF) - imageMean) / imageStd
      floatValues[i * 3 + 1] = ((`val` shr 8 and 0xFF) - imageMean) / imageStd
      floatValues[i * 3 + 2] = ((`val` and 0xFF) - imageMean) / imageStd
    }
    Trace.endSection()

    Trace.beginSection("feed")
    inferenceInterface?.feed(inputName, floatValues, 1, inputSize.toLong(), inputSize.toLong(), 3)
    Trace.endSection()

    Trace.beginSection("run")
    inferenceInterface?.run(outputNames, logStats)
    Trace.endSection()

    Trace.beginSection("fetch")
    inferenceInterface?.fetch(outputName, outputs)
    Trace.endSection()

    val pq = PriorityQueue(
      3,
      object : Comparator<Recognition?> {
        override fun compare(lhs: Recognition?, rhs: Recognition?): Int {
          if (lhs != null && rhs != null) {
            return rhs.confidence!!.compareTo(lhs.confidence!!)
          }
          return -1
        }
      })
    for (i in outputs.indices) {
      if (outputs[i] > THRESHOLD) {
        pq.add(
          Recognition(
            "" + i, if (labels.size > i) labels[i] else "unknown", outputs[i], null
          )
        )
      }
    }
    val recognitions = ArrayList<Recognition?>()
    val recognitionsSize =
      min(pq.size.toDouble(), MAX_RESULTS.toDouble())
        .toInt()
    for (i in 0 until recognitionsSize) {
      recognitions.add(pq.poll())
    }
    Trace.endSection()
    return recognitions
  }

  override fun enableStatLogging(debug: Boolean) {
    this.logStats = debug
  }

  override val statString: String?
    get() = inferenceInterface?.statString

  override fun close() {
    inferenceInterface?.close()
  }

  companion object {
    private const val TAG = "TensorFlowImageClassifier"

    private const val MAX_RESULTS = 1
    private const val THRESHOLD = 0.0f

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param inputSize The input size. A square image of inputSize x inputSize is assumed.
     * @param imageMean The assumed mean of the image values.
     * @param imageStd The assumed std of the image values.
     * @param inputName The label of the image input node.
     * @param outputName The label of the output node.
     * @throws IOException
     */
    fun create(
      assetManager: AssetManager,
      modelFilename: String?,
      labelFilename: String,
      inputSize: Int,
      imageMean: Int,
      imageStd: Float,
      inputName: String?,
      outputName: String?
    ): Classifier {
      val c = TensorFlowImageClassifier()
      c.inputName = inputName
      c.outputName = outputName

      val actualFilename =
        labelFilename.split("file:///android_asset/".toRegex()).dropLastWhile { it.isEmpty() }
          .toTypedArray()[1]
      Log.i(
        TAG,
        "Reading labels from: $actualFilename"
      )
      var br: BufferedReader? = null
      try {
        br = BufferedReader(InputStreamReader(assetManager.open(actualFilename)))
        var line: String = ""
        while (br.readLine().also {
          if(it != null)
            line = it
          } != null) {
          c.labels.add(line)
        }
        br.close()
      } catch (e: IOException) {
        throw RuntimeException("Problem reading label file!", e)
      }
      c.inferenceInterface = TensorFlowInferenceInterface(assetManager, modelFilename)

      val operation: org.tensorflow.Operation? = c.inferenceInterface!!.graphOperation(outputName)
      val output: org.tensorflow.Output<Any> = operation!!.output(0)

      val shape = output.shape()
      val numClasses = shape.size(1).toInt()
      Log.i(TAG, "Read " + c.labels.size + " labels, output layer size is " + numClasses)

      c.inputSize = inputSize
      c.imageMean = imageMean
      c.imageStd = imageStd

      c.outputNames = arrayOf(outputName)
      c.intValues = IntArray(inputSize * inputSize)
      c.floatValues = FloatArray(inputSize * inputSize * 3)
      c.outputs = FloatArray(numClasses)
      return c
    }
  }
}
