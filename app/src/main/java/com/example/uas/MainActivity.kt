package com.example.uas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.uas.ui.theme.UasTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProfileScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var movieData by remember { mutableStateOf<JSONObject?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .offset(y = 20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Proyek Manajemen",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aplikasi ini dirancang untuk projek UAS kelompok 3, yaitu untuk mengelola proyek manajemen yang sudah kami rancang sedemikian rupa dengan susah payah mengerjakan satu minggu penuh dengan tidur yang tidak nyenyak, karena codingan eror, saya harap bapak ibu dosen bisa memaklumi aplikasi sederhana ini, karena kami masih belajar dan masih pemula.",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                val intent = Intent(context, MainActivity1::class.java)
                context.startActivity(intent)
            }) {
                Text(text = "Mulai")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                performApiRequest(
                    onSuccess = { data ->
                        movieData = data
                        errorMessage = ""
                    },
                    onError = { error ->
                        errorMessage = error
                    }
                )
            }) {
                Text(text = "Akses API")
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
        if (errorMessage.isNotEmpty()) {
            Text(text = "Error: $errorMessage", color = androidx.compose.ui.graphics.Color.Red)
        } else if (movieData != null) {
            movieData?.let { data ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    item {
                        val posterUrl = data.getString("Poster")
                        if (posterUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(posterUrl),
                                contentDescription = "Movie Poster",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(500.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Title: ${data.getString("Title")}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Year: ${data.getString("Year")}", fontSize = 18.sp)
                        Text(text = "Rated: ${data.getString("Rated")}", fontSize = 18.sp)
                        Text(text = "Released: ${data.getString("Released")}", fontSize = 18.sp)
                        Text(text = "Runtime: ${data.getString("Runtime")}", fontSize = 18.sp)
                        Text(text = "Genre: ${data.getString("Genre")}", fontSize = 18.sp)
                        Text(text = "Director: ${data.getString("Director")}", fontSize = 18.sp)
                        Text(text = "Writer: ${data.getString("Writer")}", fontSize = 18.sp)
                        Text(text = "Actors: ${data.getString("Actors")}", fontSize = 18.sp)
                        Text(text = "Plot: ${data.getString("Plot")}", fontSize = 16.sp)
                        Text(text = "Language: ${data.getString("Language")}", fontSize = 18.sp)
                        Text(text = "Country: ${data.getString("Country")}", fontSize = 18.sp)
                        Text(text = "Awards: ${data.getString("Awards")}", fontSize = 18.sp)
                        Text(text = "IMDB Rating: ${data.getString("imdbRating")}", fontSize = 18.sp)
                        Text(text = "IMDB Votes: ${data.getString("imdbVotes")}", fontSize = 18.sp)
                        Text(text = "Box Office: ${data.getString("BoxOffice")}", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

fun performApiRequest(onSuccess: (JSONObject) -> Unit, onError: (String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL("https://www.omdbapi.com/?i=tt3896198&apikey=4c80ad95") // Ubah ke HTTPS
            val urlConnection = url.openConnection() as HttpURLConnection
            try {
                val inputStream = urlConnection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                withContext(Dispatchers.Main) {
                    onSuccess(jsonResponse)
                }
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    UasTheme {
        ProfileScreen()
    }
}
