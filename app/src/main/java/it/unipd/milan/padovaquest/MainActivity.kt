package it.unipd.milan.padovaquest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.core.presentation.BaseActivity
import it.unipd.milan.padovaquest.feature_group_quest.presentation.join_group_quest.JoinGroupQuestViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intent?.data?.let { uri ->
            val questCode = uri.getQueryParameter("quest-code")
            if (questCode != null && FirebaseAuth.getInstance().currentUser != null) {
                JoinGroupQuestViewModel.questCodeByLink = questCode
            }
        }

        startActivity(Intent(this, BaseActivity::class.java))
        finish()


    }
}