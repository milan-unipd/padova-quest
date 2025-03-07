package it.unipd.milan.padovaquest.feature_profile.domain.use_cases

import android.annotation.SuppressLint
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.feature_profile.domain.GroupQuestResult
import it.unipd.milan.padovaquest.feature_profile.domain.repo.MyProfileRepository
import it.unipd.milan.padovaquest.shared_quests.domain.model.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GetGroupQuestResultsUseCase @Inject constructor(
    private val myProfileRepository: MyProfileRepository
) {

    suspend operator fun invoke(questID: String) = withContext(Dispatchers.IO) {

        val result = myProfileRepository.getGroupQuestResults(questID)
        if (result is Resource.Error) {
            return@withContext Resource.Error(result.exception)
        }

        result as Resource.Success
        val questResultDataModel = result.result

        val sdf = SimpleDateFormat("dd MMM yyyy 'at' HH:mm", Locale.getDefault())


        val durations = mutableMapOf<String, Long?>()
        val date0Time = Date(0).time
        questResultDataModel.finishTimes.entries.forEach {
            if (it.value == null) {
                durations[it.key] = null
            } else if (it.value!!.time != date0Time) {
                durations[it.key] = it.value!!.time - questResultDataModel.createdOn.time
            } else {
                durations[it.key] = Long.MAX_VALUE
            }
        }

        val sortedUsers = questResultDataModel.users.sortedWith(
            compareByDescending<Person> { it.numOfCorrectAnswers }
                .thenBy { durations[it.id] ?: Long.MAX_VALUE },

            ).map { user ->
            if (durations[user.id] == null) {
                "${questResultDataModel.users.indexOf(user) + 1}. " +
                        "${user.name}\n\t\t " +
                        "Still playing"

            } else if (durations[user.id] != Long.MAX_VALUE) {
                "${questResultDataModel.users.indexOf(user) + 1}. " +
                        "${user.name}\n\t\t " +
                        "Correct answers: ${user.numOfCorrectAnswers}\n\t\t " +
                        "Time: ${formatDuration(durations[user.id])}"
            } else {
                "${questResultDataModel.users.indexOf(user) + 1}. " +
                        "${user.name}\n\t\t " +
                        "Dropped out"
            }
        }.toMutableList()


        val groupQuestResult = GroupQuestResult(
            sortedUsers,
            createdOn = sdf.format(questResultDataModel.createdOn),
            places = questResultDataModel.places,

            )

        return@withContext Resource.Success(groupQuestResult)
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(durationMillis: Long?): String {
        if (durationMillis == null)
            return "Not finished yet"
        val hours = durationMillis / 3_600_000
        val minutes = (durationMillis % 3_600_000) / 60_000
        val seconds = (durationMillis % 60_000) / 1_000
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}