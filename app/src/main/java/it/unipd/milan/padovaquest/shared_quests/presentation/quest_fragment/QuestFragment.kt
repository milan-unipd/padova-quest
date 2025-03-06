package it.unipd.milan.padovaquest.shared_quests.presentation.quest_fragment

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
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.core.util.repeatOnResumed
import it.unipd.milan.padovaquest.databinding.FragmentQuestBinding
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import it.unipd.milan.padovaquest.shared_quests.presentation.map_fragment.MapFragment
import it.unipd.milan.padovaquest.shared_quests.presentation.map_fragment.MarkerIconHelper
import it.unipd.milan.padovaquest.shared_quests.presentation.trivia_fragment.TriviaViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuestFragment : MapFragment() {

    override fun getFragmentLayout(): Int = R.layout.fragment_quest


    private lateinit var binding: FragmentQuestBinding
    private val viewModel: QuestFragmentViewModel by viewModels()
    private val triviaViewModel: TriviaViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentQuestBinding.inflate(inflater, container, false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val quest = sharedQuestDataRepository.quest

        if (quest?.id == null || quest.finishedOn != null) {
            findNavController().navigate(R.id.action_questFragment_to_walkFragment)
            return
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSideMenuItemsVisible(stopQuestVisible = true)
        showToolbar()

    }


    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        viewLifecycleOwner.lifecycleScope.launch {
            val quest = sharedQuestDataRepository.quest

            if (quest?.id == null)
                return@launch

            quest.places.forEach { place ->
                val icon = if (place.visited) {
                    if (quest.answers[place.id]!!.values.contains(0))
                        MarkerIconHelper.getMarkerIcon(requireContext(), R.drawable.location_pin_done_24px)
                    else
                        MarkerIconHelper.getMarkerIcon(requireContext(), R.drawable.location_pin_wrong_answer_24px)
                } else
                    MarkerIconHelper.getMarkerIcon(requireContext(), R.drawable.location_pin_24px)

                mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(place.location.latitude, place.location.longitude))
                        .title(place.name)
                        .icon(icon))
            }

            repeatOnResumed {
                sharedQuestDataRepository.nearestPlaceFlow.collect { place ->
                    if (place == null)
                        return@collect
                    showTriviaQuestion(place)

                }
            }
        }
        sharedQuestDataRepository.emitLastLocationAgain()

    }

    private fun showTriviaQuestion(place: Place) {
        triviaViewModel.place = place
        hideToolbar()
        findNavController().navigate(R.id.action_questFragment_to_triviaFragment)
    }

    override fun onSideMenuItemClicked(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_stop_quest -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Stop quest")
                    .setMessage("Are you sure you want to stop the quest?")
                    .setPositiveButton("Yes") { _, _ ->
                        cancelQuest()
                    }
                    .setNegativeButton("No", null)
                    .show()
                true
            }

            else -> false
        }
    }

    private fun cancelQuest() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cancelQuest(sharedQuestDataRepository.quest!!)?.collect {
                when (it) {
                    is Resource.Error -> Toast.makeText(requireContext(), it.exception.message, Toast.LENGTH_SHORT).show()
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        findNavController().navigate(R.id.action_questFragment_to_walkFragment)
                        closeSideMenu()
                    }
                }
            }
        }
    }
}