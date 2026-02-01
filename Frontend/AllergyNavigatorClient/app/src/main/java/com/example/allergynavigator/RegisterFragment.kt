package com.example.allergynavigator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.allergynavigator.databinding.FragmentRegisterBinding
import com.google.android.material.chip.Chip
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs

    private val allergies = listOf(
        Allergy("Береза", R.drawable.ic_tree),
        Allergy("Злаковые травы", R.drawable.ic_grass),
        Allergy("Полынь", R.drawable.ic_plant),
        Allergy("Тополиный пух", R.drawable.ic_cotton),
        Allergy("Амброзия", R.drawable.ic_ragweed),
        Allergy("Ольха", R.drawable.ic_alder)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        prefs = Prefs(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAllergyChips()

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val name = binding.etName.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {

                // 1. Бэкграунд таска
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val request = RegisterRequest(name = name, username = email, pass = password)
                        val response = RetrofitClient.instance.registerUser(request)

                        if (response.isSuccessful) {
                            prefs.userEmail = email
                            prefs.userName = name
                            prefs.isLoggedIn = true
                            findNavController().navigate(R.id.action_register_to_map)
                        } else {
                            Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // В случае ошибки
            }
        }
    }

    private fun setupAllergyChips() {
        allergies.forEach { allergy ->
            val chip = Chip(requireContext()).apply {
                text = allergy.name
                setChipIconResource(allergy.iconRes)
                isCheckable = true
                chipIconSize = resources.getDimension(R.dimen.chip_icon_size)
                setOnCheckedChangeListener { _, isChecked ->
                }
            }
            binding.chipGroup.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Allergy(val name: String, val iconRes: Int)