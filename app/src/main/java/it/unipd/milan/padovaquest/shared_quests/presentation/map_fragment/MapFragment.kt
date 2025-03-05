package it.unipd.milan.padovaquest.shared_quests.presentation.map_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.core.presentation.BaseViewModel
import it.unipd.milan.padovaquest.core.util.repeatOnResumed
import it.unipd.milan.padovaquest.databinding.FragmentMapBinding
import it.unipd.milan.padovaquest.shared_quests.domain.model.LocationStatus
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import javax.inject.Inject
import kotlin.math.max


abstract class MapFragment : Fragment(), OnMapReadyCallback {

    @Inject
    lateinit var sharedQuestDataRepository: SharedQuestDataRepository
    private lateinit var binding: FragmentMapBinding


    protected lateinit var mMap: GoogleMap
    internal var myMarker: Marker? = null
    private var isFirstLocation = true

    private var navigationView: NavigationView? = null
    private var drawerLayout: DrawerLayout? = null

    private val baseViewModel: BaseViewModel by activityViewModels()


    protected abstract fun getFragmentLayout(): Int
    protected abstract fun onSideMenuItemClicked(item: MenuItem): Boolean


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMapBinding.inflate(inflater)
        binding.fragmentContainer.addView(inflater.inflate(getFragmentLayout(), container, false))
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.locationFab.setOnClickListener {
            moveMyMarkerAndCamera(sharedQuestDataRepository.getLastLocationStatus(), true)
        }
        setupMenuListener()
        showToolbar()
    }

    protected fun setSideMenuItemsVisible(
        userProfileVisible: Boolean = false,
        personalQuestVisible: Boolean = false,
        groupQuestVisible: Boolean = false,
        joinGroupQuestVisible: Boolean = false,
        stopQuestVisible: Boolean = false,
        logOutVisible: Boolean = false
    ) {
        navigationView?.menu?.findItem(R.id.nav_user_profile)?.isVisible = userProfileVisible
        navigationView?.menu?.findItem(R.id.nav_personal_quest)?.isVisible = personalQuestVisible
        navigationView?.menu?.findItem(R.id.nav_group_quest)?.isVisible = groupQuestVisible
        navigationView?.menu?.findItem(R.id.nav_group_quest_join)?.isVisible = joinGroupQuestVisible
        navigationView?.menu?.findItem(R.id.nav_log_out)?.isVisible = logOutVisible
        navigationView?.menu?.findItem(R.id.nav_stop_quest)?.isVisible = stopQuestVisible
    }

    protected fun closeSideMenu() {
        drawerLayout?.closeDrawers()
    }

    protected fun showToolbar() {
        baseViewModel.showToolbar()
    }

    protected fun hideToolbar() {
        baseViewModel.hideToolbar()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.apply {
            isMapToolbarEnabled = false
            isCompassEnabled = false
            isRotateGesturesEnabled = false
        }

        val padova = LatLng(45.4081455201746, 11.877832426359364)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(padova, 13f))


        val icon = MarkerIconHelper.getMarkerIcon(requireContext(), R.drawable.person_pin_circle_24px)

        myMarker = mMap.addMarker(
            MarkerOptions()
                .position(LatLng(0.0, 0.0))
                .icon(icon)

        )
        viewLifecycleOwner.repeatOnResumed {
            sharedQuestDataRepository.locationFlow.collect { locationStatus ->
                moveMyMarkerAndCamera(locationStatus)
            }
        }
    }

    private fun moveMyMarkerAndCamera(locationStatus: LocationStatus, moveCamera: Boolean = false) {
        if (locationStatus.isLocationEnabled && locationStatus.location != null) {
            val latlng = LatLng(locationStatus.location.latitude, locationStatus.location.longitude)
            myMarker?.position = latlng
            if (moveCamera || isFirstLocation) {
                isFirstLocation = false
                val zoom = max(mMap.cameraPosition.zoom, 15f)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom))
            }
        }
    }

    private fun setupMenuListener() {
        drawerLayout = activity?.findViewById<DrawerLayout>(R.id.main)
        navigationView = drawerLayout?.findViewById<NavigationView>(R.id.navigation_view)

        navigationView?.setNavigationItemSelectedListener { item ->
            onSideMenuItemClicked(item)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}