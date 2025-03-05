package it.unipd.milan.padovaquest.feature_profile.presentation.my_quests

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.shared_quests.domain.model.Quest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuestAdapter(
    private val context: Context,
    private val quests: MutableList<Quest>,
    private val onQuestClicked: (Quest) -> Unit
) : RecyclerView.Adapter<QuestAdapter.QuestViewHolder>() {

    private val sdf = SimpleDateFormat("dd MMM yyyy 'at' HH:mm", Locale.getDefault())


    // ViewHolder to hold references to UI elements in the card
    inner class QuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questCardView: MaterialCardView = itemView.findViewById(R.id.questCardView)
        val createdByTextView: TextView = itemView.findViewById(R.id.createdByTextView)
        val createdOnTextView: TextView = itemView.findViewById(R.id.createdOnTextView)
        val finishedOnTextView: TextView = itemView.findViewById(R.id.finishedOnTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val placesTextView: TextView = itemView.findViewById(R.id.placesTextView)
        val numOfCorrectAnswersTextView: TextView = itemView.findViewById(R.id.numOfCorrectAnswersTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.quest_item, parent, false)
        return QuestViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val quest = quests[position]

        if (quest.type == "personal") {
            holder.createdByTextView.visibility = View.GONE
        } else if (quest.type == "group") {
            holder.createdByTextView.text = "Created By: ${quest.creatorName}"
        }

        holder.createdOnTextView.text = "Created On: ${formatDate(quest.createdOn)}"
        holder.finishedOnTextView.text = "Finished On: ${formatDate(quest.finishedOn)}"
        holder.placesTextView.text = "Places: ${quest.places.joinToString(", ") { it.name }}"
        holder.statusTextView.text = if (quest.answers.size == quest.questions.size) "Completed" else "Canceled"
        holder.numOfCorrectAnswersTextView.text = "Correct Answers: ${quest.numOfCorrectAnswers}"

        holder.questCardView.setOnClickListener {
            onQuestClicked(quest)
        }
    }

    private fun formatDate(date: Date?): String {
        if (date == null) return "Not Available"
        return sdf.format(date)
    }

    override fun getItemCount(): Int {
        return quests.size
    }

    // Add new items to the end of the window
    fun addItems(newItems: List<Quest>) {
        val startIndex = quests.size
        quests.addAll(newItems)
        notifyItemRangeInserted(startIndex, newItems.size)
    }
}