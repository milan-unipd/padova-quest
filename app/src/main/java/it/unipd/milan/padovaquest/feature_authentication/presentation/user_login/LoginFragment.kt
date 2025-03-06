package it.unipd.milan.padovaquest.feature_authentication.presentation.user_login

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.core.util.EmailVerificator
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.databinding.FragmentLoginBinding
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.presentation.service.QuestService
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(), PermissionsHelper.PermissionCallback {

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        PermissionsHelper.initialize(this) {
            onPermissionsGranted()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            loginViewModel.loginState.collect {

                binding.loginButton.isEnabled = !it.isLoading
                binding.googleSignInButton.isEnabled = !it.isLoading
                binding.registerButton.isEnabled = !it.isLoading
                binding.forgotPasswordBtn.isEnabled = !it.isLoading

                if (it.success) {
                    checkPermissionsAndLogIn()
                } else if (it.error != null) {
                    AlertDialog.Builder(context)
                        .setTitle("Attention!")
                        .setMessage(it.error)
                        .setNeutralButton("OK", null)
                        .setCancelable(false)
                        .show()
                }
            }
        }

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isEmpty()) {
                Toast.makeText(context, "You have to enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!EmailVerificator.isValidEmail(username)) {
                Toast.makeText(context, "The email address is badly formated!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(context, "You have to enter your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            it.isEnabled = false
            loginViewModel.doLogin(username, password)
        }

        binding.googleSignInButton.setOnClickListener {
            val credentialManager = CredentialManager.create(requireActivity().baseContext)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        context = requireContext(),
                        request = request
                    )
                    val credential = result.credential
                        // Extract credential from the result returned by Credential Manager
                    // Check if credential is of type Google ID
                    if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        // Create Google ID Token
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                        // Sign in to Firebase with using the token
                        loginViewModel.firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } else {
                        Log.w("TAGA", "Credential is not of type Google ID!")
                    }
                } catch (e: GetCredentialException) {
                    Log.e("TAGA", "Couldn't retrieve user's credentials: ${e.localizedMessage}")
                }
            }

        }



        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.forgotPasswordBtn.setOnClickListener {
            val email = binding.usernameEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                Toast.makeText(context, "An email has been sent to you so you can change the password", Toast.LENGTH_LONG).show()
            } else
                Toast.makeText(context, "Enter your email in the username box", Toast.LENGTH_LONG).show()
        }

        if (Firebase.auth.currentUser != null)
            checkPermissionsAndLogIn()
    }


    private fun checkPermissionsAndLogIn() {
        PermissionsHelper.checkAndRequestPermissions(this@LoginFragment)
    }

    override fun onPermissionsGranted() {

        if (QuestService.isRunning) {
            decideWhereToGo(loginViewModel.sharedQuestDataRepository.quest)
        } else
            lifecycleScope.launch {
                loginViewModel.getCurrentQuest(Firebase.auth.currentUser!!.uid).collect {
                    binding.loginButton.isEnabled = it !is Resource.Loading
                    binding.googleSignInButton.isEnabled = it !is Resource.Loading
                    binding.registerButton.isEnabled = it !is Resource.Loading
                    binding.forgotPasswordBtn.isEnabled = it !is Resource.Loading

                    when (it) {
                        is Resource.Loading -> Unit
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), it.exception.message.toString(), Toast.LENGTH_SHORT).show()
                        }

                        is Resource.Success -> {
                            val intent = Intent(context, QuestService::class.java)
                            ContextCompat.startForegroundService(requireContext(), intent)
                            decideWhereToGo(it.result)
                        }
                    }
                }
            }
    }

    private fun decideWhereToGo(quest: Quest?) {
        if (quest?.id == null) {
            findNavController().navigate(R.id.action_loginFragment_to_walkFragment)
        } else if (quest.type == "personal") {
            findNavController().navigate(R.id.action_loginFragment_to_questFragment)
        } else if (quest.type == "group") {
            if (quest.status == "created") {
                if (quest.createdBy == Firebase.auth.currentUser!!.uid)
                    findNavController().navigate(R.id.action_loginFragment_to_groupQuestStartFragment)
                else
                    findNavController().navigate(R.id.action_loginFragment_to_joinGroupQuestFragment)
            } else if (quest.status == "started") {
                findNavController().navigate(R.id.action_loginFragment_to_questFragment)
            }
        }
    }
}