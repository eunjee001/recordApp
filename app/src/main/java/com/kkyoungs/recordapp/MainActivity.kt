package com.kkyoungs.recordapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.kkyoungs.recordapp.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

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
    private var fileName: String = ""
    private var recordingStopped = false
    private var uriViewModel: UriViewModel? = null // 오디오 파일 uri
    private var uriList = arrayListOf<UriViewModel>()
    private var player : MediaPlayer ?= null
    /** 리사이클러뷰  */
    private val audioAdapter by lazy {
        RecordingAdapter()
    }
    private var isPlaying = false
    var playIcon : ImageView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pauseButton.visibility = View.GONE
//        val sdCard = Environment.getExternalStorageDirectory()
//        val file = File(sdCard, "audioTest.mp4")
//        fileName = file.absolutePath
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
        binding.list.setOnClickListener {
        if (uriViewModel !=null) {
            adapterSetting()
            binding.doRecording.visibility = View.GONE
            binding.showRecordingList.visibility = View.VISIBLE
        }else  {
            Toast.makeText(this, "저장된 녹음 파일이 없습니다.", Toast.LENGTH_SHORT).show()
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
            pauseRecording()

        }
    }

    override fun onPause() {
        super.onPause()
        if (recorder !=null){
            recorder?.release()
            recorder = null
        }
    }


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
        println(">>>> start?")

    } else {
        println(">>>> stop?")
        stopRecording()
    }

    private fun startRecording() {
        binding.pauseButton.visibility = View.VISIBLE
        recordingStopped = true
        val recordPath = getExternalFilesDir("/")!!.absolutePath
        // 파일 이름 변수를 현재 날짜가 들어가도록 초기화. 그 이유는 중복된 이름으로 기존에 있던 파일이 덮어 쓰여지는 것을 방지하고자 함.
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        fileName = recordPath + "/" + "RecordExample_" + timeStamp + "_" + "audio.mp4"
        state = State.RECORDING
        recorder = MediaRecorder().apply {

            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()

            } catch (e: IOException) {
                Log.e("APP", "prepare() failed $e")
            }
        }

        binding.waveformView.clearData()
        timer.start()

        binding.recordButton.setImageDrawable(
            ContextCompat.getDrawable(
                this, R.drawable.save
            )
        )
//
//        binding.recordButton.setOnClickListener {
//            onRecord(false)
//        }
//


    }

    private fun pauseRecording() {
        if (!recordingStopped){
            recorder ?.pause()
            recordingStopped = true
            timer.stop(true)
            binding.pauseButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this, R.drawable.baseline_play_arrow_24
                )
            )
        }else{
            recorder?.resume()
            recordingStopped = false
            timer.start()
            binding.pauseButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this, R.drawable.pause
                )
            )
        }

    }

    private fun stopRecording(){
        recorder?.apply {
            stop()
            release()
        }
        recorder = null


        state = State.RELEASE
        timer.stop(false)
        Toast.makeText(this, "녹음을 저장 했습니다.." , Toast.LENGTH_SHORT).show()
        binding.timerTextView.text = "00:00:00"
        binding.waveformView.clearWave()

        binding.recordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record))
        uriViewModel = UriViewModel(Uri.parse(fileName))
        uriList.add(uriViewModel!!)

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
    private fun adapterSetting(){
        binding.recordRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false )
            adapter = audioAdapter
        }
        println(">>> arrayListOf<UriViewModel>()" +uriList)
        audioAdapter.submitList(uriList)
        binding.recordDoing.setOnClickListener {
            binding.doRecording.visibility = View.VISIBLE
            binding.showRecordingList.visibility = View.GONE
        }
        audioAdapter.setOnItemClickListener(object : RecordingAdapter.OnRecordingClickListener{
            override fun onItemClick(view: View, position: Int) {
                val uriName = uriViewModel?.uri
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
    override fun onTick(duration: Long) {
        val millisecond = duration % 1000
        val second = (duration / 1000) % 60
        val minute = (duration / 1000 / 60)

        binding.timerTextView.text = String.format("%02d:%02d:%02d", minute, second, millisecond / 10).toString()

//        if (state == State.PLAYING){
//            binding.waveformView.replayAmplitude()
//        }else
            if (state == State.RECORDING){
            binding.waveformView.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f)
        }
    }
}