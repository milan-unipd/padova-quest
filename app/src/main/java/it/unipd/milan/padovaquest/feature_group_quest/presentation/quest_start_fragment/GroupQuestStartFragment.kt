package it.unipd.milan.padovaquest.feature_group_quest.presentation.quest_start_fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.core.util.repeatOnResumed
import it.unipd.milan.padovaquest.databinding.FragmentGroupQuestStartBinding

@AndroidEntryPoint
class GroupQuestStartFragment : Fragment() {


    private val viewModel: GroupQuestStartViewModel by viewModels()
    private lateinit var binding: FragmentGroupQuestStartBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupQuestStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.repeatOnResumed {
            viewModel.sharedQuestDataRepository.groupQuestStatus.collect { questStatus ->

                binding.groupQuestStartProgressBar.visibility =
                    if (questStatus.isBeingCreated || questStatus.isBeingDeleted || questStatus.isStarting)
                        View.VISIBLE
                    else
                        View.GONE

                if (questStatus.shouldCreate) {
                    viewModel.createGroupQuest()
                    return@collect
                }

                if (questStatus.hasBeenCreated) {
                    binding.groupQuestCodeTextView.text = viewModel.sharedQuestDataRepository.quest?.id
                    return@collect
                }

                if (questStatus.wasDeleted) {
                    findNavController().navigate(R.id.action_groupQuestStartFragment_to_walkFragment)
                    return@collect
                }

                if (questStatus.hasStarted) {
                    findNavController().navigate(R.id.action_groupQuestStartFragment_to_questFragment)
                    return@collect
                }

                if (questStatus.exception != null) {
                    Toast.makeText(requireContext(), questStatus.exception.message, Toast.LENGTH_SHORT).show()
                    return@collect
                }
            }
        }

        binding.groupQuestCodeShareBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            val questCode = binding.groupQuestCodeTextView.text.toString()
            intent.putExtra(Intent.EXTRA_TEXT, "Hey, join in my Padova Group Quest, just click the link: \n " +
                    "http://padova-quest.com?quest-code=$questCode")
            context?.startActivity(Intent.createChooser(intent, "Share via"))
        }

        binding.startGroupQuestBtn.setOnClickListener {
            viewModel.startGroupQuest()
        }

        binding.groupQuestDeleteBtn.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Quest")
                .setMessage("Are you sure you want to delete the quest?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.deleteGroupQuest()
                }
                .setNegativeButton("No", null)
                .setCancelable(false)
                .show()
        }
    }
}