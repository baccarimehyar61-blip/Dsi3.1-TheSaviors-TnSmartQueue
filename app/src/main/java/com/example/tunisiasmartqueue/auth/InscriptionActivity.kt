
package com.example.tunisiasmartqueue.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tunisiasmartqueue.MainActivity
import com.example.tunisiasmartqueue.R
import com.example.tunisiasmartqueue.api.ApiClient
import com.example.tunisiasmartqueue.api.RegisterRequest
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class InscriptionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Afficher le design de la page
        setContentView(R.layout.ecran_inscription)

        // 1) Retrouver les champs
        val champNom = findViewById<TextInputEditText>(R.id.etNom)
        val champPrenom = findViewById<TextInputEditText>(R.id.etPrenom)
        val champEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val champTelephone = findViewById<TextInputEditText>(R.id.etPhone)
        val champMotDePasse = findViewById<TextInputEditText>(R.id.etPassword)
        val champConfirmation = findViewById<TextInputEditText>(R.id.etConfirm)

        // 2) Retrouver les cadres d'erreur
        val cadreEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val cadreTelephone = findViewById<TextInputLayout>(R.id.tilPhone)
        val cadreMotDePasse = findViewById<TextInputLayout>(R.id.tilPassword)
        val cadreConfirmation = findViewById<TextInputLayout>(R.id.tilConfirm)
        val messageErreur = findViewById<TextView>(R.id.tvError)

        val boutonInscription = findViewById<Button>(R.id.btnRegister)
        val lienConnexion = findViewById<TextView>(R.id.tvLogin)

        // 3) Clic sur le bouton S'inscrire
        boutonInscription.setOnClickListener {

            // Lire les informations saisies
            val nom = champNom.text.toString().trim()
            val prenom = champPrenom.text.toString().trim()
            val email = champEmail.text.toString().trim()
            val telephone = champTelephone.text.toString().replace(" ", "")
            val motDePasse = champMotDePasse.text.toString()
            val confirmation = champConfirmation.text.toString()

            // Effacer les anciennes erreurs
            messageErreur.visibility = View.GONE
            cadreEmail.error = null
            cadreTelephone.error = null
            cadreMotDePasse.error = null
            cadreConfirmation.error = null

            // Vérifier le nom et le prénom
            if (nom.length < 2 || prenom.length < 2) {
                messageErreur.text = "Le nom et le prénom sont obligatoires."
                messageErreur.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Vérifier l'e-mail
            val emailValide =
                Patterns.EMAIL_ADDRESS.matcher(email).matches()

            if (!emailValide) {
                cadreEmail.error = "Adresse e-mail invalide"
                return@setOnClickListener
            }

            // Vérifier le téléphone tunisien
            val modeleTelephone = Regex("^(\\+216)?[2-579]\\d{7}$")

            if (!modeleTelephone.matches(telephone)) {
                cadreTelephone.error = "Numéro invalide (8 chiffres)"
                return@setOnClickListener
            }

            // Vérifier le mot de passe
            if (motDePasse.length < 8) {
                cadreMotDePasse.error = "8 caractères minimum"
                return@setOnClickListener
            }

            // Vérifier la confirmation
            if (motDePasse != confirmation) {
                cadreConfirmation.error =
                    "Les mots de passe ne correspondent pas"
                return@setOnClickListener
            }

            // Désactiver le bouton pendant l'inscription
            boutonInscription.isEnabled = false

            // Envoyer les informations au backend
            lifecycleScope.launch {
                try {
                    val demande = RegisterRequest(
                        firstName = prenom,
                        lastName = nom,
                        email = email,
                        phone = telephone,
                        password = motDePasse
                    )

                    val reponse = ApiClient.authApi.register(demande)

                    if (reponse.isSuccessful) {

                        val resultat = reponse.body()

                        if (resultat != null) {

                            // Enregistrer le token et les informations utilisateur
                            GestionAuth.sauvegarderSession(
                                this@InscriptionActivity,
                                resultat
                            )

                            Toast.makeText(
                                this@InscriptionActivity,
                                "Compte créé avec succès !",
                                Toast.LENGTH_LONG
                            ).show()

                            // Ouvrir l'accueil
                            startActivity(
                                Intent(this@InscriptionActivity, MainActivity::class.java)
                            )
                            finish()

                        } else {
                            messageErreur.text = "Réponse vide du serveur"
                            messageErreur.visibility = View.VISIBLE
                        }

                    } else {

                        // Afficher l'erreur du backend
                        messageErreur.text = when (reponse.code()) {
                            400 -> "Certaines informations sont invalides."
                            409 -> "Cet e-mail est déjà utilisé."
                            else -> "Erreur du serveur (${reponse.code()})."
                        }

                        messageErreur.visibility = View.VISIBLE
                    }

                } catch (e: Exception) {
                    messageErreur.text =
                        "Erreur : ${e.javaClass.simpleName} - ${e.message}"

                    messageErreur.visibility = View.VISIBLE
                    e.printStackTrace()
                } finally {
                    boutonInscription.isEnabled = true
                }
            }
        }

        // 4) Lien Se connecter
        lienConnexion.setOnClickListener {
            finish()
        }
    }
}
