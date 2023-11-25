package com.ahpp.coinvert.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    fun saveData(keysList: List<String>, valuesList: List<String>, dataUltimaAtt: String) {
        val editor = sharedPreferences.edit()
        editor.putString("keysList", keysList.joinToString(","))
        editor.putString("valuesList", valuesList.joinToString(","))
        editor.putString("dataUltimaAtt", dataUltimaAtt)
        editor.apply()
    }

    fun getKeysList(): List<String> {
        val savedKeysList = sharedPreferences.getString("keysList", "") ?: ""
        return savedKeysList.split(",").filter { it.isNotEmpty() }
    }

    fun getValuesList(): List<String> {
        val savedValuesList = sharedPreferences.getString("valuesList", "") ?: ""
        return savedValuesList.split(",").filter { it.isNotEmpty() }
    }

    fun getDataUltimaAtt(): String {
        return sharedPreferences.getString("dataUltimaAtt", "") ?: ""
    }

    fun hasSavedData(): Boolean {
        return sharedPreferences.contains("keysList") &&
                sharedPreferences.contains("valuesList") &&
                sharedPreferences.contains("dataUltimaAtt")
    }
}
