package com.example.androidapp6

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private val dbName: String = "SampleDB"
    private val tableName: String = "ItemTable"
    private val dbVersion: Int = 1

    private lateinit var janCode: EditText
    private lateinit var itemName: EditText
    private lateinit var price: EditText
    private lateinit var button: Button
    private lateinit var switchDelete: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val janCode = findViewById<EditText>(R.id.janCode)
        janCode = findViewById(R.id.janCode)
        janCode.setOnEditorActionListener { _, actionId, _ ->
            //Log.d("MyApp", janCode.text.toString())
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (janCode.text.isNotEmpty()) {
                    selectDataItemName()
                }
                setFocus("itemName")
            }
            return@setOnEditorActionListener true
        }

        itemName = findViewById(R.id.itemName)

        price = findViewById(R.id.price)

        //val button = findViewById<Button>(R.id.button)
        button = findViewById(R.id.button)
        button.setOnClickListener {
            if (checkInput()) {
                if (button.text.toString() == "登録") {
                    insertData(janCode.text.toString().toInt(), itemName.text.toString(), price.text.toString().toInt())
                    showToast("登録しました")
                } else if (button.text.toString() == "更新") {
                    updateData(janCode.text.toString().toInt(), itemName.text.toString(), price.text.toString().toInt())
                    showToast("更新しました")
                } else {
                    deleteData(janCode.text.toString().toInt())
                    showToast("削除しました")
                }
                clearEditText()
                setFocus("janCode")
            }
        }

        //val switchDelete = findViewById<Switch>(R.id.switchDelete)
        switchDelete = findViewById(R.id.switchDelete)
        switchDelete.setOnCheckedChangeListener { buttonView, isChecked ->
            //Toast.makeText(this, isChecked.toString(), Toast.LENGTH_SHORT).show()
            if (isChecked) {
                //button.setText("削除")
                button.setText(R.string.button_del_name)
                //button.text = "削除"
                button.setBackgroundColor(Color.RED)

            } else {
                button.setText(R.string.button_upd_name)
                button.setBackgroundColor(Color.rgb(0, 128,0))
            }
        }

    }

    private fun checkInput(): Boolean {
        if (janCode.text.isEmpty()) {
            showToast("JANコードが未入力です")
            return false
        }
        if (itemName.text.isEmpty()) {
            showToast("商品名が未入力です")
            return false
        }
        if (price.text.isEmpty()) {
            showToast("価格が未入力です")
            return false
        }
        return true
    }

    private fun clearEditText() {
        janCode.text.clear()
        itemName.text.clear()
        price.text.clear()
        button.text = "登録"
        button.setBackgroundColor(Color.BLUE)
        switchDelete.visibility = View.INVISIBLE
        switchDelete.isChecked = false
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
    }

    private fun setItemName(name: String) {
        itemName.setText(name)
    }

    private fun setFocus(name: String) {
        val viewId = resources.getIdentifier(name, "id", packageName)
        val targetFocus = findViewById<EditText>(viewId)
        targetFocus.requestFocus()
    }

    private class SampleDBHelper(
        context: Context,
        databaseName: String,
        factory: SQLiteDatabase.CursorFactory?,
        version: Int
    ) :
        SQLiteOpenHelper(context, databaseName, factory, version) {
            override fun onCreate(database: SQLiteDatabase?) {
                database?.execSQL("create table if not exists ItemTable (JanCode int primary key, ItemName text, Price int)")
            }

            override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
                if (oldVersion < newVersion) {
                    database?.execSQL("alter table ItemTable add column DeleteFlag int default 0")
                }
            }
    }

    private fun insertData(code: Int, name: String, pri: Int) {
        try {
            val dbHelper = SampleDBHelper(applicationContext, dbName, null, dbVersion)
            val database = dbHelper.writableDatabase

            val values = ContentValues()
            values.put("JanCode", code)
            values.put("ItemName", name)
            values.put("Price",pri)

            database.insertOrThrow(tableName, null, values)
        }catch (exception: Exception) {
            Log.d("insertData", exception.toString())
        }
    }

    private fun updateData(whereJanCode: Int, newItemName: String, newPrice: Int) {
        try {
            val dbHelper = SampleDBHelper(applicationContext, dbName, null, dbVersion)
            val database = dbHelper.writableDatabase

            val values = ContentValues()
            values.put("ItemName", newItemName)
            values.put("Price",newPrice)

            val whereClauses = "JanCode = ?"
            val whereArgs = arrayOf(whereJanCode.toString())

            database.update(tableName, values, whereClauses, whereArgs)
        }catch (exception: Exception) {
            Log.d("updateData", exception.toString())
        }
    }

    private fun deleteData(whereJanCode: Int) {
        try {
            val dbHelper = SampleDBHelper(applicationContext, dbName, null, dbVersion)
            val database = dbHelper.writableDatabase

            val whereClauses = "JanCode = ?"
            val whereArgs = arrayOf(whereJanCode.toString())

            database.delete(tableName, whereClauses, whereArgs)
        }catch (exception: Exception) {
            Log.d("deleteData", exception.toString())
        }
    }

    private fun selectDataItemName() {
        try{
            val dbHelper = SampleDBHelper(applicationContext, dbName, null, dbVersion)
            val database = dbHelper.readableDatabase

            val sql = "select ItemName,Price from " + tableName + " where JanCode = ?"

            val cursor = database.rawQuery(sql, arrayOf(janCode.text.toString()))
            if (cursor.count > 0) {
                cursor.moveToFirst()
                //setItemName(cursor.getString(0))
                itemName.setText(cursor.getString(0))
                price.setText(cursor.getInt(1).toString())
                button.text = "更新"
                button.setBackgroundColor(Color.rgb(0, 128,0))
                switchDelete.visibility = View.VISIBLE
                switchDelete.isChecked = false
            } else {
                itemName.text.clear()
                price.text.clear()
                button.text = "登録"
                button.setBackgroundColor(Color.BLUE)
                switchDelete.visibility = View.INVISIBLE
                switchDelete.isChecked = false
            }
            cursor.close()

        }catch (exception: Exception) {
            Log.d("selectDataItemName", exception.toString())
        }
    }
}