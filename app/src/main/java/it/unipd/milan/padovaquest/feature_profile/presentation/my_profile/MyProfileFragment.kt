package it.unipd.milan.padovaquest.feature_profile.presentation.my_profile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.databinding.FragmentMyProfileBinding
import it.unipd.milan.padovaquest.feature_profile.domain.use_cases.MyProfileUseCases
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MyProfileFragment : Fragment() {

    private lateinit var binding: FragmentMyProfileBinding

    @Inject
    lateinit var myProfileUseCases: MyProfileUseCases

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = Firebase.auth.currentUser!!

        binding.nameTextView.text = user.displayName
        binding.emaikTextView.text = user.email
        val timestamp = user.metadata?.creationTimestamp
        if (timestamp != null) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val formattedDate = sdf.format(Date(timestamp))
            binding.joinedOnTextView.text = formattedDate
        } else {
            binding.joinedOnTextView.text = "Unknown"
        }

        binding.changeNameButton.setOnClickListener {
            showEditNameDialog()
        }

        binding.logoutButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes") { _, _ ->
                    Firebase.auth.signOut()
                    activity?.finish()
                }
                .show()
        }
    }

    private fun showEditNameDialog() {
        val user = Firebase.auth.currentUser

        val inflater = LayoutInflater.from(activity)
        val dialogView = inflater.inflate(R.layout.dialog_edit_name, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameEditText)

        nameInput.setText(user?.displayName ?: "")

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Name")
            .setView(dialogView)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val newName = nameInput.text.toString().trim()
            if (newName.isEmpty()) {
                nameInput.error = "Name cannot be empty"
                return@setOnClickListener
            }
            if (newName == user?.displayName) {
                Toast.makeText(activity, "Name is the same", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false
            nameInput.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                val result = myProfileUseCases.updateNameUseCase(user!!.uid, newName)

                if (result is Resource.Error) {
                    Toast.makeText(activity, "Failed to update name", Toast.LENGTH_SHORT).show()
                } else {
                    binding.nameTextView.text = newName
                    Toast.makeText(activity, "Name updated!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }
    }
}