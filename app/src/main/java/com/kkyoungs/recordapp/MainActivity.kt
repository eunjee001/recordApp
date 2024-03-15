package com.kkyoungs.recordapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kkyoungs.recordapp.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity(), OnTimerTickListener {
    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200
    }
    // 릴리즈 -> 녹음중 -> 릴리즈
    // 릴리즈 -> 재생 -> 릴리즈

    private enum class State {
        RELEASE, RECORDING, PAUSE
    }

    private lateinit var timer : Timer

    private var state: State = State.RELEASE
    private lateinit var binding: ActivityMainBinding
    private var recorder: MediaRecorder? = null
    private var player : MediaPlayer ?= null
    private var fileName: String = ""
    private var recordingStopped = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pauseButton.visibility = View.GONE

        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
        timer = Timer(this)
        binding.recordButton.setOnClickListener {
            when (state) {
                State.RELEASE -> {
                    recordPermission()
                }

                State.RECORDING -> {
                    onRecord(false)

                }

                State.PAUSE ->{
                }

            }
        }
//        binding.playButton.setOnClickListener {
//            when (state) {
//                State.RELEASE -> {
//                    onPlay(true)
//                }
//                else ->{
//                    //nothing
//                }
//
//            }
//        }

        binding.pauseButton.setOnClickListener {
            when (state) {
                State.RELEASE -> {
                    recordPermission()
                }

                State.RECORDING -> {
                }

                State.PAUSE->{
                    pauseRecording()

                    Toast.makeText(this, "녹음 정지", Toast.LENGTH_SHORT).show()

                }


            }
        }
    }

    private fun onPlay(start : Boolean) = if (start) startPlaying() else stopPlaying()

    private fun recordPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 실제로 녹음을 시작하면 됨
                onRecord(true)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                showPermissionRationalDialog()
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }
        }

    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun startRecording() {
        binding.pauseButton.visibility = View.VISIBLE
        recordingStopped = true

        state = State.RECORDING
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("APP", "prepare() failed $e")
            }
            start()
        }

        binding.waveformView.clearData()
        timer.start()

        binding.recordButton.setImageDrawable(
            ContextCompat.getDrawable(
                this, R.drawable.save
            )
        )



    }

    private fun pauseRecording() {
        state = State.PAUSE

        if (!recordingStopped){
            recorder = MediaRecorder().apply {
                pause()
            }
            recordingStopped = true
        }else{
            recorder?.resume()
            recordingStopped = false

        }
        timer.stop()

        binding.recordButton.setImageDrawable(
            ContextCompat.getDrawable(
                this, R.drawable.record
            )
        )



    }

    private fun stopRecording(){
        recorder?.apply {
            stop()
            release()
        }

        timer.stop()

        state = State.RELEASE

        binding.recordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record))

    }

    private fun startPlaying(){

        player = MediaPlayer().apply {

            try {
                setDataSource(fileName)
                prepare()
            }catch (e:IOException){
                Log.e("APP", "media playter prepare fail $e")
            }
            start()
        }
        binding.waveformView.clearWave()
        timer.start()
        player?.setOnCompletionListener {
            stopPlaying()
        }

    }

    private fun stopPlaying(){
        state = State.RELEASE
        player?.release()
        player = null
        timer.stop()
        binding.recordButton.isEnabled = true
        binding.recordButton.alpha = 1.0f
    }

    //원리
    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this)
            .setMessage("녹음 권한을 켜주세요")
            .setPositiveButton("권한 허용하기") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }.setNegativeButton("취소") { dialogInterface, _ -> dialogInterface.cancel() }
            .show()
    }


    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this)
            .setMessage("녹음 권한을 켜주셔야지 앱을 정상적으로 사용할 수 있습니다. 앱 설정 화면으로 진입해서 권한 켜주세요")
            .setPositiveButton("권한 변경하기") { _, _ ->
                navigateToAppSetting()
            }.setNegativeButton("취소") { dialogInterface, _ -> dialogInterface.cancel() }
            .show()
    }

    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_CODE
                && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        if (audioRecordPermissionGranted) {
            // 녹음 시작
            onRecord(true)
        } else {
            // 권한 팝업 체크
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                showPermissionRationalDialog()
            } else {
                showPermissionSettingDialog()
            }
        }
    }

    override fun onTick(duration: Long) {
        val millisecond = duration % 1000
        val second = (duration / 1000) % 60
        val minute = (duration / 1000 / 60)

        binding.timerTextView.text = String.format("%02d:%02d:%02d", minute, second, millisecond / 10)

//        if (state == State.PLAYING){
//            binding.waveformView.replayAmplitude()
//        }else
            if (state == State.RECORDING){
            binding.waveformView.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f)
        }
    }
}