package com.example.allergynavigator

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.allergynavigator.databinding.FragmentFiltersBinding
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.chip.Chip
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class FiltersFragment : Fragment(R.layout.fragment_filters) {

    private var _binding: FragmentFiltersBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFiltersBinding.bind(view)

        binding.applyButton.setOnClickListener {
            saveAndSyncAllergies()
        }

        binding.resetButton.setOnClickListener {
            binding.chipGroup.clearCheck()
        }
    }

    private fun saveAndSyncAllergies() {
        val selectedAllergies = mutableListOf<String>()
        for (i in 0 until binding.chipGroup.childCount) {
            val chip = binding.chipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedAllergies.add(chip.text.toString())
            }
        }

        // 1. Локальное сохранение аллергий
        val prefs = Prefs(requireContext())
        prefs.userAllergies = selectedAllergies.toSet()

        // 2. Sync with Server using Coroutines
        val request = AllergyRequest(selectedAllergies)
        val token = "Bearer YOUR_TOKEN" // Заменено типа

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.syncAllergies(token, request)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Синхронизировано", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                // This catches network timeouts or no internet
                Toast.makeText(requireContext(), "Сохранено локально (оффлайн)", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }
}