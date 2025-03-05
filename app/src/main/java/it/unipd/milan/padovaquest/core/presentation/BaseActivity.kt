package it.unipd.milan.padovaquest.core.presentation

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.R
import it.unipd.milan.padovaquest.databinding.ActivityBaseBinding
import it.unipd.milan.padovaquest.shared_quests.presentation.service.QuestService
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBaseBinding
    private val viewModel: BaseViewModel by viewModels()

    private lateinit var networkObserver: ConnectivityObserver

    override fun onResume() {
        super.onResume()
        QuestService.isInForeground = true
    }

    override fun onPause() {
        QuestService.isInForeground = false
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For devices running Android Q (API 29) or higher
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            // For devices running below Android Q
            getSharedPreferences("app_settings", MODE_PRIVATE)
                .edit()
                .putInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
                .apply()
        }

        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.toolbar.setBackgroundColor(Color.argb(150, 255, 255, 255))
        setSupportActionBar(binding.toolbar)

        val drawerLayout = binding.root
        val toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()




        lifecycleScope.launch {
            launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {

                    viewModel.toolbarState.collect { visible ->
                        binding.toolbar.isVisible = visible
                        binding.navigationView.getHeaderView(0)?.findViewById<TextView>(R.id.menu_header_textview)?.text = Firebase.auth.currentUser?.displayName
                        val lockMode = if (visible) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                        drawerLayout.setDrawerLockMode(lockMode)
                    }
                }
            }
            launch {
                networkObserver = ConnectivityObserver(applicationContext)
                networkObserver.networkStatusFlow.collect { isConnected ->
                    runOnUiThread {
                        if (isConnected) {
                            binding.internetLostTextView.visibility = View.GONE
                        } else {
                            binding.internetLostTextView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}