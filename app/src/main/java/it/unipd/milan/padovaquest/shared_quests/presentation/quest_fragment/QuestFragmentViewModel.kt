package it.unipd.milan.padovaquest.shared_quests.presentation.quest_fragment

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_group_quest.domain.use_case.GroupQuestUseCases
import it.unipd.milan.padovaquest.feature_personal_quest.domain.use_case.PersonalQuestUseCases
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class QuestFragmentViewModel @Inject constructor(
    private val personalQuestUseCases: PersonalQuestUseCases,
    private val groupQuestUseCases: GroupQuestUseCases
) : ViewModel() {

    suspend fun cancelQuest(quest:Quest): Flow<Resource<Unit>>?{
        return when (quest.type) {
            "personal" -> personalQuestUseCases.cancelQuestUseCase()
            "group" -> groupQuestUseCases.cancelQuestUseCase(Firebase.auth.currentUser!!.uid)
            else -> null
        }

    }


}