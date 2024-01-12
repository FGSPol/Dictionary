package fr.isep.dictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        val dictionarybtn: Button = findViewById(R.id.dictionarybtn)
        val quizbtn: Button = findViewById(R.id.quizbtn)


        // Set click listeners for the buttons
        dictionarybtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent);
        }

        quizbtn.setOnClickListener {
            // Handle Button 2 click
        }
    }
}