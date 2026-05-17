package com.vidyarthibus.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vidyarthibus.app.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // The Top Major Campuses in the region based on scraped data!
        val campuses = listOf(
            "St. Aloysius (City Centre / Light House Hill)",
            "NITK Surathkal",
            "MIT Manipal",
            "Mangalore University (Konaje)",
            "Yenepoya (Deralakatte / Kotekar Beeri)",
            "Sahyadri (Adyar)",
            "SJEC (Vamanjoor)",
            "Nitte (Karkala)",
            "Canara Engineering College (Benjanapadavu)"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, campuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCampus.adapter = adapter

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val savedCampus = prefs.getInt("SELECTED_CAMPUS", 0)
        binding.spinnerCampus.setSelection(savedCampus)

        binding.spinnerCampus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                prefs.edit().putInt("SELECTED_CAMPUS", position).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.cardToCampus.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("AUTO_ROUTE", true)
            intent.putExtra("CAMPUS_INDEX", binding.spinnerCampus.selectedItemPosition)
            startActivity(intent)
        }

        binding.cardFromCampus.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("CAMPUS_INDEX", binding.spinnerCampus.selectedItemPosition)
            startActivity(intent)
        }

        binding.tvSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
