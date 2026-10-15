package com.shafi.appfeedback.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shafi.app_feedback.AppFeedback
import com.shafi.appfeedback.databinding.ActivityMainBinding

/**
 * Created by Shafi on 14/10/2026.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        AppFeedback.check(this)
        onClickListeners()
    }

    private fun onClickListeners() {

        //feedback
        binding.feedbackBtn.setOnClickListener {
            AppFeedback.show(this)
        }
    }
}
