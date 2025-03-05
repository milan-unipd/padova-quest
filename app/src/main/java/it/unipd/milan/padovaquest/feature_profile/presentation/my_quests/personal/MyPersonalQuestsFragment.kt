package it.unipd.milan.padovaquest.feature_profile.presentation.my_quests.personal

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.feature_profile.presentation.my_quests.MyQuestsFragment
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest

@AndroidEntryPoint
class MyPersonalQuestsFragment : MyQuestsFragment() {
    override val viewModel: MyPersonalQuestsViewModel by viewModels()


    override fun getType(): String {
        return "personal"
    }

    override fun onQuestClicked(quest: Quest) {

    }
}