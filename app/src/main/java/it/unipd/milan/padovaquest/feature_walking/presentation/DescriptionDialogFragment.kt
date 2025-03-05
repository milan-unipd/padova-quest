package it.unipd.milan.padovaquest.feature_walking.presentation

import android.app.Dialog
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.core.util.Resource
import it.unipd.milan.padovaquest.databinding.FragmentDescriptionDialogBinding
import it.unipd.milan.padovaquest.feature_walking.domain.use_case.WalkUseCases
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DescriptionDialogFragment(
    private val place: Place,
    private val shouldShowYouAreNear: Boolean = false
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentDescriptionDialogBinding

    @Inject
    lateinit var walkUseCases: WalkUseCases

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDescriptionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (shouldShowYouAreNear) {
            val notification: Ringtone = RingtoneManager.getRingtone(
                context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            )
            notification.play()
            binding.youAreNear.visibility = View.VISIBLE
        }

        binding.title.text = place.name

        lifecycleScope.launch {

            walkUseCases.getPlaceDescriptionUseCase(place.id).collect { description ->
                binding.progressBar.visibility = if (description is Resource.Loading) View.VISIBLE else View.GONE
                when (description) {
                    Resource.Loading -> Unit
                    is Resource.Error -> dismiss()
                    is Resource.Success -> binding.scrollableTextView.text = Html.fromHtml(description.result, Html.FROM_HTML_MODE_LEGACY)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setDimAmount(0.8f)


        }
    }


}