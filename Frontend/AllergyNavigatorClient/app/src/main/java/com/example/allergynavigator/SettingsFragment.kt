package com.example.allergynavigator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.allergynavigator.databinding.FragmentSettingsBinding
import com.google.android.material.chip.Chip

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        prefs = Prefs(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvUserName.text = prefs.userName.ifEmpty { "Пользователь" }
        binding.tvUserEmail.text = prefs.userEmail.ifEmpty { "user@example.com" }

        setupAllergyChips()

        binding.switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveBoolean("notifications_enabled", isChecked)
        }

        binding.tvFeedback.setOnClickListener {
            android.widget.Toast.makeText(
                requireContext(),
                "Открытие формы обратной связи",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnLogout.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Выйти") { _, _ ->
                    logout()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun setupAllergyChips() {
        val savedAllergies = prefs.userAllergies

        allergies.forEach { allergy ->
            val chip = Chip(requireContext()).apply {
                text = allergy.name
                setChipIconResource(allergy.iconRes)
                isCheckable = true
                isChecked = savedAllergies.contains(allergy.name)
                chipIconSize = resources.getDimension(R.dimen.chip_icon_size)
                setOnCheckedChangeListener { _, isChecked ->
                    val currentAllergies = prefs.userAllergies.toMutableSet()
                    if (isChecked) {
                        currentAllergies.add(allergy.name)
                    } else {
                        currentAllergies.remove(allergy.name)
                    }
                    prefs.userAllergies = currentAllergies
                }
            }
            binding.chipGroup.addView(chip)
        }
    }

    private fun logout() {
        prefs.isLoggedIn = false
        prefs.userEmail = ""
        prefs.userName = ""

        findNavController().navigate(R.id.action_global_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



