package com.example.tunisiasmartqueue.notifications

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tunisiasmartqueue.MainActivity
import com.example.tunisiasmartqueue.R
import com.google.android.material.card.MaterialCardView

class Notifications : Fragment(R.layout.ecran_notifications) {

    // S'exécute quand l'écran est prêt
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Le lien « Tout marquer comme lu »
        view.findViewById<TextView>(R.id.tvMarkAll).setOnClickListener {
            GestionNotifications.markAllRead(requireContext())
            afficherNotifications(view)
        }

        // Afficher la liste au démarrage
        afficherNotifications(view)
    }

    // Affiche (ou rafraîchit) toute la liste des notifications
    private fun afficherNotifications(view: View) {
        val liste = GestionNotifications.getAll(requireContext())

        val conteneur = view.findViewById<LinearLayout>(R.id.notifContainer)
        val sousTitre = view.findViewById<TextView>(R.id.tvNSub)
        val boutonToutLire = view.findViewById<View>(R.id.tvMarkAll)
        val texteVide = view.findViewById<View>(R.id.tvNEmpty)

        // 1) Compter les notifications non lues
        var nonLues = 0
        for (notif in liste) {
            if (!notif.read) {
                nonLues++
            }
        }

        // 2) Écrire le texte du haut
        if (nonLues > 0) {
            sousTitre.text = "$nonLues non lue(s)"
            boutonToutLire.visibility = View.VISIBLE
        } else {
            sousTitre.text = "Tout est à jour"
            boutonToutLire.visibility = View.GONE
        }

        // 3) Message « Aucune notification » si la liste est vide
        if (liste.isEmpty()) {
            texteVide.visibility = View.VISIBLE
        } else {
            texteVide.visibility = View.GONE
        }

        // 4) Vider la liste puis recréer une ligne par notification
        conteneur.removeAllViews()

        for (notif in liste) {
            val ligne = LayoutInflater.from(requireContext())
                .inflate(R.layout.ligne_notification, conteneur, false)

            ligne.findViewById<TextView>(R.id.tvNTitle).text = notif.title
            ligne.findViewById<TextView>(R.id.tvNMessage).text = notif.message
            ligne.findViewById<TextView>(R.id.tvNTime).text = notif.time

            // Couleur du petit point selon le type (bleu par défaut = « info »)
            var couleurPoint = "#2563EB"
            if (notif.type == "success") {
                couleurPoint = "#15803D"
            }
            if (notif.type == "warning") {
                couleurPoint = "#B45309"
            }
            val point = ligne.findViewById<View>(R.id.vDot)
            point.backgroundTintList = ColorStateList.valueOf(Color.parseColor(couleurPoint))

            // Fond de la carte : rosé si non lue, blanc si lue
            val carte = ligne as MaterialCardView
            if (notif.read) {
                carte.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            } else {
                carte.setCardBackgroundColor(Color.parseColor("#FFF8F8"))
            }

            // Un clic sur une notification non lue la marque comme lue
            ligne.setOnClickListener {
                if (!notif.read) {
                    GestionNotifications.markRead(requireContext(), notif.id)
                    afficherNotifications(view)
                }
            }

            conteneur.addView(ligne)
        }

        // 5) Mettre à jour le badge rouge de la cloche
        val activite = requireActivity() as MainActivity
        activite.refreshBadge()
    }
}