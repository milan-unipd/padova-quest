package it.unipd.milan.padovaquest.feature_group_quest.presentation.join_group_quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unipd.milan.padovaquest.feature_group_quest.domain.model.GroupQuestStatus
import it.unipd.milan.padovaquest.feature_group_quest.domain.use_case.GroupQuestUseCases
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinGroupQuestViewModel @Inject constructor(
    private val sharedQuestDataRepository: SharedQuestDataRepository,
    private val groupQuestUseCases: GroupQuestUseCases
) : ViewModel() {

    fun getQuest(): Quest {
        return sharedQuestDataRepository.quest!!
    }

    fun getGroupQuestStatusFlow() = sharedQuestDataRepository.groupQuestStatus

    fun joinGroupQuest(questID: String) {
        viewModelScope.launch {
            groupQuestUseCases.joinGroupQuestUseCase(Firebase.auth.currentUser!!.uid, questID)
        }
    }

    fun leaveGroupQuest() {
        viewModelScope.launch {
            groupQuestUseCases.leaveGroupQuestUseCase(Firebase.auth.currentUser!!.uid)
        }
    }

    fun setGroupQuestStatus(groupQuestStatus: GroupQuestStatus) {
        sharedQuestDataRepository.setGroupQuestStatus(groupQuestStatus)
    }

    companion object {
        var questCodeByLink: String? = null
    }
}