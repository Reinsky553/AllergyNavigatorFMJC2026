package com.example.allergynavigator

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.allergynavigator.databinding.FragmentMapBinding
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.preference.PreferenceManager
import android.location.Geocoder
import java.util.Locale
import com.example.allergynavigator.models.RouteRequest
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.osmdroid.views.overlay.Polyline
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.views.overlay.Marker
import coil.imageLoader
import coil.request.ImageRequest
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import com.example.allergynavigator.models.Landmark

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationOverlay: MyLocationNewOverlay

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            locationOverlay.enableMyLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Configuration.getInstance().load(requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = "AllergyNavigator"

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Настройка карты
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), binding.map)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
        binding.map.overlays.add(locationOverlay)
        binding.map.setMultiTouchControls(true)

        // 2. Вид по умолчанию
        val mapController = binding.map.controller
        mapController.setZoom(15.0)
        val cityPoint = GeoPoint(55.0000, 86.0000)
        mapController.setCenter(cityPoint)

        // 3. Запрос разрешений
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        // 4. Кнопка центрирования
        binding.recenterBtn.setOnClickListener {
            val myLocation = locationOverlay.myLocation
            if (myLocation != null) {
                binding.map.controller.animateTo(myLocation)
            } else {
                Toast.makeText(requireContext(), "Searching for GPS...", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Кнопки навигации
        binding.filtersBtn.setOnClickListener {
            findNavController().navigate(R.id.action_map_to_settings)
        }

        binding.btnFindRoute.setOnClickListener {
            val address = binding.searchBar.text.toString()
            val startLocation = locationOverlay.myLocation

            if (address.isNotEmpty() && startLocation != null) {
                findAndDrawRoute(address, startLocation)
            } else {
                Toast.makeText(requireContext(), "Enter address and wait for GPS", Toast.LENGTH_SHORT).show()
            }
        }

        // Кнопка тур по городу
        binding.btnCityTour.setOnClickListener {
            val startLocation = locationOverlay.myLocation
            if (startLocation != null) {
                startCityTour(startLocation)
            } else {
                Toast.makeText(requireContext(), "Wait for GPS location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCityTour(startPoint: GeoPoint) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val api = RetrofitClient.mapInstance.create(ApiService::class.java)
                    val requestBody = RouteRequest(
                        start_coords = listOf(startPoint.latitude, startPoint.longitude),
                        end_coords = listOf(0.0, 0.0),
                        sensitivity = 1.0
                    )
                    api.getTourRoute(requestBody)
                }

                if (response.status == "success" && response.route.isNotEmpty()) {
                    drawPathOnMap(response.route)

                    // Добавление маркеров локаций
                    response.landmarks?.let { landmarkList ->
                        addMarkersToMap(landmarkList)
                    }

                    Toast.makeText(requireContext(), "Tour route and landmarks loaded!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun findAndDrawRoute(address: String, startPoint: GeoPoint) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Конвертирование адреса в координаты!!!
                val destinationCoords = withContext(Dispatchers.IO) {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val results = geocoder.getFromLocationName(address, 1)
                    if (!results.isNullOrEmpty()) {
                        listOf(results[0].latitude, results[0].longitude)
                    } else null
                }

                if (destinationCoords == null) {
                    Toast.makeText(requireContext(), "Address not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2. Получение путей с сервера
                val response = withContext(Dispatchers.IO) {
                    val api = RetrofitClient.mapInstance.create(ApiService::class.java)
                    val requestBody = RouteRequest(
                        start_coords = listOf(startPoint.latitude, startPoint.longitude),
                        end_coords = destinationCoords,
                        sensitivity = 1.0
                    )
                    api.getRoute(requestBody) // @app.post("/get_route")
                }

                // 3. Нарисовать
                if (response.status == "success" && response.route.isNotEmpty()) {
                    drawPathOnMap(response.route)
                } else {
                    Toast.makeText(requireContext(), "Server returned no path", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                println("DEBUG: Connection Error: ${e.message}")
                Toast.makeText(requireContext(), "Connection Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun drawPathOnMap(points: List<List<Double>>) {
        // Вынужденные издержки
        val polyline = Polyline(binding.map)

        // Сервер в геопоинты
        val geoPoints = points.map { GeoPoint(it[0], it[1]) }
        polyline.setPoints(geoPoints)

        polyline.outlinePaint.color = Color.RED
        polyline.outlinePaint.strokeWidth = 12f

        // Очищает старые пути
        val existingPolylines = binding.map.overlays.filterIsInstance<Polyline>()
        binding.map.overlays.removeAll(existingPolylines)

        binding.map.overlays.add(polyline)

        // Зум на путь
        if (geoPoints.isNotEmpty()) {
            binding.map.zoomToBoundingBox(polyline.bounds, true, 100)
        }

        binding.map.invalidate() // Refresh
    }

    private fun addMarkersToMap(landmarks: List<Landmark>) {
        // 1. Очищает старые пути
        val existingMarkers = binding.map.overlays.filterIsInstance<Marker>()
        binding.map.overlays.removeAll(existingMarkers)

        landmarks.forEach { landmark ->
            val marker = Marker(binding.map)
            marker.position = GeoPoint(landmark.lat, landmark.lon)
            marker.title = landmark.name

            val request = ImageRequest.Builder(requireContext())
                .data(landmark.image_url)
                .target { drawable ->
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    val smallIcon = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
                    marker.icon = BitmapDrawable(resources, smallIcon)
                }
                .build()

            requireContext().imageLoader.enqueue(request)

            binding.map.overlays.add(marker)
        }
        binding.map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}