package it.unipd.milan.padovaquest.feature_profile.presentation.my_quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.databinding.FragmentMyQuestsBinding
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

abstract class MyQuestsFragment : Fragment() {

    private lateinit var binding: FragmentMyQuestsBinding

    private lateinit var questAdapter: QuestAdapter
    private val windowSize = 10  // Number of items to load at a time
    private val prefetchThreshold = 5

    protected abstract val viewModel: MyQuestsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyQuestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @FlowPreview
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questAdapter = QuestAdapter(requireContext(), mutableListOf()) { quest ->
            onQuestClicked(quest)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = questAdapter


        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.questsFlow.collect {
                    if (it is Resource.Success) {
                        questAdapter.addItems(it.result)
                    } else if (it is Resource.Error) {
                        Toast.makeText(requireContext(), "Error loading quests", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            launch {
                viewModel.scrollState
                    .debounce(500)
                    .collectLatest { scrollValue ->
                        viewModel.getQuests(scrollValue, windowSize, getType())
                    }
            }
        }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                if (totalItemCount > 0 && lastVisibleItemPosition >= totalItemCount - prefetchThreshold) {

                    viewModel.onScrollChanged(totalItemCount)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onScrollChanged(0)

    }


    abstract fun getType(): String
    abstract fun onQuestClicked(quest: Quest)

}