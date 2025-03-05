package it.unipd.milan.padovaquest.feature_profile.presentation.my_quests.group

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.core.util.repeatOnResumed
import it.unipd.milan.padovaquest.databinding.ActivityGroupQuestResultsBinding

@AndroidEntryPoint
class GroupQuestResultsActivity : AppCompatActivity() {


    private val viewModel: GroupQuestResultsViewModel by viewModels()
    private lateinit var binding: ActivityGroupQuestResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupQuestResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        if (bundle == null || !bundle.containsKey("questID")) {
            finish()
            return
        }

        val questID = bundle.getString("questID")!!

        viewModel.getQuestResults(questID)


        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            mutableListOf<String>()
        )
        binding.listview.adapter = adapter

        repeatOnResumed {
            viewModel.questResultsFlow.collect {
                binding.progressBar.visibility = if (it is Resource.Loading) View.VISIBLE else View.GONE
                when (it) {
                    is Resource.Error -> {
                        Toast.makeText(this, "Error loading quest results", Toast.LENGTH_SHORT).show()
                        finish()
                    }

                    is Resource.Loading -> Unit

                    is Resource.Success -> {
                        val result = it.result
                        binding.dateTextView.text = result.createdOn
                        binding.placesTextView.text = result.places.joinToString(",")
                        val items = result.scoreBoard
                        adapter.addAll(items)
                        adapter.notifyDataSetChanged()
                    }

                    null -> {
                        return@collect
                    }
                }
            }
        }

    }
}