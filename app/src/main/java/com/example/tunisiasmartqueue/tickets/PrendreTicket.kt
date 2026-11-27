package com.example.tunisiasmartqueue.tickets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tunisiasmartqueue.MainActivity
import com.example.tunisiasmartqueue.R
import com.example.tunisiasmartqueue.notifications.GestionNotifications
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Un service proposé (ex : « A · Extrait de naissance »)
private data class Service(val lettre: String, val nom: String, val attente: Int, val duree: Int)

// Un établissement avec ses services (ex : « La Poste »)
private data class Etablissement(val nom: String, val services: List<Service>)

// Une agence avec son adresse
private data class Agence(val nom: String, val adresse: String)

class PrendreTicket : Fragment(R.layout.ecran_prendre_ticket) {

    // ---------- Les données proposées à l'utilisateur ----------
    private val etablissements = listOf(
        Etablissement("Municipalité", listOf(
            Service("A", "Extrait de naissance", 8, 20),
            Service("B", "Légalisation de signature", 5, 15),
            Service("C", "Permis de construire", 12, 35),
            Service("D", "État civil", 6, 18)
        )),
        Etablissement("La Poste", listOf(
            Service("E", "Opérations postales", 10, 20),
            Service("F", "Mandats et virements", 4, 12)
        )),
        Etablissement("Recette des Finances", listOf(
            Service("G", "Taxes", 9, 25),
            Service("H", "Impôts", 14, 40),
            Service("I", "Amendes", 3, 10)
        ))
    )

    private val agences = listOf(
        Agence("Agence centre-ville", "Avenue Habib Bourguiba, Tunis"),
        Agence("Agence Les Berges du Lac", "Rue du Lac Léman, Tunis"),
        Agence("Agence Ariana", "Avenue de la République, Ariana")
    )

    // ---------- Ce que l'utilisateur a choisi jusqu'ici ----------
    private var etape = 1                                   // étape 1, 2 ou 3
    private var etablissementChoisi: Etablissement? = null  // null = pas encore choisi
    private var serviceChoisi: Service? = null

    // ---------- Les éléments de l'écran ----------
    private lateinit var zoneChoix: LinearLayout
    private lateinit var boutonRetour: TextView
    private lateinit var texteEtape: TextView
    private lateinit var texteTitre: TextView
    private lateinit var texteSousTitre: TextView
    private lateinit var carteConfirmation: View

    // S'exécute quand l'écran est prêt
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrouver les éléments de l'écran
        zoneChoix = view.findViewById(R.id.choicesContainer)
        boutonRetour = view.findViewById(R.id.tvBack)
        texteEtape = view.findViewById(R.id.tvStepLabel)
        texteTitre = view.findViewById(R.id.tvStepTitle)
        texteSousTitre = view.findViewById(R.id.tvStepSub)
        carteConfirmation = view.findViewById(R.id.cardDone)

        // Bouton « Retour » : revient à l'étape précédente
        boutonRetour.setOnClickListener {
            if (etape == 3) {
                afficherServices()
            } else {
                afficherEtablissements()
            }
        }

        // Boutons de la confirmation
        view.findViewById<Button>(R.id.btnNew).setOnClickListener {
            afficherEtablissements()
        }
        view.findViewById<Button>(R.id.btnTrack).setOnClickListener {
            val activite = requireActivity() as MainActivity
            activite.goTo(R.id.nav_tracking)
        }

