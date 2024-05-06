package com.smart_g.glassModels

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(
  context: Context?,
  dbName: String?,
  version: Int
) : SQLiteOpenHelper(context, dbName, null, version) {


  override fun onCreate(db: SQLiteDatabase?) {
    val createTableStatement = "Create Table " +
      TABLE_NAME+
      " (ID Integer Primary Key Autoincrement, " +
      COL_PERSON_NAME +
      " String, " +
      COL_VALUE +
      " String)"
    db?.execSQL(createTableStatement)
  }

  override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    TODO("Not yet implemented")
  }

  fun addOneFace(faceData: FaceDB): Boolean{
    val cv = ContentValues()
    cv.put(COL_PERSON_NAME, faceData.getName())
    cv.put(COL_VALUE, faceData.getSerialisedData())
    val status = this.writableDatabase.insert(TABLE_NAME, null, cv)
    if(status==-1L)
      return false
    return true
  }

  fun deleteFaceOf(personName: String): Int {
    val args = arrayOf(personName)
    return writableDatabase.delete(TABLE_NAME, "$COL_PERSON_NAME=?", args)
  }

  fun getAllFaces() : List <FaceDB>{
    val queryString = "Select * from $TABLE_NAME"
    val allFaces = mutableListOf<FaceDB>()
    val cursor = this.readableDatabase.rawQuery(queryString, null)

    //If no data return empty list
    if(!cursor.moveToFirst()){
      return emptyList()
    }

    do {
      val personName = cursor.getString(1)
      val serialisedValue = cursor.getString(2)
      val activeFace = FaceDB(personName, serialisedValue)
      allFaces.add(activeFace)

    }while(cursor.moveToNext())

    cursor.close()
    return allFaces
  }

  companion object{
    private const val TABLE_NAME = "FaceDataTable"
    private const val COL_PERSON_NAME = "PersonName"
    private const val COL_VALUE = "Value"
  }
}