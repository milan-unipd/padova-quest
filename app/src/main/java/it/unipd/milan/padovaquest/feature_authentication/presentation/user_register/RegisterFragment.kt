package it.unipd.milan.padovaquest.feature_authentication.presentation.user_register

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.core.util.repeatOnResumed
import it.unipd.milan.padovaquest.databinding.FragmentRegisterBinding

@AndroidEntryPoint
class RegisterFragment : Fragment() {


    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.passwordRepeatEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val password = binding.passwordEditText.text.toString()
                val repeatPassword = s.toString()

                if (password == repeatPassword || repeatPassword.isEmpty()) {
                    binding.passwordRepeatInputLayout.error = null
                } else {
                    binding.passwordRepeatInputLayout.error = getString(R.string.passwords_do_not_match)
                }
            }
        })

        binding.usernameEditText.doAfterTextChanged {
            if (it != null && it.toString().isNotEmpty())
                binding.usernameInputLayout.error = null
            else
                binding.usernameInputLayout.error = "This field cannot be empty"

        }

        binding.passwordEditText.doAfterTextChanged {
            if (it != null && it.toString().isNotEmpty()) {
                if (it.toString().length < 6)
                    binding.passwordInputLayout.error = "Password must be at least 6 characters long"
                else
                    binding.passwordInputLayout.error = null
            } else {
                binding.passwordInputLayout.error = "This field cannot be empty"
            }
        }

        binding.emailEditText.doAfterTextChanged {
            if (it != null && it.toString().isNotEmpty()) {
                if (!isValidEmail(it.toString()))
                    binding.emailInputLayout.error = "Invalid email"
                else
                    binding.emailInputLayout.error = null
            } else {
                binding.emailInputLayout.error = "This field cannot be empty"
            }
        }

        binding.signupButton.setOnClickListener {
            val name = binding.usernameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val repeatPassword = binding.passwordRepeatEditText.text.toString().trim()

            var hasError = false

            if (!isValidEmail(email)) {
                binding.emailInputLayout.error = "Invalid email"
                hasError = true
            } else {
                binding.emailInputLayout.error = null
            }

            if (name.isEmpty()) {
                hasError = true
            }

            if (password.isEmpty() || password.length < 6) {
                hasError = true
            }

            if (repeatPassword.isEmpty()) {
                hasError = true
            }

            if (hasError)
                return@setOnClickListener

            viewModel.register(name, email, password)
        }

        viewLifecycleOwner.repeatOnResumed {
            viewModel.registerFlow.collect { result ->
                if (result == null)
                    return@collect
                viewModel.emptyRegisterFlow()

                binding.signupButton.isEnabled = result !is Resource.Loading
                binding.usernameInputLayout.isEnabled = result !is Resource.Loading
                binding.emailInputLayout.isEnabled = result !is Resource.Loading
                binding.passwordInputLayout.isEnabled = result !is Resource.Loading
                binding.passwordRepeatInputLayout.isEnabled = result !is Resource.Loading

                if (result is Resource.Error) {
                    AlertDialog.Builder(context)
                        .setTitle("Attention!")
                        .setMessage(result.exception.localizedMessage)
                        .setNeutralButton("OK", null)
                        .setCancelable(false)
                        .show()

                } else if (result is Resource.Success) {
                    findNavController().popBackStack()
                }


            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }


}