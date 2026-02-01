package com.example.allergynavigator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.allergynavigator.databinding.FragmentLoginBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        prefs = Prefs(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Заполнение почты
        val savedEmail = prefs.userEmail
        if (savedEmail.isNotEmpty()) {
            binding.etEmail.setText(savedEmail)
        }

        // Один клик
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Вызов
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        // UserRequest to LoginRequest
                        val request = LoginRequest(username = email, pass = password)
                        val response = RetrofitClient.instance.loginUser(request)

                        if (response.isSuccessful) {
                            prefs.userEmail = email
                            prefs.isLoggedIn = true
                            findNavController().navigate(R.id.action_login_to_map)
                        } else {
                            Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Connection error", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                binding.etEmail.error = if (email.isEmpty()) "Введите email" else null
                binding.etPassword.error = if (password.isEmpty()) "Введите пароль" else null
            }
        }

        // Передаресация на регистрацию
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.btnForgotPassword.setOnClickListener {
            Toast.makeText(requireContext(), "In development", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}