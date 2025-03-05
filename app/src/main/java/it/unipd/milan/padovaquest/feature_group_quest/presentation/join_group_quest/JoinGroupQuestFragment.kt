package it.unipd.milan.padovaquest.feature_group_quest.presentation.join_group_quest

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.core.util.repeatOnResumed
import it.unipd.milan.padovaquest.databinding.FragmentJoinGroupQuestBinding
import it.unipd.milan.padovaquest.feature_group_quest.domain.model.GroupQuestStatus

@AndroidEntryPoint
class JoinGroupQuestFragment : Fragment() {


    private val viewModel: JoinGroupQuestViewModel by viewModels()
    private lateinit var binding: FragmentJoinGroupQuestBinding

    private var isWaiting = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentJoinGroupQuestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isWaiting) {
                    findNavController().navigate(R.id.action_joinGroupQuestFragment_to_walkFragment)
                }
            }
        })

        viewLifecycleOwner.repeatOnResumed {
            viewModel.getGroupQuestStatusFlow().collect { status ->


                binding.groupQuestJoinProgressBar.visibility = if (status.isJoining || status.isLeaving)
                    View.VISIBLE
                else
                    View.GONE

                if(status.isJoining)
                    isWaiting = true

                if (status.shouldJoin) {
                    isWaiting = true
                    binding.questCodeEditText.setText(viewModel.getQuest().id)
                    viewModel.joinGroupQuest(viewModel.getQuest().id!!)
                    return@collect
                }

                if (status.isWaitingToStart) {
                    isWaiting = true
                    binding.questCodeEditText.setText(viewModel.getQuest().id)
                    binding.joinGroupQuestBtn.isEnabled = false
                    binding.questCodeEditText.isEnabled = false
                    binding.waitingGroupTextView.visibility = View.VISIBLE
                    binding.leaveGroupQuestBtn.visibility = View.VISIBLE
                    return@collect
                }

                if (status.hasStarted) {
                    findNavController().navigate(R.id.action_joinGroupQuestFragment_to_questFragment)
                    return@collect
                }

                if (status.hasLeft) {
                    viewModel.setGroupQuestStatus(GroupQuestStatus())
                    findNavController().navigate(R.id.action_joinGroupQuestFragment_to_walkFragment)
                    return@collect
                }

                if (status.wasDeleted) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Group Quest Deleted")
                        .setMessage("Ooops! The quest you were trying to join has been deleted.")
                        .setNeutralButton("OK", null)
                        .setCancelable(false)
                        .show()
                    viewModel.setGroupQuestStatus(GroupQuestStatus())
                    findNavController().navigate(R.id.action_joinGroupQuestFragment_to_walkFragment)
                    return@collect
                }

                if (status.exception != null) {
                    isWaiting = false
                    Toast.makeText(context, status.exception.message, Toast.LENGTH_SHORT).show()
                    return@collect
                }
            }
        }

        binding.joinGroupQuestBtn.setOnClickListener {
            val questCode = binding.questCodeEditText.text.toString().trim()
            if (questCode.isEmpty()) {
                Toast.makeText(context, "You have to enter a quest code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.joinGroupQuest(questCode)
        }

        binding.leaveGroupQuestBtn.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Group Quest")
                .setMessage("Are you sure you want to leave the quest?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.leaveGroupQuest()
                }
                .setNeutralButton("No", null)
                .setCancelable(false)
                .show()
        }

    }
}