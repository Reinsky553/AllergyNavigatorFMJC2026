package com.example.allergynavigator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.allergynavigator.databinding.FragmentOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs

    private val onboardingPages = listOf(
        OnboardingPage(
            "Добро пожаловать!",
            "Отслеживайте уровень пыльцы аллергенов в вашем городе",
            R.drawable.ic_welcome
        ),
        OnboardingPage(
            "Карта аллергенов",
            "Смотрите на карте зоны с высокой концентрацией аллергенов",
            R.drawable.ic_map
        ),
        OnboardingPage(
            "Выбор аллергий",
            "Настройте приложение под ваши аллергии",
            R.drawable.ic_allergy
        ),
        OnboardingPage(
            "Настройки",
            "Включите уведомления и настройте приложение",
            R.drawable.ic_settings
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = Prefs(requireContext())

        if (prefs.isOnboardingShown) {
            navigateToNextScreen()
            return
        }

        val adapter = OnboardingAdapter(onboardingPages)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.indicator, binding.viewPager) { tab, position ->
        }.attach()

        binding.btnSkip.setOnClickListener {
            completeOnboarding()
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == onboardingPages.size - 1) {
                    binding.btnSkip.text = "Начать"
                } else {
                    binding.btnSkip.text = "Пропустить"
                }
            }
        })
    }

    private fun navigateToNextScreen() {
        // Check if the current destination is actually the OnboardingFragment
        if (findNavController().currentDestination?.id == R.id.onboardingFragment) {
            findNavController().navigate(R.id.action_onboarding_to_login)
        }
    }

    private fun completeOnboarding() {
        prefs.isOnboardingShown = true
        navigateToNextScreen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val iconRes: Int
)

class OnboardingAdapter(private val pages: List<OnboardingPage>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val title: android.widget.TextView = view.findViewById(R.id.tvTitle)
        val description: android.widget.TextView = view.findViewById(R.id.tvDescription)
        val icon: android.widget.ImageView = view.findViewById(R.id.ivIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_page, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val page = pages[position]
        holder.title.text = page.title
        holder.description.text = page.description
        holder.icon.setImageResource(page.iconRes)
    }

    override fun getItemCount() = pages.size
}