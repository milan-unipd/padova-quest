package it.unipd.milan.padovaquest.feature_profile.presentation.my_quests.group

import android.content.Intent
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.feature_profile.presentation.my_quests.MyQuestsFragment
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest

@AndroidEntryPoint
class MyGroupQuestsFragment : MyQuestsFragment() {

    override val viewModel: MyGroupQuestsViewModel by viewModels()
    override fun getType(): String {
        return "group"
    }

    override fun onQuestClicked(quest: Quest) {
        startActivity(Intent(requireContext(), GroupQuestResultsActivity::class.java).apply {
            putExtra("questID", quest.id)
        })
    }


}