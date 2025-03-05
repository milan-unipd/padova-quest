package it.unipd.milan.padovaquest.feature_walking.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.core.presentation.BaseViewModel
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.core.util.repeatOnResumed
import it.unipd.milan.padovaquest.databinding.FragmentWalkBinding
import it.unipd.milan.padovaquest.feature_group_quest.domain.model.GroupQuestStatus
import it.unipd.milan.padovaquest.feature_group_quest.presentation.join_group_quest.JoinGroupQuestViewModel
import it.unipd.milan.padovaquest.feature_profile.presentation.ProfileActivity
import it.unipd.milan.padovaquest.feature_profile.presentation.my_quests.group.GroupQuestResultsActivity
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.presentation.map_fragment.MapFragment
import it.unipd.milan.padovaquest.shared_quests.presentation.service.QuestService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WalkFragment : MapFragment() {


    private val viewModel: WalkViewModel by viewModels()
    private val baseViewModel: BaseViewModel by activityViewModels()
    private lateinit var binding: FragmentWalkBinding

    private val markerMap = mutableMapOf<String, Marker>()


    override fun getFragmentLayout(): Int = R.layout.fragment_walk


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWalkBinding.inflate(inflater)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.repeatOnResumed {
            if (Firebase.auth.currentUser == null) {
                baseViewModel.logOut()
                val intent = Intent(requireContext(), QuestService::class.java)
                requireContext().stopService(intent)
                findNavController().navigate(R.id.action_walkFragment_to_loginFragment)
                return@repeatOnResumed
            }

            if (JoinGroupQuestViewModel.questCodeByLink != null) {
                val questCodeByLink = JoinGroupQuestViewModel.questCodeByLink
                JoinGroupQuestViewModel.questCodeByLink = null
                sharedQuestDataRepository.quest = Quest(id = questCodeByLink, type = "group", status = "created")
                sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(shouldJoin = true))
                hideToolbar()
            }

            val quest = sharedQuestDataRepository.quest

            if (quest?.id == null) {
                sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus())
                return@repeatOnResumed
            }

            if (quest.finishedOn != null) {
                if (quest.type == "group") {
                    val questID = quest.id!!
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Quest Finished")
                        .setMessage("You've finished the quest! Do you want to check out the results?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes") { _, _ ->
                            startActivity(Intent(requireContext(), GroupQuestResultsActivity::class.java).apply {
                                putExtra("questID", questID)
                            })
                        }.setCancelable(false)
                        .show()
                } else if (quest.type == "personal") {
                    var message = "You've finished the quest!\n" +
                            "You got ${quest.numOfCorrectAnswers} correct answers."

                    if (quest.numOfCorrectAnswers == quest.answers.size) {
                        message += "\nPerfect job!!"
                    } else if (quest.numOfCorrectAnswers >= quest.answers.size * 0.6) {
                        message += "\nGood job!!"
                    }

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Quest Finished")
                        .setMessage(message)
                        .setNeutralButton("OK", null)
                        .setCancelable(false)
                        .show()

                }
                sharedQuestDataRepository.quest = Quest()

                return@repeatOnResumed
            }

            if (quest.type == "personal") {
                findNavController().navigate(R.id.action_walkFragment_to_questFragment)
                return@repeatOnResumed
            } else if (quest.type == "group") {
                if (quest.status == "created") {
                    if (quest.createdBy == Firebase.auth.currentUser!!.uid) {
                        findNavController().navigate(R.id.action_walkFragment_to_groupQuestStartFragment)
                        return@repeatOnResumed
                    } else {
                        findNavController().navigate(R.id.action_walkFragment_to_joinGroupQuestFragment)
                        return@repeatOnResumed
                    }
                } else if (quest.status == "started") {
                    findNavController().navigate(R.id.action_walkFragment_to_questFragment)
                    return@repeatOnResumed
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnResumed {
                viewModel.placesFlow.collectLatest { places ->
                    showPlacesOnMap(places)
                }
            }

            repeatOnResumed {
                sharedQuestDataRepository.locationFlow.collect { locationStatus ->
                    if (!locationStatus.isLocationEnabled || locationStatus.location == null)
                        return@collect

                    viewModel.getNearPlace(locationStatus.location)

                }
            }


            repeatOnResumed {
                sharedQuestDataRepository.nearestPlaceFlow.collect { place ->
                    if (place == null || sharedQuestDataRepository.quest?.id != null)
                        return@collect
                    sharedQuestDataRepository.emmitNearestPlaceFlow(null)
                    showDescriptionDialog(place, true)
                    viewModel.setPlaceSeen(place.id)
                }
            }
        }

        setSideMenuItemsVisible(userProfileVisible = true, personalQuestVisible = true, groupQuestVisible = true, joinGroupQuestVisible = true, logOutVisible = true)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        mMap.setOnCameraIdleListener {
            val bounds = mMap.projection.visibleRegion.latLngBounds
            viewModel.getNearbyPlaces(bounds)
        }

        mMap.setOnMarkerClickListener { clickedMarker ->
            if (clickedMarker.id == myMarker?.id)
                return@setOnMarkerClickListener false

            val place = viewModel.getPlace(markerMap.entries.find { it.value.id == clickedMarker.id }?.key)
            if (place == null)
                return@setOnMarkerClickListener false

            showDescriptionDialog(place)
            true
        }
    }

    private fun showPlacesOnMap(places: List<Place>) {
        val placeIds = places.map { it.id }.toSet()

        places.forEach { place ->
            val position = LatLng(place.location.latitude, place.location.longitude)
            val existingMarker = markerMap[place.id]

            if (existingMarker == null) {
                val newMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(place.name)

                )

                markerMap[place.id] = newMarker!!
            } else {
                existingMarker.position = position
            }
        }

        val iterator = markerMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!placeIds.contains(entry.key)) {
                entry.value.remove()
                iterator.remove()
            }
        }
    }

    private fun showDescriptionDialog(place: Place, shouldShowYouAreNear: Boolean = false) {
        val bottomSheet = DescriptionDialogFragment(place, shouldShowYouAreNear)
        bottomSheet.show(parentFragmentManager, "DescriptionDialog")
    }


    override fun onSideMenuItemClicked(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_user_profile -> {
                startActivity(Intent(requireContext(), ProfileActivity::class.java))
                true
            }

            R.id.nav_personal_quest -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Personal Quest")
                    .setMessage("Do you want to start a personal quest?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes") { _, _ -> startPersonalQuest() }
                    .show()
                true
            }

            R.id.nav_group_quest -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Group Quest")
                    .setMessage("Do you want to start a group quest?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes") { _, _ ->
                        sharedQuestDataRepository.setGroupQuestStatus(GroupQuestStatus(shouldCreate = true))
                        closeSideMenu()
                        hideToolbar()
                        findNavController().navigate(R.id.action_walkFragment_to_groupQuestStartFragment)
                    }
                    .show()
                true
            }

            R.id.nav_group_quest_join -> {
                closeSideMenu()
                hideToolbar()
                findNavController().navigate(R.id.action_walkFragment_to_joinGroupQuestFragment)
                true
            }

            R.id.nav_log_out -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Log out")
                    .setMessage("Are you sure you want to log out?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes") { _, _ ->
                        baseViewModel.logOut()
                        val intent = Intent(requireContext(), QuestService::class.java)
                        requireContext().stopService(intent)
                        findNavController().navigate(R.id.action_walkFragment_to_loginFragment)
                    }
                    .show()
                true
            }

            else -> false
        }
    }

    private fun startPersonalQuest() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.startPersonalQuest().collect { result ->
                when (result) {
                    is Resource.Error -> Toast.makeText(requireContext(), result.exception.message, Toast.LENGTH_SHORT).show()
                    Resource.Loading -> Unit
                    is Resource.Success -> {
                        findNavController().navigate(R.id.action_walkFragment_to_questFragment)
                        closeSideMenu()
                        sharedQuestDataRepository.emitLastLocationAgain()
                    }
                }

            }
        }
    }
}