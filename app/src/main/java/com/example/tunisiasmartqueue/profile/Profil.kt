
package com.example.tunisiasmartqueue.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tunisiasmartqueue.R
import com.example.tunisiasmartqueue.api.ApiClient
import com.example.tunisiasmartqueue.api.UserResponse
import com.example.tunisiasmartqueue.api.ProfileUpdateRequest
import com.example.tunisiasmartqueue.auth.GestionAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class Profil : Fragment(R.layout.ecran_profil) {

    private var profil: UserResponse? = null

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText

    private lateinit var tvHello: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvCreatedAt: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvBack: TextView

    private lateinit var btnEdit: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private lateinit var layoutActions: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser les éléments XML

        tvHello = view.findViewById(R.id.tvHello)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvRole = view.findViewById(R.id.tvRole)
        tvCreatedAt = view.findViewById(R.id.tvCreatedAt)
        tvMessage = view.findViewById(R.id.tvMessage)
        tvBack = view.findViewById(R.id.tvBack)

        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etEmail = view.findViewById(R.id.etEmail)
        etPhone = view.findViewById(R.id.etPhone)

        btnEdit = view.findViewById(R.id.btnEdit)
        btnSave = view.findViewById(R.id.btnSave)
        btnCancel = view.findViewById(R.id.btnCancel)

        layoutActions = view.findViewById(R.id.layoutActions)
        progressBar = view.findViewById(R.id.progressBar)

        // Retour au tableau de bord

        tvBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Charger le profil

        loadProfile()

        // Modifier le profil

        btnEdit.setOnClickListener {

            setFieldsEnabled(true)

            btnEdit.visibility = View.GONE

            layoutActions.visibility = View.VISIBLE

            showMessage("")
        }

        // Enregistrer les modifications

        btnSave.setOnClickListener {
            saveProfile()
        }

        // Annuler les modifications

        btnCancel.setOnClickListener {
            cancelEditing()
        }
    }

    // Charger le profil depuis le backend

    private fun loadProfile() {

        val token = GestionAuth.getToken(requireContext())

        if (token.isNullOrBlank()) {
            showMessage("Veuillez vous connecter.")
            return
        }

        progressBar.visibility = View.VISIBLE

        ApiClient.profilApi.getProfil("Bearer $token")
            .enqueue(object : Callback<UserResponse> {

                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {

                    if (!isAdded) return

                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {

                        val data = response.body()

                        if (data != null) {

                            profil = data
                            displayProfile(data)

                        } else {

                            showMessage("Profil introuvable.")
                        }

                    } else {

                        showMessage(
                            "Erreur ${response.code()} : Impossible de charger le profil."
                        )
                    }
                }

                override fun onFailure(
                    call: Call<UserResponse>,
                    t: Throwable
                ) {

                    if (!isAdded) return

                    progressBar.visibility = View.GONE

                    showMessage(
                        "Erreur de connexion au serveur : ${t.message}"
                    )
                }
            })
    }

    // Afficher les informations du profil

    private fun displayProfile(data: UserResponse) {

        tvHello.text = "Bonjour ${data.firstName} 👋"

        tvEmail.text = data.email

        tvRole.text = "Type de compte : ${data.role}"

        tvCreatedAt.text =
            "Date de création : ${formatDate(data.createdAt)}"

        etFirstName.setText(data.firstName)

        etLastName.setText(data.lastName)

        etEmail.setText(data.email)

        etPhone.setText(data.phone ?: "")

        setFieldsEnabled(false)

        btnEdit.visibility = View.VISIBLE
        btnEdit.isEnabled = true

        layoutActions.visibility = View.GONE

        btnSave.isEnabled = true
        btnCancel.isEnabled = true
    }

    // Activer ou désactiver les champs

    private fun setFieldsEnabled(enabled: Boolean) {

        etFirstName.isEnabled = enabled

        etLastName.isEnabled = enabled

        etPhone.isEnabled = enabled

        // L'adresse e-mail reste non modifiable

        etEmail.isEnabled = false
    }

    // Enregistrer les modifications

    private fun saveProfile() {

        val firstName = etFirstName.text.toString().trim()

        val lastName = etLastName.text.toString().trim()

        val phone = etPhone.text.toString().trim()

        // Vérifier les champs obligatoires

        if (firstName.isEmpty() || lastName.isEmpty()) {

            showMessage(
                "Le prénom et le nom sont obligatoires."
            )

            return
        }

        val token = GestionAuth.getToken(requireContext())

        if (token.isNullOrBlank()) {

            showMessage("Veuillez vous connecter.")

            return
        }

        val request = ProfileUpdateRequest(
            firstName = firstName,
            lastName = lastName,
            phone = phone.ifEmpty { null }
        )

        progressBar.visibility = View.VISIBLE

        btnSave.isEnabled = false
        btnCancel.isEnabled = false

        ApiClient.profilApi.updateProfil(
            "Bearer $token",
            request
        ).enqueue(object : Callback<UserResponse> {

            override fun onResponse(
                call: Call<UserResponse>,
                response: Response<UserResponse>
            ) {

                if (!isAdded) return

                progressBar.visibility = View.GONE

                btnSave.isEnabled = true
                btnCancel.isEnabled = true

                if (response.isSuccessful) {

                    val data = response.body()

                    if (data != null) {

                        profil = data

                        displayProfile(data)

                        showMessage(
                            "Votre profil a été modifié avec succès."
                        )

                    } else {

                        showMessage(
                            "Réponse invalide du serveur."
                        )
                    }

                } else {

                    showMessage(
                        "Erreur ${response.code()} lors de la modification."
                    )
                }
            }

            override fun onFailure(
                call: Call<UserResponse>,
                t: Throwable
            ) {

                if (!isAdded) return

                progressBar.visibility = View.GONE

                btnSave.isEnabled = true
                btnCancel.isEnabled = true

                showMessage(
                    "Erreur de connexion au serveur : ${t.message}"
                )
            }
        })
    }

    // Annuler les modifications

    private fun cancelEditing() {

        profil?.let {
            displayProfile(it)
        }

        showMessage("")
    }

    // Afficher les messages

    private fun showMessage(message: String) {

        tvMessage.text = message

        tvMessage.visibility =
            if (message.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    // Formater la date

    private fun formatDate(date: String?): String {

        if (date.isNullOrEmpty()) return "—"

        return try {

            val datePart = date.substringBefore("T")

            val inputFormat = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

            val outputFormat = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.FRANCE
            )

            outputFormat.format(
                inputFormat.parse(datePart)!!
            )

        } catch (e: Exception) {

            date
        }
    }
}
