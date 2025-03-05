package it.unipd.milan.padovaquest.shared_quests.presentation.trivia_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.databinding.FragmentTriviaBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TriviaFragment : Fragment() {

    private val viewModel: TriviaViewModel by activityViewModels()
    private lateinit var binding: FragmentTriviaBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTriviaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val quest = viewModel.sharedQuestDataRepository.quest!!
        val question = quest.questions[viewModel.place.id]!!


        binding.landmarkTriviaTitleTextview.text = viewModel.place.name
        binding.landmarkTriviaQuestionTextview.text = question.question

        listOf(
            binding.triviaButton1,
            binding.triviaButton2,
            binding.triviaButton3
        ).shuffled().forEachIndexed { i, button ->
            button.text = question.answers[i]
            button.setOnClickListener {
                viewModel.answerQuestion(question.id!!, i)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.answerFlow.collect { resource ->
                if (resource == null)
                    return@collect
                viewModel.emptyAnswerFlow()
                viewModel.sharedQuestDataRepository.emmitNearestPlaceFlow(null)

                binding.triviaButton1.isEnabled = resource !is Resource.Loading
                binding.triviaButton2.isEnabled = resource !is Resource.Loading
                binding.triviaButton3.isEnabled = resource !is Resource.Loading

                when (resource) {
                    is Resource.Loading -> Unit
                    is Resource.Error -> Toast.makeText(requireContext(), resource.exception.message, Toast.LENGTH_SHORT).show()
                    is Resource.Success -> {

                        val dialog = MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Answer sent")
                            .setCancelable(false)
                            .setNeutralButton("OK") { _, _ ->
                                findNavController().navigate(R.id.action_triviaFragment_to_questFragment)
                            }

                        if (resource.result) { //answered correctly
                            dialog.setMessage("You answered correctly!")
                        } else {
                            dialog.setMessage("Sorry, the answer wasn't correct")
                        }

                        dialog.setOnCancelListener {
                            findNavController().navigate(R.id.action_triviaFragment_to_questFragment)
                        }

                        dialog.show()

                    }
                }
            }
        }
    }
}