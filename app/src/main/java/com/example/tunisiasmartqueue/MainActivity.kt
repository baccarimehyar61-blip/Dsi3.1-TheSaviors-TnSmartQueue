package com.example.tunisiasmartqueue

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.tunisiasmartqueue.auth.ConnexionActivity
import com.example.tunisiasmartqueue.auth.GestionAuth
import com.example.tunisiasmartqueue.dashboard.Accueil
import com.example.tunisiasmartqueue.notifications.GestionNotifications
import com.example.tunisiasmartqueue.notifications.Notifications
import com.example.tunisiasmartqueue.profile.Profil
import com.example.tunisiasmartqueue.tickets.MesTickets
import com.example.tunisiasmartqueue.tickets.PrendreTicket
import com.example.tunisiasmartqueue.tickets.Suivi
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // La barre de navigation du bas (Accueil, Ticket, Mes tickets, Suivi, Profil)
    private lateinit var barreNavigation: BottomNavigationView

    // S'exécute à l'ouverture de l'écran
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Page protégée : si l'utilisateur n'est pas connecté, retour au login
        if (!GestionAuth.isLoggedIn(this)) {
            startActivity(Intent(this, ConnexionActivity::class.java))
            finish()
            return
        }

        // 2) Afficher le design (activity_main.xml)
        setContentView(R.layout.activity_main)

        // 3) Ajouter une marge pour que l'en-tête ne passe pas sous la barre d'état
        val vueRacine = findViewById<View>(R.id.root)
        ViewCompat.setOnApplyWindowInsetsListener(vueRacine) { vue, insets ->
            val barres = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vue.setPadding(barres.left, barres.top, barres.right, barres.bottom)
            insets
        }

        // 4) La barre de navigation : un clic sur un onglet ouvre son écran
        barreNavigation = findViewById(R.id.bottomNav)
        barreNavigation.setOnItemSelectedListener { onglet ->
            ouvrirOnglet(onglet.itemId)
            true
        }
        // Toucher l'onglet déjà actif recharge aussi l'écran
        barreNavigation.setOnItemReselectedListener { onglet ->
            ouvrirOnglet(onglet.itemId)
        }

        // 5) La cloche ouvre les notifications
        val boutonCloche = findViewById<ImageButton>(R.id.btnNotif)
        boutonCloche.setOnClickListener {
            openNotifications()
        }

        // 6) Afficher le nombre de notifications non lues sur la cloche
        refreshBadge()

        // 7) Au premier lancement, on commence par l'onglet Accueil
        if (savedInstanceState == null) {
            barreNavigation.selectedItemId = R.id.nav_dashboard
        }
    }

    // Met à jour le badge rouge de la cloche (nombre de notifications non lues)
    fun refreshBadge() {
        val badge = findViewById<TextView>(R.id.tvBadge)
        val nombre = GestionNotifications.unreadCount(this)

        badge.text = nombre.toString()
        if (nombre > 0) {
            badge.visibility = View.VISIBLE
        } else {
            badge.visibility = View.GONE
        }
    }

    // Change d'onglet (utilisée par les autres écrans)
    fun goTo(itemId: Int) {
        barreNavigation.selectedItemId = itemId
    }

    // Ouvre l'écran des notifications
    fun openNotifications() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, Notifications())
            .addToBackStack(null)   // le bouton retour du téléphone revient à l'écran d'avant
            .commit()
    }

    // Choisit quel écran afficher selon l'onglet touché
    private fun ouvrirOnglet(itemId: Int) {
        when (itemId) {
            R.id.nav_dashboard -> afficher(Accueil())
            R.id.nav_take -> afficher(PrendreTicket())
            R.id.nav_tickets -> afficher(MesTickets())
            R.id.nav_tracking -> afficher(Suivi())
            R.id.nav_profile -> afficher(Profil())
        }
    }

    // Remplace l'écran du milieu par le fragment demandé
    private fun afficher(fragment: Fragment) {
        // On vide l'historique de retour pour ne pas empiler les écrans
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}