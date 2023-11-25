package com.ahpp.coinvert

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ahpp.coinvert.animatedCard.AnimatedBorderCard
import com.ahpp.coinvert.api.Api
import com.ahpp.coinvert.dropdownmenu.LargeDropdownMenu
import com.ahpp.coinvert.util.PreferenceManager
import java.util.SortedMap

class MainActivity : ComponentActivity() {

    private lateinit var dataUltimaAtt: String
    private lateinit var currencyRatesSorted: SortedMap<String, String>
    private lateinit var keysList: List<String> // Lista para conter apenas as moedas
    private lateinit var valuesList: List<String> // Lista para conter apenas os valores

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)

        if(isNetworkAvailable(this)){
            //se tiver conexao com internet, atualize tudo
            val api = Api()
            api.setCurrencyRates(this@MainActivity)
            this.currencyRatesSorted = api.getCurrencyRates().toSortedMap()
            this.keysList = currencyRatesSorted.keys.toList()
            this.valuesList = currencyRatesSorted.values.toList()
            this.dataUltimaAtt = api.getDate()
            // Salve os novos dados localmente
            preferenceManager.saveData(keysList, valuesList, dataUltimaAtt)
        } else if (preferenceManager.hasSavedData()){
            //se tiver dados salvos localmente, use-os
            this.keysList = preferenceManager.getKeysList()
            this.valuesList = preferenceManager.getValuesList()
            this.dataUltimaAtt = preferenceManager.getDataUltimaAtt()
            Toast.makeText(this, "Sem conexão. Usando dados previamente salvos.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Sem conexão. Fechando app.", Toast.LENGTH_SHORT).show()
            finish()
        }

        installSplashScreen()
        setContent {
            DrawBackground()
        }
    }

    @Composable
    fun DrawBackground() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF018685), Color(0xFF86E2D9))))
        ) {
            //imagem
            Image(
                painter = painterResource(id = R.drawable.coin_money),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 10.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //header
                AnimatedBorderCard(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                        .padding(top = 50.dp, start = 16.dp, end = 16.dp),
                    shape = RoundedCornerShape(size = 10.dp),
                    gradient = Brush.linearGradient(listOf(Color(0xFFFEC00D), Color.Cyan)),
                    borderWidth = 5.dp
                ) {
                    //coloque os elementos aqui
                    DrawItens()
                }
                //textinhos
                Text(
                    text = dataUltimaAtt,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }

    @Composable
    fun DrawItens() {
        var valor by rememberSaveable { mutableStateOf("") }

        // Variáveis de estado para os selectedIndex dos LargeDropdownMenu
        var fromCurrencyIndex by remember { mutableStateOf(keysList.indexOf("BRL")) }
        var toCurrencyIndex by remember { mutableStateOf(keysList.indexOf("USD")) }

        // Obtém os valores das moedas selecionadas
        val fromCurrency = valuesList.getOrNull(fromCurrencyIndex) ?: ""
        val toCurrency = valuesList.getOrNull(toCurrencyIndex) ?: ""

        Column(
            Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            Row(Modifier.weight(1f)) {
                Column(Modifier.weight(1f)) {
                    Text(text = "De:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    //coloca o spinner aqui

                    LargeDropdownMenu(
                        label = "",
                        items = keysList,
                        selectedIndex = fromCurrencyIndex,
                        onItemSelected = { index, _ -> fromCurrencyIndex = index },
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(text = "Para:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    //coloca o spinner aqui
                    LargeDropdownMenu(
                        label = "",
                        items = keysList,
                        selectedIndex = toCurrencyIndex,
                        onItemSelected = { index, _ -> toCurrencyIndex = index },
                    )
                }
            }
            Row(Modifier.weight(1f)) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Valor:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    //um editText para receber valor aqui
                    OutlinedTextField(
                        value = valor,
                        onValueChange = { if (it.length <= 15) valor = it },
                        label = { Text(text = "Digite um valor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Black,
                            focusedBorderColor = Color(0xFF018685),
                            focusedLabelColor = Color(0xFF018685),
                            cursorColor = Color(0xFF018685)
                        )
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Valor convertido:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(start = 10.dp, bottom = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = calcularCotacao(valor, fromCurrency, toCurrency),
                            fontSize = 25.sp
                        )
                    }
                }
            }
        }
    }

    private fun calcularCotacao(valor: String, cotacaoDe: String, cotacaoPara: String): String {
        if (valor.isEmpty() || "N/A" == valor)
            return "0"
        return try {
            String.format(
                "%.2f",
                (valor.toDouble() / cotacaoDe.toDouble()) * (cotacaoPara.toDouble())
            )
        } catch (ignored: Exception) {
            "0"
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}