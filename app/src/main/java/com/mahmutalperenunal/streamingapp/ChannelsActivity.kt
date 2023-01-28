package com.mahmutalperenunal.streamingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mahmutalperenunal.streamingapp.databinding.ActivityChannelsBinding

class ChannelsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChannelsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setup toolbar
        binding.channelsToolbar.title = resources.getString(R.string.app_name)
        setSupportActionBar(binding.channelsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        //open camera
        binding.channel1.setOnClickListener {
            val intent = Intent(applicationContext, VideoPlayerActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    //exit app on back button pressed
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}