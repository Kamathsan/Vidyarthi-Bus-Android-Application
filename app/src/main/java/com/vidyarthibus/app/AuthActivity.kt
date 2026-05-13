package com.vidyarthibus.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.vidyarthibus.app.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goToHome()
            return
        }

        binding.btnLogin.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (phone.length < 10 || password.length < 6) {
                Toast.makeText(this, "Enter valid phone & 6+ char password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // MOCKING PHONE AUTH WITH EMAIL/PASSWORD FOR TESTING
            val mockEmail = "$phone@vidyarthibus.app"

            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false

            // Try to sign in first
            auth.signInWithEmailAndPassword(mockEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        goToHome()
                    } else {
                        // If sign in fails, try to register them automatically!
                        auth.createUserWithEmailAndPassword(mockEmail, password)
                            .addOnCompleteListener { regTask ->
                                binding.progressBar.visibility = View.GONE
                                binding.btnLogin.isEnabled = true

                                if (regTask.isSuccessful) {
                                    Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show()
                                    goToHome()
                                } else {
                                    Toast.makeText(this, "Login Failed: Incorrect password or network error", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

}
