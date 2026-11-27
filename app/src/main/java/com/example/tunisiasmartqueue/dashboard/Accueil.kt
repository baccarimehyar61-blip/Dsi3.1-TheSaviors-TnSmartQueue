package com.example.tunisiasmartqueue.dashboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tunisiasmartqueue.MainActivity
import com.example.tunisiasmartqueue.R
import com.example.tunisiasmartqueue.api.ApiClient
import com.example.tunisiasmartqueue.api.TicketResponse
import com.example.tunisiasmartqueue.auth.GestionAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Accueil : Fragment(R.layout.ecran_accueil) {

    private var pageView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pageView = view

        val activite = requireActivity() as MainActivity

        view.findViewById<Button>(R.id.btnTakeTicket).setOnClickListener {
            activite.goTo(R.id.nav_take)
        }

        view.findViewById<Button>(R.id.btnEmptyTake).setOnClickListener {
            activite.goTo(R.id.nav_take)
        }

        view.findViewById<TextView>(R.id.tvTracking).setOnClickListener {
            activite.goTo(R.id.nav_tracking)
        }

        view.findViewById<TextView>(R.id.tvSeeAll).setOnClickListener {
            activite.goTo(R.id.nav_tickets)
        }

        afficherAccesRapide(view, activite)
    }

    // Actualiser les tickets chaque fois que l'accueil apparaît
    override fun onResume() {
        super.onResume()
        chargerTickets()
    }

    // Charger les tickets depuis le backend
    private fun chargerTickets() {

        val token = GestionAuth.getToken(requireContext())

        if (token.isNullOrEmpty()) {
            pageView?.let {
                afficherTicketActif(it, emptyList())
                afficherHistorique(it, emptyList())
            }
            return
        }

        ApiClient.ticketsApi.getTickets("Bearer $token")
            .enqueue(object : Callback<List<TicketResponse>> {

                override fun onResponse(
                    call: Call<List<TicketResponse>>,
                    response: Response<List<TicketResponse>>
                ) {
                    if (!isAdded) return

                    val view = pageView ?: return

                    if (response.isSuccessful) {

                        val tickets = response.body() ?: emptyList()

                        afficherTicketActif(view, tickets)
                        afficherHistorique(view, tickets)

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Erreur serveur : ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<TicketResponse>>,
                    t: Throwable
                ) {
                    if (!isAdded) return

                    Toast.makeText(
                        requireContext(),
                        "Erreur connexion : ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // Afficher le ticket actif
    private fun afficherTicketActif(
        view: View,
        tickets: List<TicketResponse>
    ) {

        val zoneTicket = view.findViewById<View>(R.id.layoutActive)
        val zoneVide = view.findViewById<View>(R.id.layoutEmpty)
        val statut = view.findViewById<TextView>(R.id.tvStatus)

        // Chercher le dernier ticket qui n'est pas terminé
        val ticket = tickets
            .sortedByDescending { it.createdAt }
            .firstOrNull {
                it.status == "EN_ATTENTE" ||
                        it.status == "APPELE" ||
                        it.status == "EN_COURS"
            }

        // Aucun ticket actif
        if (ticket == null) {

            zoneTicket.visibility = View.GONE
            zoneVide.visibility = View.VISIBLE
            statut.visibility = View.GONE

            return
        }

        // Afficher le ticket
        zoneTicket.visibility = View.VISIBLE
        zoneVide.visibility = View.GONE
        statut.visibility = View.VISIBLE

        statut.text = afficherStatut(ticket.status)

        view.findViewById<TextView>(R.id.tvTicketNumber).text =
            ticket.ticketNumber

        view.findViewById<TextView>(R.id.tvAgency).text =
            "${ticket.agencyName} — ${ticket.agencyAddress}"

        view.findViewById<TextView>(R.id.tvService).text =
            ticket.serviceName

        view.findViewById<TextView>(R.id.tvWaiting).text =
            "${ticket.estimatedTime} min"

        // Le backend ne fournit pas encore le nombre de personnes devant vous
        view.findViewById<TextView>(R.id.tvPeopleAhead).text = "—"

        // Le backend ne fournit pas encore le pourcentage de progression
        view.findViewById<TextView>(R.id.tvProgress).text = "Non disponible"

        view.findViewById<ProgressBar>(R.id.progressBar).visibility =
            View.GONE

        // Couleur du statut
        appliquerCouleurStatut(statut, ticket.status)
    }

    // Afficher les trois derniers tickets
    private fun afficherHistorique(
        view: View,
        tickets: List<TicketResponse>
    ) {

        val conteneur = view.findViewById<LinearLayout>(
            R.id.historyContainer
        )

        val texteVide = view.findViewById<TextView>(
            R.id.tvEmptyHistory
        )

        conteneur.removeAllViews()

        val derniersTickets = tickets
            .sortedByDescending { it.createdAt }
            .take(3)

        texteVide.visibility =
            if (derniersTickets.isEmpty()) View.VISIBLE else View.GONE

        for (ticket in derniersTickets) {

            val ligne = LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.ligne_historique,
                    conteneur,
                    false
                )

            ligne.findViewById<TextView>(R.id.tvHistNumber).text =
                ticket.ticketNumber

            ligne.findViewById<TextView>(R.id.tvHistService).text =
                ticket.serviceName

            ligne.findViewById<TextView>(R.id.tvHistDate).text =
                formaterDate(ticket.createdAt)

            val statut = ligne.findViewById<TextView>(
                R.id.tvHistStatus
            )

            statut.text = afficherStatut(ticket.status)

            appliquerCouleurStatut(statut, ticket.status)

            conteneur.addView(ligne)
        }
    }

    // Traduire le statut reçu du backend
    private fun afficherStatut(status: String): String {

        return when (status) {
            "EN_ATTENTE" -> "En attente"
            "APPELE" -> "Appelé"
            "EN_COURS" -> "En cours"
            "TERMINE" -> "Terminé"
            "ANNULE" -> "Annulé"
            "ABSENT" -> "Absent"
            else -> status
        }
    }

    // Couleur du statut
    private fun appliquerCouleurStatut(
        texte: TextView,
        status: String
    ) {

        val fond: String
        val couleur: String

        when (status) {

            "TERMINE" -> {
                fond = "#F0F0F0"
                couleur = "#333333"
            }

            "ANNULE", "ABSENT" -> {
                fond = "#FFF0F1"
                couleur = "#E32636"
            }

            "APPELE", "EN_COURS" -> {
                fond = "#E8F0FE"
                couleur = "#1D4ED8"
            }

            else -> {
                fond = "#FFF3E0"
                couleur = "#B45309"
            }
        }

        texte.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(fond))

        texte.setTextColor(Color.parseColor(couleur))
    }

    // Transformer la date du backend en date lisible
    private fun formaterDate(date: String): String {

        val jour = date.substringBefore("T")

        if (jour.length < 10) return date

        return "${jour.substring(8, 10)}/" +
                "${jour.substring(5, 7)}/" +
                jour.substring(0, 4)
    }

    // Les accès rapides
    private fun afficherAccesRapide(
        view: View,
        activite: MainActivity
    ) {

        val conteneur = view.findViewById<LinearLayout>(
            R.id.quickContainer
        )

        conteneur.removeAllViews()

        ajouterRaccourci(
            conteneur,
            R.drawable.ic_add,
            "Prendre un ticket",
            "Choisir une agence et un service"
        ) {
            activite.goTo(R.id.nav_take)
        }

        ajouterRaccourci(
            conteneur,
            R.drawable.ic_list,
            "Mes tickets",
            "Consulter toutes vos réservations"
        ) {
            activite.goTo(R.id.nav_tickets)
        }

        ajouterRaccourci(
            conteneur,
            R.drawable.ic_clock,
            "Suivi de la file",
            "Suivre votre position et le temps d'attente"
        ) {
            activite.goTo(R.id.nav_tracking)
        }

        ajouterRaccourci(
            conteneur,
            R.drawable.ic_bell,
            "Notifications",
            "Consulter vos dernières notifications"
        ) {
            activite.openNotifications()
        }

        ajouterRaccourci(
            conteneur,
            R.drawable.ic_person,
            "Mon profil",
            "Gérer vos informations personnelles"
        ) {
            activite.goTo(R.id.nav_profile)
        }
    }

    // Ajouter une ligne d'accès rapide
    private fun ajouterRaccourci(
        conteneur: LinearLayout,
        icone: Int,
        titre: String,
        sousTitre: String,
        action: () -> Unit
    ) {

        val ligne = LayoutInflater.from(requireContext())
            .inflate(
                R.layout.ligne_acces_rapide,
                conteneur,
                false
            )

        ligne.findViewById<ImageView>(R.id.ivQuickIcon)
            .setImageResource(icone)

        ligne.findViewById<TextView>(R.id.tvQuickTitle).text =
            titre

        ligne.findViewById<TextView>(R.id.tvQuickSubtitle).text =
            sousTitre

        ligne.setOnClickListener {
            action()
        }

        conteneur.addView(ligne)
    }

    override fun onDestroyView() {
        pageView = null
        super.onDestroyView()
    }
}