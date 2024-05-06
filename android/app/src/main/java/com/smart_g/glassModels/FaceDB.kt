package com.smart_g.glassModels

import kotlin.math.sqrt

class FaceDB {
  private var value : FloatArray
  private var personName : String

  constructor(pName: String, faceData: FloatArray){
    this.value = faceData
    this.personName = pName
  }

  constructor(pName: String, serialisedValue : String){
    this.value = this.getEmbeddings(serialisedValue)
    this.personName = pName
  }

  fun getName() : String{
    return this.personName
  }

  fun getValue() : FloatArray{
    return this.value
  }

  fun getSerialisedData(): String {
    var serialisedData = ""
    for(i in 0..this.value.size-2){
      serialisedData += this.value[i].toString() + STRING_SEPARATOR
    }
    serialisedData += this.value[this.value.size-1]
    return serialisedData
  }

  fun getCosineSimilarity(floatArray: FloatArray) : Double{
    var dotProduct = 0.0
    var normA = 0.0
    var normB = 0.0
    for (i in value.indices) {
      dotProduct += value[i] * floatArray[i]
      normA += value[i]*value[i]
      normB += floatArray[i]*floatArray[i]
    }
    return dotProduct / (sqrt(normA) * sqrt(normB))
  }

  private fun getEmbeddings(serialisedValue: String): FloatArray{
    val embedding = mutableListOf<Float>()
    serialisedValue.split(STRING_SEPARATOR).forEach {
      embedding.add(it.toFloat())
    }
    return embedding.toFloatArray()
  }

  companion object{
    private const val STRING_SEPARATOR = ","
  }
}