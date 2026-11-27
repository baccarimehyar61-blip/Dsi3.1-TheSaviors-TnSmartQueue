package com.example.tunisiasmartqueue.tickets

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tunisiasmartqueue.MainActivity
import com.example.tunisiasmartqueue.R

class Suivi : Fragment(R.layout.ecran_suivi) {

    // S'exécute quand l'écran est prêt
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activite = requireActivity() as MainActivity

        // 1) Retrouver les éléments de l'écran
        val carteSuivi = view.findViewById<View>(R.id.cardTrack)
        val zoneVide = view.findViewById<View>(R.id.layoutTrEmpty)

        // 2) Les boutons
        view.findViewById<Button>(R.id.btnTrEmptyNew).setOnClickListener {
            activite.goTo(R.id.nav_take)
        }
        view.findViewById<Button>(R.id.btnTrNew).setOnClickListener {
            activite.goTo(R.id.nav_take)
        }
        view.findViewById<Button>(R.id.btnTrTickets).setOnClickListener {
            activite.goTo(R.id.nav_tickets)
        }

        // 3) Chercher le ticket actif
        val ticket = GestionTickets.getActiveTicket(requireContext())

        // Pas de ticket actif : on affiche « Aucun ticket actif » et on s'arrête
        if (ticket == null) {
            carteSuivi.visibility = View.GONE
            zoneVide.visibility = View.VISIBLE
            return
        }

        // 4) Il y a un ticket actif : on affiche la carte de suivi
        carteSuivi.visibility = View.VISIBLE
        zoneVide.visibility = View.GONE

        view.findViewById<TextView>(R.id.tvTrStatus).text = ticket.status
        view.findViewById<TextView>(R.id.tvTrNumber).text = ticket.number
        view.findViewById<TextView>(R.id.tvTrEst).text = ticket.establishment
        view.findViewById<TextView>(R.id.tvTrAgency).text = ticket.agency
        view.findViewById<TextView>(R.id.tvTrService).text = ticket.service
        view.findViewById<TextView>(R.id.tvTrAhead).text = ticket.peopleAhead.toString()
        view.findViewById<TextView>(R.id.tvTrTime).text = "${ticket.waiting} min"

        // La progression doit rester entre 0 et 100
        val progression = ticket.progress.coerceIn(0, 100)
        view.findViewById<TextView>(R.id.tvTrProgress).text = "$progression%"
        view.findViewById<ProgressBar>(R.id.pbTrack).progress = progression

        // 5) Le message change selon le nombre de personnes devant
        val message: String
        if (ticket.peopleAhead <= 2) {
            message = "C'est bientôt votre tour ! Restez à proximité de l'agence."
        } else {
            message = "Votre tour approche. Vous serez informé quand ce sera à vous."
        }
        view.findViewById<TextView>(R.id.tvTrMessage).text = message
    }
}