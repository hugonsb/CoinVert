package com.ahpp.coinvert.api

import android.content.Context
import android.util.Xml
import android.widget.Toast
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Api {
    private var currencyRates = HashMap<String, String>() //Para armazenar os pares currency-rate
    private var currentTime: String? = null
    private var date: String? = null

    fun setCurrencyRates(context: Context) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {

            val url = URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml")
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val xmlInputStream = connection.inputStream

            if (xmlInputStream != null) {
                try {
                    val xmlPullParser = Xml.newPullParser()
                    xmlPullParser.setInput(xmlInputStream, null)
                    var eventType = xmlPullParser.eventType
                    var currentCurrency = ""
                    var currentRate = ""

                    currencyRates["EUR"] = "1.0"

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {
                            XmlPullParser.START_TAG -> {
                                when (xmlPullParser.name) {
                                    "Cube" -> {
                                        if (xmlPullParser.getAttributeValue(
                                                null,
                                                "currency"
                                            ) != null
                                        ) {
                                            currentCurrency =
                                                xmlPullParser.getAttributeValue(null, "currency")
                                        }
                                        if (xmlPullParser.getAttributeValue(null, "rate") != null) {
                                            currentRate =
                                                xmlPullParser.getAttributeValue(null, "rate")
                                        }
                                        if (xmlPullParser.getAttributeValue(null, "time") != null) {
                                            currentTime =
                                                xmlPullParser.getAttributeValue(null, "time")
                                        }
                                    }
                                }
                            }

                            XmlPullParser.END_TAG -> {
                                if (xmlPullParser.name == "Cube" && currentCurrency.isNotEmpty() && currentRate.isNotEmpty()) {
                                    currencyRates[currentCurrency] = currentRate
                                    currentCurrency = ""
                                    currentRate = ""
                                }
                            }
                        }
                        eventType = xmlPullParser.next()
                    }
                    setDate()

                } catch (e: XmlPullParserException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    xmlInputStream.close()
                    executor.shutdown()
                }
            }
        }

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Toast.makeText(context, "Erro de rede", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun getCurrencyRates(): HashMap<String, String> {
        return this.currencyRates
    }

    private fun setDate() {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale("US"))
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale("BR"))
        val date = currentTime?.let { inputFormat.parse(it) }
        val formattedDate = date?.let { outputFormat.format(it) }
        this.date = formattedDate
    }

    fun getDate(): String {
        return "Dados obtidos do Banco Central Europeu\n Última atualização: $date"
    }
}