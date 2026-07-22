package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.FuelEntry
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromFuelEntryList(entries: List<FuelEntry>?): String {
        if (entries == null) return "[]"
        val array = JSONArray()
        for (entry in entries) {
            val obj = JSONObject()
            obj.put("id", entry.id)
            obj.put("quantityLitres", entry.quantityLitres)
            obj.put("odometerKm", entry.odometerKm ?: JSONObject.NULL)
            obj.put("dateTime", entry.dateTime)
            obj.put("pricePerLitre", entry.pricePerLitre ?: JSONObject.NULL)
            obj.put("totalAmountPaid", entry.totalAmountPaid ?: JSONObject.NULL)
            obj.put("stationLocation", entry.stationLocation ?: "")
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toFuelEntryList(data: String?): List<FuelEntry> {
        if (data.isNullOrBlank()) return emptyList()
        val list = mutableListOf<FuelEntry>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FuelEntry(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        quantityLitres = obj.optDouble("quantityLitres", 0.0),
                        odometerKm = if (obj.isNull("odometerKm")) null else obj.optDouble("odometerKm"),
                        dateTime = obj.optLong("dateTime", System.currentTimeMillis()),
                        pricePerLitre = if (obj.isNull("pricePerLitre")) null else obj.optDouble("pricePerLitre"),
                        totalAmountPaid = if (obj.isNull("totalAmountPaid")) null else obj.optDouble("totalAmountPaid"),
                        stationLocation = obj.optString("stationLocation", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
