package fr.isep.dictionary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import fr.isep.dictionary.databinding.ActivityMainBinding
import fr.isep.dictionary.models.theWord
import fr.isep.dictionary.util.RetrofitInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var txtToSpeech: TextToSpeech
    lateinit var binding: ActivityMainBinding
    lateinit var adapter: MeaningAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set initial visibility of speak button to GONE
        binding.speakBtn.visibility = View.GONE

        binding.searchBtn.setOnClickListener {
            // Get the word from the search field
            val word = binding.searchFieldInput.text.toString()
            getDescription(word)

            var btnSpeak = findViewById<ImageButton>(R.id.speak_btn)
            binding.speakBtn.visibility = View.VISIBLE

            btnSpeak.setOnClickListener {
                // Use TextToSpeech and speak the word
                txtToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {
                    if (it == TextToSpeech.SUCCESS) {
                        txtToSpeech.language = Locale.US
                        txtToSpeech.setSpeechRate(1.0f)
                        txtToSpeech.speak(word.toString(), TextToSpeech.QUEUE_ADD, null)
                    }
                })
            }
        }

        // Initialize the adapter and set it to the RecyclerView
        adapter = MeaningAdapter(emptyList())
        binding.meaningRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.meaningRecyclerView.adapter = adapter
    }

    // Function to fetch the meaning/description of the word
    private fun getDescription(word: String) {
        setInProgress(true)
        // Using coroutines to perform the network request asynchronously
        GlobalScope.launch {
            try {
                // Make a network request using Retrofit to get the meaning of the word
                val response = RetrofitInstance.dictionaryApi.getMeaning(word)

                if (response.body() == null) {
                    throw (Exception())
                }
                // Update UI on the main thread
                runOnUiThread {
                    // Set progress UI to indicate loading is complete
                    setInProgress(false)
                    // Update the UI with the meaning of the word
                    response.body()?.first()?.let {
                        setUI(it)
                    }
                }
            } catch (e: Exception) {
                // If you don't find the word
                runOnUiThread {
                    setInProgress(false)
                    Toast.makeText(
                        applicationContext,
                        "Something went wrong, it could be the word doesn't exist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Update the UI with the meaning of the word
    private fun setUI(response: theWord) {
        binding.wordTextview.text = response.word
        binding.phoneticTextview.text = response.phonetic
        adapter.updateNewData(response.meanings)
    }

    // Set the visibility of UI elements based on loading status
    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.searchBtn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.searchBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}
