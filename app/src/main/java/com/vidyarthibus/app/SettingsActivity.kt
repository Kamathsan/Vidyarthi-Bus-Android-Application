package com.vidyarthibus.app

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.vidyarthibus.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        binding.toolbarSettings.setNavigationOnClickListener {
            finish() // Goes back to HomeActivity
        }

        // Display current user logic
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            binding.tvStudentEmail.text = currentUser.email ?: currentUser.phoneNumber ?: "Authenticated User"
        }

        // Tools Logic
        binding.btnClearCache.setOnClickListener {
            // Clears the OSMDroid map cache
            val prefs = getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Toast.makeText(this, "Offline Map Cache Cleared Successfully", Toast.LENGTH_SHORT).show()
        }

        binding.btnResetCampus.setOnClickListener {
            // Clears the saved campus preference
            val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            prefs.edit().remove("SELECTED_CAMPUS").apply()
            Toast.makeText(this, "Default Campus Reset", Toast.LENGTH_SHORT).show()
        }
    }
}
