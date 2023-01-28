package com.mahmutalperenunal.streamingapp

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.mahmutalperenunal.streamingapp.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding

    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var handler: Handler

    private var isFullScreen: Boolean = false
    private var isLock: Boolean = false

    private lateinit var fullScreenButton: ImageView
    private lateinit var lockButton: ImageView
    private lateinit var playButton: ImageView
    private lateinit var pauseButton: ImageView


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setup toolbar
        binding.playerToolbar.title = resources.getString(R.string.app_name)
        setSupportActionBar(binding.playerToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        checkConnection()

        handler = Handler(Looper.getMainLooper())

        fullScreenButton = findViewById(R.id.fullscreen_button)
        lockButton = findViewById(R.id.lock_button)
        playButton = findViewById(R.id.play_button)
        pauseButton = findViewById(R.id.pause_button)

        simpleExoPlayer = SimpleExoPlayer.Builder(this)
            .setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000)
            .build()

        binding.videoView.player = simpleExoPlayer
        binding.videoView.keepScreenOn = true

        simpleExoPlayer.addListener(object : Player.Listener {
            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    binding.progressBar.visibility = View.VISIBLE
                    pauseButton.visibility = View.GONE
                    playButton.visibility = View.GONE
                } else if (playbackState == Player.STATE_READY) {
                    binding.progressBar.visibility = View.GONE
                    pauseButton.visibility = View.VISIBLE
                    playButton.visibility = View.GONE
                }

                if (!simpleExoPlayer.playWhenReady) {
                    handler.removeCallbacks(updateProgressAction)
                } else {
                    onProgress()
                }
            }
        })

        val videoSource = Uri.parse("https://firebasestorage.googleapis.com/v0/b/kres-app-ed36c.appspot.com/o/Video%2FSample%20Video.mp4?alt=media&token=fb83154a-5d6e-4d34-b92b-0f9478f851af")

        val mediaItem = MediaItem.fromUri(videoSource)

        simpleExoPlayer.setMediaItem(mediaItem)
        simpleExoPlayer.prepare()
        simpleExoPlayer.play()


        fullScreenButton.setOnClickListener {

            requestedOrientation = if (!isFullScreen) {
                fullScreenButton.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_full_screen_exit))
                hideSystemUI()
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                fullScreenButton.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_full_screen))
                showSystemUI()
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }

            isFullScreen = !isFullScreen

        }


        lockButton.setOnClickListener {

            if (!isLock) {
                lockButton.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_lock))
            } else {
                lockButton.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_lock))
            }

            isLock = !isLock

            lockScreen(isLock)
        }


        playButton.setOnClickListener {
            simpleExoPlayer.play()
            playButton.visibility = View.GONE
            pauseButton.visibility = View.VISIBLE
        }

        pauseButton.setOnClickListener {
            simpleExoPlayer.pause()
            pauseButton.visibility = View.GONE
            playButton.visibility = View.VISIBLE
        }
    }


    //check internet connection
    private fun checkConnection() {

        val networkConnection = NetworkConnection(applicationContext)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                AlertDialog.Builder(this, R.style.CustomAlertDialog)
                    .setTitle("İnternet Bağlantısı Yok")
                    .setMessage("Lütfen internet bağlantınızı kontrol edin!")
                    .setIcon(R.drawable.without_internet)
                    .setNegativeButton("Tamam") { dialog, _ ->
                        checkConnection()
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }

    }


    private fun onProgress() {
        val player= simpleExoPlayer
        val position: Long = player.currentPosition

        handler.removeCallbacks(updateProgressAction)

        val playbackState = player.playbackState

        if(playbackState != Player.STATE_IDLE && playbackState!= Player.STATE_ENDED) {

            var delayMs: Long

            if(player.playWhenReady && playbackState == Player.STATE_READY) {
                delayMs  = (1000 - position % 1000)
                if(delayMs < 200) {
                    delayMs += 1000
                }
            } else {
                delayMs = 1000
            }

            handler.postDelayed(updateProgressAction, delayMs)

        }
    }

    private val updateProgressAction = Runnable { onProgress() }


    private fun lockScreen(lock: Boolean) {
        val playPause = findViewById<LinearLayout>(R.id.play_pause_linearLayout)
        val progressBar = findViewById<LinearLayout>(R.id.progressBar_linearLayout)

        if (lock) {
            playPause.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
        } else {
            playPause.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(
            window,
            window.decorView.findViewById(android.R.id.content)
        ).hide(WindowInsetsCompat.Type.systemBars())
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(
            window,
            window.decorView.findViewById(android.R.id.content)
        ).show(WindowInsetsCompat.Type.systemBars())
    }


    override fun onStop() {
        super.onStop()
        simpleExoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        simpleExoPlayer.release()
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer.pause()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if(isLock) return

        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fullScreenButton.performClick()
        } else {
            val intent = Intent(applicationContext, ChannelsActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

    }
}