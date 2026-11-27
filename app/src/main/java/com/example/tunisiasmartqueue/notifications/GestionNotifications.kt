package com.example.tunisiasmartqueue.notifications

import android.content.Context
import android.content.SharedPreferences
import com.example.tunisiasmartqueue.auth.GestionAuth
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Une notification = une « fiche » avec ces informations
data class Notif(
    val id: Long,
    val type: String,      // "success", "warning" ou "info"
    val title: String,
    val message: String,
    val time: String,
    val read: Boolean      // true = déjà lue
)

object GestionNotifications {

    // Nom du petit fichier où le téléphone garde les notifications
    private const val NOM_FICHIER = "tsq_notifs"

    // Ouvre le stockage du téléphone
    private fun stockage(context: Context): SharedPreferences {
        return context.getSharedPreferences(NOM_FICHIER, Context.MODE_PRIVATE)
    }

    // Chaque utilisateur a sa propre liste, repérée par son mail
    private fun cle(context: Context): String {
        return "notifs_" + GestionAuth.email(context)
    }

    // Retourne toutes les notifications (les plus récentes en premier)
    fun getAll(context: Context): List<Notif> {

        // Première ouverture : on crée 3 notifications de démonstration
        if (!stockage(context).contains(cle(context))) {
            val demo = mutableListOf<Notif>()
            demo.add(Notif(3, "success", "Bienvenue sur Tunisia Smart Queue",
                "Votre compte est prêt. Prenez votre premier ticket.", "Aujourd'hui", false))
            demo.add(Notif(2, "warning", "Temps d'attente élevé",
                "L'attente est plus longue que d'habitude dans certaines agences.", "Hier", false))
            demo.add(Notif(1, "info", "Astuce",
                "Suivez votre position en temps réel depuis l'onglet Suivi.", "Hier", true))
            sauvegarder(context, demo)
        }

        // Lire le texte JSON et le transformer en liste de fiches
        val texte = stockage(context).getString(cle(context), "[]") ?: "[]"
        val tableau = JSONArray(texte)

        val resultat = mutableListOf<Notif>()
        for (i in 0 until tableau.length()) {
            val objet = tableau.getJSONObject(i)
            resultat.add(depuisJson(objet))
        }
        return resultat
    }

    // Nombre de notifications non lues (pour le badge de la cloche)
    fun unreadCount(context: Context): Int {
        var total = 0
        for (notif in getAll(context)) {
            if (!notif.read) {
                total++
            }
        }
        return total
    }

    // Ajoute une nouvelle notification en haut de la liste
    fun add(context: Context, type: String, title: String, message: String) {
        val heure = SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE).format(Date())
        val nouvelle = Notif(System.currentTimeMillis(), type, title, message, heure, false)

        val liste = mutableListOf<Notif>()
        liste.add(nouvelle)                 // la nouvelle d'abord
        liste.addAll(getAll(context))       // puis les anciennes
        sauvegarder(context, liste)
    }

    // Marque UNE notification comme lue
    fun markRead(context: Context, id: Long) {
        val nouvelleListe = mutableListOf<Notif>()
        for (notif in getAll(context)) {
            if (notif.id == id) {
                nouvelleListe.add(notif.copy(read = true))
            } else {
                nouvelleListe.add(notif)
            }
        }
        sauvegarder(context, nouvelleListe)
    }

    // Marque TOUTES les notifications comme lues
    fun markAllRead(context: Context) {
        val nouvelleListe = mutableListOf<Notif>()
        for (notif in getAll(context)) {
            nouvelleListe.add(notif.copy(read = true))
        }
        sauvegarder(context, nouvelleListe)
    }

    // Enregistre la liste dans le téléphone
    private fun sauvegarder(context: Context, liste: List<Notif>) {
        val tableau = JSONArray()
        for (notif in liste) {
            tableau.put(versJson(notif))
        }
        val editeur = stockage(context).edit()
        editeur.putString(cle(context), tableau.toString())
        editeur.apply()
    }

    // Transforme une fiche Notif en JSON (pour la sauvegarde)
    private fun versJson(notif: Notif): JSONObject {
        val objet = JSONObject()
        objet.put("id", notif.id)
        objet.put("type", notif.type)
        objet.put("title", notif.title)
        objet.put("message", notif.message)
        objet.put("time", notif.time)
        objet.put("read", notif.read)
        return objet
    }

    // Transforme un JSON en fiche Notif (pour la lecture)
    private fun depuisJson(objet: JSONObject): Notif {
        return Notif(
            objet.getLong("id"),
            objet.getString("type"),
            objet.getString("title"),
            objet.getString("message"),
            objet.getString("time"),
            objet.getBoolean("read")
        )
    }
}