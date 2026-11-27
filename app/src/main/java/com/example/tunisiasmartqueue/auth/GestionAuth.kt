
package com.example.tunisiasmartqueue.auth

import android.content.Context
import com.example.tunisiasmartqueue.api.AuthResponse

object GestionAuth {

    // Nom du stockage de la session
    private const val nf = "tsq_prefs"

    // Ouvre le stockage du téléphone
    private fun stockage(context: Context) =
        context.getSharedPreferences(nf, Context.MODE_PRIVATE)

    // Enregistre les informations reçues du backend
    fun sauvegarderSession(context: Context, response: AuthResponse) {
        val editeur = stockage(context).edit()

        editeur.putString("token", response.token)
        editeur.putString("session_prenom", response.user.firstName)
        editeur.putString("session_nom", response.user.lastName)
        editeur.putString("session_email", response.user.email)
        editeur.putString("session_role", response.user.role)

        editeur.apply()
    }

    // Vérifie si un utilisateur est connecté
    fun isLoggedIn(context: Context): Boolean {
        return !stockage(context).getString("token", null).isNullOrBlank()
    }

    // Récupère le token JWT
    fun getToken(context: Context): String? {
        return stockage(context).getString("token", null)
    }

    // Prénom de l'utilisateur connecté
    fun prenom(context: Context): String {
        return stockage(context).getString("session_prenom", "") ?: ""
    }

    // E-mail de l'utilisateur connecté
    fun email(context: Context): String {
        return stockage(context).getString("session_email", "") ?: ""
    }

    // Déconnexion
    fun logout(context: Context) {
        stockage(context).edit().clear().apply()
    }
}
