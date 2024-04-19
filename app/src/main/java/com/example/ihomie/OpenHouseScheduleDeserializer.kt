package com.example.ihomie

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class OpenHouseScheduleDeserializer : JsonDeserializer<List<OpenHouseSchedule>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<OpenHouseSchedule> {
        return when {
            json?.isJsonArray == true -> {
                val jsonArray = json.asJsonArray
                jsonArray.map { jsonElement ->
                    Gson().fromJson(jsonElement, OpenHouseSchedule::class.java)
                }
            }
            json?.isJsonObject == true -> {
                emptyList()
            }
            else -> {
                emptyList()
            }
        }
    }
}