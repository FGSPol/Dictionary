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

//hei! testing github
class MainActivity : AppCompatActivity() {

    lateinit var txtToSpeech : TextToSpeech

    lateinit var binding: ActivityMainBinding
    lateinit var adapter: MeaningAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.speakBtn.visibility = View.GONE

        binding.searchBtn.setOnClickListener {
            val word = binding.searchFieldInput.text.toString()
            getDescription(word)

            var btnSpeak = findViewById<ImageButton>(R.id.speak_btn)
            binding.speakBtn.visibility = View.VISIBLE

            btnSpeak.setOnClickListener {
                txtToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {
                    if(it==TextToSpeech.SUCCESS){
                        txtToSpeech.language = Locale.US
                        txtToSpeech.setSpeechRate(1.0f)
                        txtToSpeech.speak(word.toString(), TextToSpeech.QUEUE_ADD,null)
                    }
                })
            }
        }



        adapter = MeaningAdapter(emptyList())
        binding.meaningRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.meaningRecyclerView.adapter = adapter
    }

    private fun getDescription(word: String) {
        setInProgress(true)
        GlobalScope.launch {
            try {
                val response = RetrofitInstance.dictionaryApi.getMeaning(word)
                if(response.body()==null){
                    throw (Exception())
                }
                runOnUiThread {
                    setInProgress(false)
                    response.body()?.first()?.let {
                        setUI(it)
                    }
                }
            }catch (e : Exception){
                runOnUiThread{
                    setInProgress(false)
                    Toast.makeText(applicationContext,"Something went wrong, it could be the word doesnt exist",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setUI(response: theWord) {
        binding.wordTextview.text = response.word
        binding.phoneticTextview.text = response.phonetic
        adapter.updateNewData(response.meanings)
    }

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