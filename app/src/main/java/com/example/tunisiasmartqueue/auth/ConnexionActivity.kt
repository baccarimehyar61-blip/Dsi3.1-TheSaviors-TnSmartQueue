
package com.example.tunisiasmartqueue.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tunisiasmartqueue.MainActivity
import com.example.tunisiasmartqueue.R
import com.example.tunisiasmartqueue.api.ApiClient
import com.example.tunisiasmartqueue.api.LoginRequest
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ConnexionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si l'utilisateur est déjà connecté, aller à l'accueil
        if (GestionAuth.isLoggedIn(this)) {
            ouvrirAccueil()
            return
        }

        setContentView(R.layout.ecran_connexion)

        // Retrouver les éléments de l'écran
        val champEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val champMotDePasse = findViewById<TextInputEditText>(R.id.etPassword)
        val cadreEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val cadreMotDePasse = findViewById<TextInputLayout>(R.id.tilPassword)
        val messageErreur = findViewById<TextView>(R.id.tvError)
        val boutonConnexion = findViewById<Button>(R.id.btnLogin)
        val lienInscription = findViewById<TextView>(R.id.tvRegister)
        val lienOublie = findViewById<TextView>(R.id.tvForgot)

        // Clic sur le bouton Connexion
        boutonConnexion.setOnClickListener {

            val email = champEmail.text.toString().trim()
            val motDePasse = champMotDePasse.text.toString()

            // Effacer les anciennes erreurs
            messageErreur.visibility = View.GONE
            cadreEmail.error = null
            cadreMotDePasse.error = null

            // Vérifier l'e-mail
            val emailValide =
                Patterns.EMAIL_ADDRESS.matcher(email).matches()

            if (!emailValide) {
                cadreEmail.error = "Adresse e-mail invalide"
                return@setOnClickListener
            }

            // Vérifier le mot de passe
            if (motDePasse.isEmpty()) {
                cadreMotDePasse.error = "Le mot de passe est obligatoire"
                return@setOnClickListener
            }

            // Désactiver le bouton pendant la connexion
            boutonConnexion.isEnabled = false

            // Envoyer la demande au backend
            lifecycleScope.launch {
                try {
                    val reponse = ApiClient.authApi.login(
                        LoginRequest(email, motDePasse)
                    )

                    if (reponse.isSuccessful) {

                        val resultat = reponse.body()

                        if (resultat != null) {
                            // Enregistrer le token et les informations utilisateur
                            GestionAuth.sauvegarderSession(
                                this@ConnexionActivity,
                                resultat
                            )

                            // Ouvrir l'accueil
                            ouvrirAccueil()

                        } else {
                            messageErreur.text = "Réponse vide du serveur"
                            messageErreur.visibility = View.VISIBLE
                        }

                    } else {

                        // Erreur renvoyée par le backend
                        messageErreur.text = when (reponse.code()) {
                            401 -> "E-mail ou mot de passe incorrect."
                            404 -> "Service de connexion introuvable."
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
                    boutonConnexion.isEnabled = true
                }
            }
        }

        // Lien Créer un compte
        lienInscription.setOnClickListener {
            startActivity(Intent(this, InscriptionActivity::class.java))
        }

        // Lien Mot de passe oublié
        lienOublie.setOnClickListener {
            startActivity(Intent(this, MotDePasseOublieActivity::class.java))
        }
    }

    // Ouvrir l'écran principal
    private fun ouvrirAccueil() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
