package fr.isep.dictionary

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import fr.isep.dictionary.models.theWord
import fr.isep.dictionary.util.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import androidx.appcompat.app.AlertDialog

class DefinitionGame : AppCompatActivity() {

    lateinit var context: Context
    lateinit var textViewDefinition: TextView
    lateinit var buttonOption1: Button
    lateinit var buttonOption2: Button
    lateinit var buttonOption3: Button

    private var wordList = emptyList<String>()
    private var randomWord1: String? = null
    private var randomWord2: String? = null
    private var randomWord3: String? = null
    private var correctWord: String? = null
    public var currentScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_definition_game)

        context = this
        textViewDefinition = findViewById(R.id.textViewDefinition)
        buttonOption1 = findViewById(R.id.buttonOption1)
        buttonOption2 = findViewById(R.id.buttonOption2)
        buttonOption3 = findViewById(R.id.buttonOption3)

        // Load words for the buttons
        loadWords()

        buttonOption1.setOnClickListener { checkAnswer(buttonOption1) }
        buttonOption2.setOnClickListener { checkAnswer(buttonOption2) }
        buttonOption3.setOnClickListener { checkAnswer(buttonOption3) }

        // Display the definition for a random word among randomWord1, randomWord2, and randomWord3
        displayRandomDefinition()
    }

    private fun loadWords() {
        wordList = readWordList("Words.txt")
        randomWord1 = getRandomWord(wordList)
        randomWord2 = getRandomWord(wordList)
        randomWord3 = getRandomWord(wordList)

        // Ensure all the 3 words are dinstincts
        while (randomWord1 == randomWord2 || randomWord1 == randomWord3 || randomWord2 == randomWord3) {
            randomWord1 = getRandomWord(wordList)
            randomWord2 = getRandomWord(wordList)
            randomWord3 = getRandomWord(wordList)
        }

        // Set text on the 3 buttons
        buttonOption1.text = randomWord1
        buttonOption2.text = randomWord2
        buttonOption3.text = randomWord3
    }

    // Function to be able to read the wordList
    private fun readWordList(fileName: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val inputStream = assets.open(fileName)
            inputStream.bufferedReader().useLines { lines ->
                list.addAll(lines.toList())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return list
    }

    // Function to pick a random word through the wordList
    private fun getRandomWord(wordList: List<String>): String? {
        return if (wordList.isNotEmpty()) {
            val randomIndex = (0 until wordList.size).random()
            wordList[randomIndex]
        } else {
            null
        }
    }

    // Function to build the definitions of the word into a displayable String
    private fun buildDefinitionText(wordDetails: theWord): CharSequence {
        // Extract all definitions into a single string
        val definitionsText = wordDetails.meanings
            .flatMap { it.definitions }
            .mapIndexed { index, definition ->
                "${index + 1}. ${definition.definition}${System.lineSeparator()}${System.lineSeparator()}" // Add 2 lineSeparators to make it readable
            }
            .joinToString("")

        return "Definitions:${System.lineSeparator()}$definitionsText"
    }

    // Display one definition among the 3 words
    private fun displayRandomDefinition() {
        correctWord = listOf(randomWord1, randomWord2, randomWord3).random()
        if (correctWord != null) {
            GlobalScope.launch(Dispatchers.Main) {
                val definition = fetchDefinition(correctWord!!)
                textViewDefinition.text = buildDefinitionText(definition)
            }
        }
    }

    // Check if the answer is good or not
    private fun checkAnswer(button: Button) {
        val selectedWord = button.text.toString()
        if (selectedWord == correctWord) {
            currentScore++
            showPopup(true, currentScore)
        } else {
            currentScore = 0
            showPopup(false, currentScore)
        }

        restartGame()
    }

    // Restart a new set of words after getting the answers of the precedent words
    private fun restartGame() {
        loadWords()
        displayRandomDefinition()
    }

    // Display the word's definition using coroutines
    suspend fun fetchDefinition(word: String): theWord {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<List<theWord>> = RetrofitInstance.dictionaryApi.getMeaning(word)

                if (response.isSuccessful && response.body() != null) {
                    val wordDetails = response.body()!!.firstOrNull()

                    // Only consider the definitions of the first meaning if available
                    val meanings = wordDetails?.meanings?.take(1) ?: emptyList()

                    return@withContext theWord(
                        wordDetails?.word ?: "",
                        wordDetails?.phonetic ?: null,
                        meanings
                    )
                } else {
                    // Handle error
                    return@withContext theWord("", null, emptyList())
                }
            } catch (e: Exception) {
                // Handle exception
                return@withContext theWord("", null, emptyList())
            }
        }
    }

    // Showing the popup window when you played the game
    private fun showPopup(isCorrect: Boolean, score: Int) {
        val dialogView = layoutInflater.inflate(R.layout.popup_layout, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.imageView)
        val textViewMessage = dialogView.findViewById<TextView>(R.id.textViewMessage)

        // Good answer
        if (isCorrect) {
            imageView.setImageResource(R.drawable.trophy) // Set your happy face image
            textViewMessage.text = "Congratulations! Good answer! Streak : $score"
            dialogView.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        }
        // Bad answer
        else {
            imageView.setImageResource(R.drawable.sadface) // Set your sad face image
            textViewMessage.text = "Sadly, it's not the correct answer. Streak : 0"
            dialogView.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
        }

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.show()
        // Give a maximum time for the window to close
        Handler().postDelayed({ alertDialog.dismiss() }, 2000)
    }
}
