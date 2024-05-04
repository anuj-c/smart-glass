package com.smart_g.glassModels

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import org.apache.commons.codec.language.DoubleMetaphone

class Audio(val context: Context) {
  private var tts: TextToSpeech? = null
  private var speechRecognizer: SpeechRecognizer? = null
  private val handler = Handler(Looper.getMainLooper())
  private var textRecognitionCallback: ((String) -> Unit)? = null
  private val commands = listOf("start", "terminate", "camera", "text", "medicine", "headline",
    "remember", "as", "who", "person", "speaker", "money", "currency", "locate", "is", "there",
    "a", "many", "people", "delete", "detect", "object")

  init {
    initializeTextToSpeech()
    handler.post {
      initializeSpeechRecognizer()
    }
  }

  private fun initializeTextToSpeech() {
    tts = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
      if (status == TextToSpeech.SUCCESS) {
        val result = tts?.setLanguage(Locale.US)
        if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
          Log.d("TAG", "TextToSpeech is ready!")
        } else {
          // Handle error
          Log.d("TAG", "TextToSpeech is failed to speak!")
        }
      } else {
        // Initialization failed
        Log.d("TAG", "TextToSpeech is failed to initialize in the first place!")
      }
    })
  }

  private fun initializeSpeechRecognizer() {
    if (SpeechRecognizer.isRecognitionAvailable(context)) {
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            Log.d("TAG", "Ready for speech")
          }
          override fun onBeginningOfSpeech() {
            Log.d("TAG", "Beginning of speech")
          }
          override fun onRmsChanged(rmsdB: Float) {}
          override fun onBufferReceived(buffer: ByteArray?) {}
          override fun onEndOfSpeech() {
            Log.d("TAG", "End of speech")
          }
          override fun onError(error: Int) {
            val message = when (error) {
              SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
              SpeechRecognizer.ERROR_CLIENT -> "Client side error"
              SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
              SpeechRecognizer.ERROR_NETWORK -> "Network error"
              SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
              SpeechRecognizer.ERROR_NO_MATCH -> "No match"
              SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
              SpeechRecognizer.ERROR_SERVER -> "Error from server"
              SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
              else -> "Unknown error"
            }
            Log.d("TAG", "Error: $message")
          }
          override fun onResults(results: Bundle?) {
            Log.d("TAG", "Finished Listening")
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
              val text = matches[0]
              handleListenedText(text)
            }
          }
          override fun onPartialResults(partialResults: Bundle?) {
            Log.d("TAG", "Partial Res: $partialResults")
          }
          override fun onEvent(eventType: Int, params: Bundle?) {
            Log.d("TAG", "EventType: $eventType")
          }
        })
      }
    }
  }

  fun setTextRecognitionCallback(callback: (String) -> Unit) {
    textRecognitionCallback = callback
  }

  fun handleListenedText (recText: String) {
    textRecognitionCallback?.invoke(recText)
  }

  fun startListening() {
    if (SpeechRecognizer.isRecognitionAvailable(context)) {
      handler.post {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        speechRecognizer?.startListening(intent)
      }
    }
  }

  fun speakText(text: String) {
    if (tts != null) {
      tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
  }

  fun stopSpeaking() {
    tts?.stop()
  }

  fun isSpeaking(): Boolean? {
    return tts?.isSpeaking
  }

  fun isPhoneticallySimilar(word1: String): MutableList<String> {
    val doubleMetaphone = DoubleMetaphone()
    val matches = mutableListOf<String>()
//    val encodedWord1 = doubleMetaphone.doubleMetaphone(word1)
//    println(encodedWord1)
    commands.forEach {
//      val encodedWord2 = doubleMetaphone.doubleMetaphone(it)
      val distance = levenshtein(word1, it)
      val maxLength = maxOf(word1.length, it.length)
      val percent = (1 - distance.toDouble() / maxLength)
      if(percent > 0.5)
        println("$percent| $it")
    }
    println(matches)
    return matches
  }

  private fun levenshtein(lhs : CharSequence, rhs : CharSequence) : Int {
    val lhsLength = lhs.length
    val rhsLength = rhs.length

    var cost = IntArray(lhsLength + 1) { it }
    var newCost = IntArray(lhsLength + 1) { 0 }

    for (i in 1..rhsLength) {
      newCost[0] = i

      for (j in 1..lhsLength) {
        val editCost = if(lhs[j - 1] == rhs[i - 1]) 0 else 1

        val costReplace = cost[j - 1] + editCost
        val costInsert = cost[j] + 1
        val costDelete = newCost[j - 1] + 1

        newCost[j] = minOf(costInsert, costDelete, costReplace)
      }

      val swap = cost
      cost = newCost
      newCost = swap
    }

    return cost[lhsLength]
  }


  fun release() {
    handler.post {
      tts?.stop()
      tts?.shutdown()
      speechRecognizer?.destroy()
    }
  }
}