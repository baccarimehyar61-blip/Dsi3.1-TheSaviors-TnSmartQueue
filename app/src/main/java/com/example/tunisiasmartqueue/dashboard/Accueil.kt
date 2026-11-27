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
import androidx.fragment.app.Fragment
import com.example.tunisiasmartqueue.MainActivity
import com.example.tunisiasmartqueue.R
import com.example.tunisiasmartqueue.tickets.GestionTickets

class Accueil : Fragment(R.layout.ecran_accueil) {

    // S'exécute quand l'écran est prêt
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // L'activité principale permet de changer d'onglet
        val activite = requireActivity() as MainActivity

        // Les boutons et liens de la page
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

        // Remplir les 3 parties de la page
        afficherTicketActif(view)
        afficherAccesRapide(view, activite)
        afficherHistorique(view)
    }

    // ---------- Partie 1 : le ticket actif ----------
    private fun afficherTicketActif(view: View) {
        val ticket = GestionTickets.getActiveTicket(requireContext())

        val zoneTicket = view.findViewById<View>(R.id.layoutActive)
        val zoneVide = view.findViewById<View>(R.id.layoutEmpty)
        val statut = view.findViewById<TextView>(R.id.tvStatus)

        // Pas de ticket actif : on affiche le message « Aucun ticket »
        if (ticket == null) {
            zoneTicket.visibility = View.GONE
            zoneVide.visibility = View.VISIBLE
            statut.visibility = View.GONE
            return
        }

        // Il y a un ticket actif : on affiche ses informations
        zoneTicket.visibility = View.VISIBLE
        zoneVide.visibility = View.GONE
        statut.visibility = View.VISIBLE
        statut.text = ticket.status

        view.findViewById<TextView>(R.id.tvTicketNumber).text = ticket.number
        view.findViewById<TextView>(R.id.tvAgency).text = ticket.agency
        view.findViewById<TextView>(R.id.tvService).text = ticket.service
        view.findViewById<TextView>(R.id.tvPeopleAhead).text = ticket.peopleAhead.toString()
        view.findViewById<TextView>(R.id.tvWaiting).text = "${ticket.waiting} min"

        // La progression doit rester entre 0 et 100
        val progression = ticket.progress.coerceIn(0, 100)
        view.findViewById<TextView>(R.id.tvProgress).text = "$progression%"
        view.findViewById<ProgressBar>(R.id.progressBar).progress = progression
    }

    // ---------- Partie 2 : les accès rapides ----------
    private fun afficherAccesRapide(view: View, activite: MainActivity) {
        val conteneur = view.findViewById<LinearLayout>(R.id.quickContainer)

        ajouterRaccourci(conteneur, R.drawable.ic_add,
            "Prendre un ticket", "Choisir une agence et un service") {
            activite.goTo(R.id.nav_take)
        }
        ajouterRaccourci(conteneur, R.drawable.ic_list,
            "Mes tickets", "Consulter toutes vos réservations") {
            activite.goTo(R.id.nav_tickets)
        }
        ajouterRaccourci(conteneur, R.drawable.ic_clock,
            "Suivi de la file", "Suivre votre position et le temps d'attente") {
            activite.goTo(R.id.nav_tracking)
        }
        ajouterRaccourci(conteneur, R.drawable.ic_bell,
            "Notifications", "Consulter vos dernières notifications") {
            activite.openNotifications()
        }
        ajouterRaccourci(conteneur, R.drawable.ic_person,
            "Mon profil", "Gérer vos informations personnelles") {
            activite.goTo(R.id.nav_profile)
        }
    }

    // Crée UNE ligne d'accès rapide et l'ajoute dans la liste
    private fun ajouterRaccourci(
        conteneur: LinearLayout,
        icone: Int,
        titre: String,
        sousTitre: String,
        action: () -> Unit
    ) {
        val ligne = LayoutInflater.from(requireContext())
            .inflate(R.layout.ligne_acces_rapide, conteneur, false)

        ligne.findViewById<ImageView>(R.id.ivQuickIcon).setImageResource(icone)
        ligne.findViewById<TextView>(R.id.tvQuickTitle).text = titre
        ligne.findViewById<TextView>(R.id.tvQuickSubtitle).text = sousTitre
        ligne.setOnClickListener { action() }

        conteneur.addView(ligne)
    }

    // ---------- Partie 3 : l'historique (3 derniers tickets) ----------
    private fun afficherHistorique(view: View) {
        val conteneur = view.findViewById<LinearLayout>(R.id.historyContainer)
        val texteVide = view.findViewById<TextView>(R.id.tvEmptyHistory)
        val tickets = GestionTickets.getTickets(requireContext()).take(3)

        if (tickets.isEmpty()) {
            texteVide.visibility = View.VISIBLE
        } else {
            texteVide.visibility = View.GONE
        }

        // Une ligne pour chaque ticket
        for (ticket in tickets) {
            val ligne = LayoutInflater.from(requireContext())
                .inflate(R.layout.ligne_historique, conteneur, false)

            ligne.findViewById<TextView>(R.id.tvHistNumber).text = ticket.number
            ligne.findViewById<TextView>(R.id.tvHistService).text = ticket.service
            ligne.findViewById<TextView>(R.id.tvHistDate).text = ticket.date

            val statut = ligne.findViewById<TextView>(R.id.tvHistStatus)
            statut.text = ticket.status

            // Couleurs selon le statut (orange par défaut = « En attente »)
            var couleurFond = "#FFF3E0"
            var couleurTexte = "#B45309"
            if (ticket.status == "Terminé") {
                couleurFond = "#F0F0F0"
                couleurTexte = "#333333"
            }
            if (ticket.status == "Annulé") {
                couleurFond = "#FFF0F1"
                couleurTexte = "#E32636"
            }
            statut.backgroundTintList = ColorStateList.valueOf(Color.parseColor(couleurFond))
            statut.setTextColor(Color.parseColor(couleurTexte))

            conteneur.addView(ligne)
        }
    }
}