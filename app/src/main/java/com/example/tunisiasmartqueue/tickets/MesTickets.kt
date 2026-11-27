
package com.example.tunisiasmartqueue.tickets

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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

class MesTickets : Fragment(R.layout.ecran_mes_tickets) {

    private lateinit var conteneur: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var tvWaiting: TextView
    private lateinit var tvDone: TextView
    private lateinit var layoutEmpty: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        conteneur = view.findViewById(R.id.ticketsContainer)
        tvTotal = view.findViewById(R.id.tvCountTotal)
        tvWaiting = view.findViewById(R.id.tvCountWaiting)
        tvDone = view.findViewById(R.id.tvCountDone)
        layoutEmpty = view.findViewById(R.id.layoutEmptyTickets)

        view.findViewById<Button>(R.id.btnNewTicket).setOnClickListener {
            (requireActivity() as MainActivity).goTo(R.id.nav_take)
        }

        chargerTickets()
    }

    private fun chargerTickets() {
        val token = GestionAuth.token(requireContext())

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Session expirée. Reconnectez-vous.", Toast.LENGTH_LONG).show()
            return
        }

        ApiClient.ticketsApi.getTickets("Bearer $token")
            .enqueue(object : Callback<List<TicketResponse>> {

                override fun onResponse(
                    call: Call<List<TicketResponse>>,
                    response: Response<List<TicketResponse>>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful) {
                        afficherTickets(response.body() ?: emptyList())
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

    private fun afficherTickets(tickets: List<TicketResponse>) {
        conteneur.removeAllViews()

        tvTotal.text = tickets.size.toString()
        tvWaiting.text = tickets.count {
            it.status == "EN_ATTENTE"
        }.toString()
        tvDone.text = tickets.count {
            it.status == "TERMINE"
        }.toString()

        layoutEmpty.visibility =
            if (tickets.isEmpty()) View.VISIBLE else View.GONE

        for (ticket in tickets) {
            val carte = layoutInflater.inflate(
                R.layout.ligne_ticket,
                conteneur,
                false
            )

            carte.findViewById<TextView>(R.id.tvTNumber).text =
                ticket.ticketNumber

            carte.findViewById<TextView>(R.id.tvTService).text =
                ticket.serviceName

            carte.findViewById<TextView>(R.id.tvTAgency).text =
                "${ticket.agencyName} — ${ticket.agencyAddress}"

            carte.findViewById<TextView>(R.id.tvTInfo).text =
                "Temps estimé : ${ticket.estimatedTime} min"

            carte.findViewById<TextView>(R.id.tvTDate).text =
                ticket.createdAt.replace("T", " à ")

            val statut = carte.findViewById<TextView>(R.id.tvTStatus)
            statut.text = ticket.status

            val lienSuivi = carte.findViewById<TextView>(R.id.tvTFollow)
            lienSuivi.visibility =
                if (ticket.status == "EN_ATTENTE") View.VISIBLE else View.GONE

            if (ticket.status == "EN_ATTENTE") {
                lienSuivi.setOnClickListener {
                    (requireActivity() as MainActivity).goTo(R.id.nav_tracking)
                }
            }

            conteneur.addView(carte)
        }
    }
}
