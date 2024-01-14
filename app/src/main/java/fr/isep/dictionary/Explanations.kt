package fr.isep.dictionary

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Explanations : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.explanations)

        val nothanksbutton: Button = findViewById(R.id.buttonNoThanks)
        val letsgobutton: Button = findViewById(R.id.buttonLetsGo)

        nothanksbutton.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        letsgobutton.setOnClickListener {
            val intent = Intent(this, DefinitionGame::class.java)
            startActivity(intent)
        }

    }

}