        // Démarrer à l'étape 1
        afficherEtablissements()
    }

    // ---------- Étape 1 : choisir l'établissement ----------
    private fun afficherEtablissements() {
        etape = 1
        etablissementChoisi = null
        serviceChoisi = null

        preparerEcran("ÉTAPE 1 SUR 3", "Choisissez un établissement",
            "Où souhaitez-vous prendre un ticket ?")

        for (etablissement in etablissements) {
            val nombre = etablissement.services.size
            ajouterChoix(etablissement.nom, "$nombre services disponibles", "→") {
                etablissementChoisi = etablissement
                afficherServices()
            }
        }
    }

    // ---------- Étape 2 : choisir le service ----------
    private fun afficherServices() {
        etape = 2
        serviceChoisi = null

        val etablissement = etablissementChoisi
        if (etablissement == null) {
            afficherEtablissements()
            return
        }

        preparerEcran("ÉTAPE 2 SUR 3", "Choisissez un service", etablissement.nom)

        for (service in etablissement.services) {
            ajouterChoix(
                "${service.lettre} · ${service.nom}",
                "${service.attente} personnes en attente",
                "~${service.duree} min"
            ) {
                serviceChoisi = service
                afficherAgences()
            }
        }
    }

    // ---------- Étape 3 : choisir l'agence ----------
    private fun afficherAgences() {
        etape = 3

        val etablissement = etablissementChoisi
        val service = serviceChoisi
        if (etablissement == null || service == null) {
            afficherEtablissements()
            return
        }

        preparerEcran("ÉTAPE 3 SUR 3", "Choisissez une agence",
            "${etablissement.nom} · ${service.nom}")

        for (agence in agences) {
            ajouterChoix(agence.nom, agence.adresse, "Choisir") {
                confirmer(agence)
            }
        }
    }

    // ---------- Confirmation : créer et enregistrer le ticket ----------
    private fun confirmer(agence: Agence) {
        val etablissement = etablissementChoisi
        val service = serviceChoisi
        if (etablissement == null || service == null) {
            return
        }

        val contexte = requireContext()
        val maintenant = Date()

        // Numéro du ticket : la lettre du service + 3 chiffres au hasard (ex : A-482)
        val chiffres = (100..999).random()
        val numero = "${service.lettre}-$chiffres"

        val ticket = Ticket(
            number = numero,
            establishment = etablissement.nom,
            service = service.nom,
            agency = agence.nom,
            address = agence.adresse,
            waiting = service.duree,
            peopleAhead = service.attente,
            status = "En attente",
            progress = 0,
            date = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(maintenant),
            time = SimpleDateFormat("HH:mm", Locale.FRANCE).format(maintenant),
            createdAt = maintenant.time
        )

        // Enregistrer le ticket
        GestionTickets.saveTicket(contexte, ticket)

        // Ajouter une notification et mettre à jour le badge de la cloche
        GestionNotifications.add(
            contexte, "success", "Ticket confirmé",
            "Votre ticket ${ticket.number} a été créé pour ${ticket.service}."
        )
        val activite = requireActivity() as MainActivity
        activite.refreshBadge()

        // Afficher l'écran de confirmation
        zoneChoix.removeAllViews()
        boutonRetour.visibility = View.GONE
        texteEtape.text = "TERMINÉ"
        texteTitre.text = "Votre ticket est prêt"
        texteSousTitre.text = "Présentez ce numéro à l'agence."
        carteConfirmation.visibility = View.VISIBLE

        val details = "${ticket.establishment} · ${ticket.service}\n" +
                "${ticket.agency}\n" +
                "${ticket.peopleAhead} personnes devant vous · ~${ticket.waiting} min\n" +
                "${ticket.date} à ${ticket.time}"

        requireView().findViewById<TextView>(R.id.tvDoneNumber).text = ticket.number
        requireView().findViewById<TextView>(R.id.tvDoneInfo).text = details
    }

    // ---------- Fonctions utiles ----------

    // Prépare l'écran pour une nouvelle étape : change les titres et vide la liste
    private fun preparerEcran(etapeTexte: String, titre: String, sousTitre: String) {
        carteConfirmation.visibility = View.GONE

        // Le bouton « Retour » n'existe pas à l'étape 1
        if (etape > 1) {
            boutonRetour.visibility = View.VISIBLE
        } else {
            boutonRetour.visibility = View.GONE
        }

        texteEtape.text = etapeTexte
        texteTitre.text = titre
        texteSousTitre.text = sousTitre
        zoneChoix.removeAllViews()
    }

    // Ajoute UNE ligne de choix dans la liste ; « action » s'exécute au clic
    private fun ajouterChoix(titre: String, sousTitre: String, droite: String, action: () -> Unit) {
        val ligne = LayoutInflater.from(requireContext())
            .inflate(R.layout.ligne_choix, zoneChoix, false)

        ligne.findViewById<TextView>(R.id.tvChoiceTitle).text = titre
        ligne.findViewById<TextView>(R.id.tvChoiceSub).text = sousTitre
        ligne.findViewById<TextView>(R.id.tvChoiceRight).text = droite
        ligne.setOnClickListener { action() }

        zoneChoix.addView(ligne)
    }
}