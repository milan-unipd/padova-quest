package it.unipd.milan.padovaquest.feature_group_quest.presentation.quest_start_fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unipd.milan.padovaquest.feature_group_quest.domain.use_case.GroupQuestUseCases
import it.unipd.milan.padovaquest.shared_quests.presentation.service.SharedQuestDataRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupQuestStartViewModel @Inject constructor(
    private val groupQuestUseCases: GroupQuestUseCases,
    val sharedQuestDataRepository: SharedQuestDataRepository
) : ViewModel() {


    fun createGroupQuest() {
        viewModelScope.launch {
            groupQuestUseCases.createGroupQuestUseCase(Firebase.auth.currentUser!!.uid)
        }

    }

    fun startGroupQuest() {
        viewModelScope.launch {
            groupQuestUseCases.startGroupQuestUseCase()
        }
    }

    fun deleteGroupQuest() {
        viewModelScope.launch {
            groupQuestUseCases.deleteGroupQuestUseCase(Firebase.auth.currentUser!!.uid)
        }
    }
}