package com.example.tunisiasmartqueue.tickets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tunisiasmartqueue.MainActivity
import com.example.tunisiasmartqueue.R
import com.example.tunisiasmartqueue.api.ApiClient
import com.example.tunisiasmartqueue.api.ServiceResponse
import com.example.tunisiasmartqueue.api.AgencyResponse
import com.example.tunisiasmartqueue.api.CreateTicketRequest
import com.example.tunisiasmartqueue.api.TicketResponse
import com.example.tunisiasmartqueue.auth.GestionAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PrendreTicket : Fragment(R.layout.ecran_prendre_ticket) {

    private var services = listOf<ServiceResponse>()
    private var etablissementChoisi: String? = null
    private var serviceChoisi: ServiceResponse? = null

    private var etape = 1

    private lateinit var zoneChoix: LinearLayout
    private lateinit var boutonRetour: TextView
    private lateinit var texteEtape: TextView
    private lateinit var texteTitre: TextView
    private lateinit var texteSousTitre: TextView
    private lateinit var carteConfirmation: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        zoneChoix = view.findViewById(R.id.choicesContainer)
        boutonRetour = view.findViewById(R.id.tvBack)
        texteEtape = view.findViewById(R.id.tvStepLabel)
        texteTitre = view.findViewById(R.id.tvStepTitle)
        texteSousTitre = view.findViewById(R.id.tvStepSub)
        carteConfirmation = view.findViewById(R.id.cardDone)

        boutonRetour.setOnClickListener {
            if (etape == 3) {
                afficherServices()
            } else {
                afficherEtablissements()
            }
        }

        view.findViewById<Button>(R.id.btnNew).setOnClickListener {
            afficherEtablissements()
        }

        view.findViewById<Button>(R.id.btnTrack).setOnClickListener {
            (requireActivity() as MainActivity)
                .goTo(R.id.nav_tracking)
        }

        chargerServices()
    }

    // Charger les services depuis le backend
    private fun chargerServices() {

        preparerEcran(
            "CHARGEMENT",
            "Chargement des établissements",
            "Veuillez patienter..."
        )

        ApiClient.catalogApi.getServices()
            .enqueue(object : Callback<List<ServiceResponse>> {

                override fun onResponse(
                    call: Call<List<ServiceResponse>>,
                    response: Response<List<ServiceResponse>>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful) {
                        services = response.body() ?: emptyList()

                        if (services.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "Aucun service disponible",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            afficherEtablissements()
                        }

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Erreur serveur : ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<ServiceResponse>>,
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

    // Étape 1 : choisir l'établissement
    private fun afficherEtablissements() {

        etape = 1
        etablissementChoisi = null
        serviceChoisi = null

        preparerEcran(
            "ÉTAPE 1 SUR 3",
            "Choisissez un établissement",
            "Où souhaitez-vous prendre un ticket ?"
        )

        val types = services
            .map { it.establishmentType }
            .distinctBy { it.lowercase() }
            .sorted()

        for (type in types) {

            val nombre = services.count {
                it.establishmentType.equals(type, ignoreCase = true)
            }

            ajouterChoix(
                type,
                "$nombre services disponibles",
                "→"
            ) {
                etablissementChoisi = type
                afficherServices()
            }
        }
    }

    // Étape 2 : choisir le service
    private fun afficherServices() {

        etape = 2
        serviceChoisi = null

        val type = etablissementChoisi ?: return

        preparerEcran(
            "ÉTAPE 2 SUR 3",
            "Choisissez un service",
            type
        )

        val servicesDisponibles = services.filter {
            it.establishmentType.equals(type, ignoreCase = true)
        }

        for (service in servicesDisponibles) {

            ajouterChoix(
                "${service.letter} · ${service.name}",
                "Service disponible",
                "~${service.estimatedTime} min"
            ) {
                serviceChoisi = service
                afficherAgences()
            }
        }
    }

    // Étape 3 : charger les agences du type choisi
    private fun afficherAgences() {

        etape = 3

        val type = etablissementChoisi ?: return
        val service = serviceChoisi ?: return

        preparerEcran(
            "ÉTAPE 3 SUR 3",
            "Choisissez une agence",
            "${service.name} · $type"
        )

        ajouterChoix(
            "Chargement des agences...",
            "Veuillez patienter",
            ""
        ) {}

        ApiClient.catalogApi.getAgencies(type)
            .enqueue(object : Callback<List<AgencyResponse>> {

                override fun onResponse(
                    call: Call<List<AgencyResponse>>,
                    response: Response<List<AgencyResponse>>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful) {

                        val agences = response.body() ?: emptyList()

                        zoneChoix.removeAllViews()

                        if (agences.isEmpty()) {
                            texteSousTitre.text =
                                "Aucune agence disponible pour cet établissement"
                            return
                        }

                        for (agence in agences) {

                            ajouterChoix(
                                agence.name,
                                agence.address,
                                "Choisir"
                            ) {
                                confirmer(agence)
                            }
                        }

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Erreur serveur : ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<List<AgencyResponse>>,
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

    // Créer le ticket dans le backend
    private fun confirmer(agence: AgencyResponse) {

        val service = serviceChoisi ?: return

        val token = GestionAuth.getToken(requireContext())

        if (token.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                "Session expirée. Reconnectez-vous.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        texteSousTitre.text = "Création de votre ticket..."
        zoneChoix.removeAllViews()

        ApiClient.ticketsApi.createTicket(
            "Bearer $token",
            CreateTicketRequest(
                serviceId = service.id,
                agencyId = agence.id
            )
        ).enqueue(object : Callback<TicketResponse> {

            override fun onResponse(
                call: Call<TicketResponse>,
                response: Response<TicketResponse>
            ) {
                if (!isAdded) return

                if (response.isSuccessful) {

                    val ticket = response.body()

                    if (ticket != null) {
                        afficherConfirmation(ticket)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Réponse vide du serveur",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Erreur création ticket : ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()

                    afficherAgences()
                }
            }

            override fun onFailure(
                call: Call<TicketResponse>,
                t: Throwable
            ) {
                if (!isAdded) return

                Toast.makeText(
                    requireContext(),
                    "Erreur connexion : ${t.message}",
                    Toast.LENGTH_LONG
                ).show()

                afficherAgences()
            }
        })
    }

    // Afficher les informations reçues du backend
    private fun afficherConfirmation(ticket: TicketResponse) {

        zoneChoix.removeAllViews()

        boutonRetour.visibility = View.GONE
        texteEtape.text = "TERMINÉ"
        texteTitre.text = "Votre ticket est prêt"
        texteSousTitre.text = "Présentez ce numéro à l'agence."

        carteConfirmation.visibility = View.VISIBLE

        val date = ticket.createdAt
            .replace("T", " à ")
            .substringBefore(".")

        val details =
            "${ticket.serviceName}\n" +
                    "${ticket.agencyName}\n" +
                    "${ticket.agencyAddress}\n" +
                    "Temps estimé : ${ticket.estimatedTime} min\n" +
                    date

        requireView()
            .findViewById<TextView>(R.id.tvDoneNumber)
            .text = ticket.ticketNumber

        requireView()
            .findViewById<TextView>(R.id.tvDoneInfo)
            .text = details
    }

    // Préparer l'écran
    private fun preparerEcran(
        etapeTexte: String,
        titre: String,
        sousTitre: String
    ) {
        carteConfirmation.visibility = View.GONE

        boutonRetour.visibility =
            if (etape > 1) View.VISIBLE else View.GONE

        texteEtape.text = etapeTexte
        texteTitre.text = titre
        texteSousTitre.text = sousTitre

        zoneChoix.removeAllViews()
    }

    // Ajouter une ligne de choix
    private fun ajouterChoix(
        titre: String,
        sousTitre: String,
        droite: String,
        action: () -> Unit
    ) {
        val ligne = LayoutInflater.from(requireContext())
            .inflate(R.layout.ligne_choix, zoneChoix, false)

        ligne.findViewById<TextView>(R.id.tvChoiceTitle).text = titre
        ligne.findViewById<TextView>(R.id.tvChoiceSub).text = sousTitre
        ligne.findViewById<TextView>(R.id.tvChoiceRight).text = droite

        ligne.setOnClickListener {
            action()
        }

        zoneChoix.addView(ligne)
    }
}