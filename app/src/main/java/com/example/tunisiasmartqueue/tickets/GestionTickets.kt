
package com.example.tunisiasmartqueue.tickets

import android.content.Context
import android.content.SharedPreferences
import com.example.tunisiasmartqueue.auth.GestionAuth
import org.json.JSONArray
import org.json.JSONObject

// Informations d'un ticket
data class Ticket(
    val number: String,
    val establishment: String,
    val service: String,
    val agency: String,
    val address: String,
    val waiting: Int,
    val peopleAhead: Int,
    val status: String,
    val progress: Int,
    val date: String,
    val time: String,
    val createdAt: Long
)

object GestionTickets {

    private const val NOM_FICHIER = "tsq_tickets"

    // Ouvrir le stockage
    private fun stockage(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            NOM_FICHIER,
            Context.MODE_PRIVATE
        )
    }

    // Chaque utilisateur possède ses tickets
    private fun cle(context: Context): String {
        return "tickets_" + GestionAuth.email(context)
    }

    // Récupérer tous les tickets
    fun getTickets(context: Context): List<Ticket> {

        val texte = stockage(context).getString(cle(context), "[]") ?: "[]"
        val tableau = JSONArray(texte)

        val tickets = mutableListOf<Ticket>()

        for (i in 0 until tableau.length()) {
            val objet = tableau.getJSONObject(i)
            tickets.add(depuisJson(objet))
        }

        return tickets
    }

    // Récupérer le ticket actif
    fun getActiveTicket(context: Context): Ticket? {

        for (ticket in getTickets(context)) {
            if (ticket.status == "En attente") {
                return ticket
            }
        }

        return null
    }

    // Enregistrer un ticket
    fun saveTicket(context: Context, ticket: Ticket) {

        val tickets = mutableListOf<Ticket>()

        tickets.add(ticket)
        tickets.addAll(getTickets(context))

        sauvegarder(context, tickets)
    }

    // Modifier le statut d'un ticket
    fun updateStatus(
        context: Context,
        number: String,
        newStatus: String
    ) {
        val tickets = getTickets(context).map { ticket ->

            if (ticket.number == number) {
                ticket.copy(status = newStatus)
            } else {
                ticket
            }
        }

        sauvegarder(context, tickets)
    }

    // Supprimer un ticket
    fun deleteTicket(context: Context, number: String) {

        val tickets = getTickets(context).filter {
            it.number != number
        }

        sauvegarder(context, tickets)
    }

    // Ajouter des tickets de démonstration
    fun seedDemo(context: Context) {

        if (getTickets(context).isNotEmpty()) {
            return
        }

        val tickets = listOf(

            Ticket(
                "A-025",
                "Municipalité",
                "Extrait de naissance",
                "Agence centre-ville",
                "Centre-ville",
                20,
                8,
                "En attente",
                35,
                "17/11/2026",
                "09:30",
                3L
            ),

            Ticket(
                "F-112",
                "La Poste",
                "Opérations postales",
                "Agence centre-ville",
                "Centre-ville",
                20,
                0,
                "Terminé",
                100,
                "12/11/2026",
                "10:05",
                2L
            ),

            Ticket(
                "G-208",
                "Recette des Finances",
                "Taxes",
                "Agence centre-ville",
                "Centre-ville",
                25,
                0,
                "Annulé",
                0,
                "05/11/2026",
                "14:20",
                1L
            )
        )

        sauvegarder(context, tickets)
    }

    // Sauvegarder la liste
    private fun sauvegarder(
        context: Context,
        tickets: List<Ticket>
    ) {

        val tableau = JSONArray()

        for (ticket in tickets) {
            tableau.put(versJson(ticket))
        }

        stockage(context)
            .edit()
            .putString(cle(context), tableau.toString())
            .apply()
    }

    // Ticket vers JSON
    private fun versJson(ticket: Ticket): JSONObject {

        val objet = JSONObject()

        objet.put("number", ticket.number)
        objet.put("establishment", ticket.establishment)
        objet.put("service", ticket.service)
        objet.put("agency", ticket.agency)
        objet.put("address", ticket.address)
        objet.put("waiting", ticket.waiting)
        objet.put("peopleAhead", ticket.peopleAhead)
        objet.put("status", ticket.status)
        objet.put("progress", ticket.progress)
        objet.put("date", ticket.date)
        objet.put("time", ticket.time)
        objet.put("createdAt", ticket.createdAt)

        return objet
    }

    // JSON vers Ticket
    private fun depuisJson(objet: JSONObject): Ticket {

        return Ticket(
            objet.getString("number"),
            objet.getString("establishment"),
            objet.getString("service"),
            objet.getString("agency"),
            objet.getString("address"),
            objet.getInt("waiting"),
            objet.getInt("peopleAhead"),
            objet.getString("status"),
            objet.getInt("progress"),
            objet.getString("date"),
            objet.getString("time"),
            objet.getLong("createdAt")
        )
    }
}
