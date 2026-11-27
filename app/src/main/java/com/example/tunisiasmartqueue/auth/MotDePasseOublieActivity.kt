package com.example.tunisiasmartqueue.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tunisiasmartqueue.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MotDePasseOublieActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Afficher le design de la page (fichier activity_forgot.xml)
        setContentView(R.layout.ecran_mot_de_passe_oublie)

        // 1) Retrouver les éléments de l'écran grâce à leur id
        val champEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val cadreEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val messageSucces = findViewById<TextView>(R.id.tvSuccess)
        val boutonEnvoyer = findViewById<Button>(R.id.btnSend)
        val lienRetour = findViewById<TextView>(R.id.tvBack)

        // 2) Quand on clique sur le bouton « Envoyer »
        boutonEnvoyer.setOnClickListener {
            val email = champEmail.text.toString().trim()
            cadreEmail.error = null   // efface l'ancienne erreur

            // Vérifier que l'e-mail a un format correct
            val emailValide = Patterns.EMAIL_ADDRESS.matcher(email).matches()
            if (!emailValide) {
                cadreEmail.error = "Adresse e-mail invalide"
                return@setOnClickListener   // on s'arrête ici
            }

            // E-mail correct : afficher le message et bloquer le bouton
            messageSucces.visibility = View.VISIBLE
            boutonEnvoyer.isEnabled = false
        }

        // 3) Le lien « Retour » ferme la page
        lienRetour.setOnClickListener {
            finish()
        }
    }
}