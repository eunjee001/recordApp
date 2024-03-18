package com.kkyoungs.recordapp

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kkyoungs.recordapp.databinding.ActivityRecordingListBinding
import java.io.File
import java.io.IOException


class RecordingListActivity(pri) : AppCompatActivity() {
    private lateinit var binding : ActivityRecordingListBinding
    private var player : MediaPlayer ?= null

    /** 리사이클러뷰  */
    private var audioAdapter: RecordingAdapter? = null
    private var isPlaying = false
    var playIcon : ImageView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init(){
        audioAdapter = RecordingAdapter(this, uriViewModel)
        val mLayoutManager = LinearLayoutManager(this)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL

        binding.recyclerView.apply {
            adapter = audioAdapter
            layoutManager = mLayoutManager
            audioAdapter?.setOnItemClickListener(object : RecordingAdapter.OnRecordingClickListener{
                override fun onItemClick(view: View, position: Int) {
                    val uriName = uriViewModel.uri
                    val file = File(uriName.toString())
                    if (isPlaying){
                        if (playIcon == view as ImageView){
                             stopPlaying()
                        }else{
                            stopPlaying()

                            playIcon = view
                            startPlaying(file)
                        }
                    }else{
                        playIcon = view as ImageView
                        startPlaying(file)
                    }
                }

            })
        }

    }

    private fun startPlaying(file : File){

        player = MediaPlayer().apply {

            try {
                setDataSource(file.absolutePath)
                prepare()
            }catch (e: IOException){
                Log.e("APP", "media playter prepare fail $e")
            }
            start()
        }
        playIcon?.setImageDrawable(resources.getDrawable(R.drawable.baseline_pause_circle_outline_24, null))
        isPlaying = true
//        binding.waveformView.clearWave()
//        timer.start()
        player?.setOnCompletionListener {
            stopPlaying()
        }

    }

    private fun stopPlaying(){
        playIcon?.setImageDrawable(resources.getDrawable(R.drawable.baseline_audio_file_24, null))
        isPlaying = false
        player?.stop()
//        timer.stop(false)
    }
}