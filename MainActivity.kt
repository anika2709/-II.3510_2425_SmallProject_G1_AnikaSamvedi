package com.example.kotlinmusicapp

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private val handler = Handler() // Handler to update SeekBar
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize MediaPlayer with the audio resource "calm_down"
        mediaPlayer = MediaPlayer.create(this, R.raw.calmdown)

        // Set up the padding for edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SeekBar and set initial progress and max
        seekBar = findViewById(R.id.seekbar)
        seekBar.progress = 0
        seekBar.max = mediaPlayer.duration

        // Set up play button
        val playBtn = findViewById<View>(R.id.play_btn)
        playBtn.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                playBtn.setBackgroundResource(R.drawable.baseline_pause_24)
                updateSeekBar()
            } else {
                mediaPlayer.pause()
                playBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24)
                handler.removeCallbacks(updateRunnable)
            }
        }

        // SeekBar change listener to update track position
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateRunnable) // Stop updating while user is interacting
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer.seekTo(seekBar?.progress ?: 0)
                updateSeekBar() // Resume updating after user interaction
            }
        })

        // Runnable to update SeekBar as the track progresses
        runnable = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(runnable, 1000) // Update every second
        }

        // Start updating SeekBar when playback starts
        handler.postDelayed(runnable, 1000)

        // Reset SeekBar and play button when the track completes
        mediaPlayer.setOnCompletionListener {
            playBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24)
            seekBar.progress = 0
            handler.removeCallbacks(runnable)
        }
    }

    // Runnable to update SeekBar
    private val updateRunnable = object : Runnable {
        override fun run() {
            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(this, 1000)
        }
    }

    // Start updating SeekBar
    private fun updateSeekBar() {
        handler.post(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release() // Release MediaPlayer resources
        handler.removeCallbacks(updateRunnable) // Stop SeekBar updates
    }
}
