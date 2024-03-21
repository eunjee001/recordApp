package com.kkyoungs.simpleRecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kkyoungs.simpleRecorder.data.RecordUri
import com.kkyoungs.simpleRecorder.data.RecordViewModel
import com.kkyoungs.simpleRecorder.data.RecordViewModelFactory
import com.kkyoungs.simpleRecorder.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity(), OnTimerTickListener {
    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200
    }

    private enum class State {
        RELEASE, RECORDING, PAUSE
    }

    private lateinit var timer: Timer

    private var state: State = State.RELEASE
    private lateinit var binding: ActivityMainBinding
    private var recorder: MediaRecorder? = null
    private var fileName: String = ""
    private var recordingStopped = false
    private var uriViewModel: RecordUri? = null // 오디오 파일 uri
    private var uriList = arrayListOf<RecordUri>()
    private var player: MediaPlayer? = null
    private val recordViewModel: RecordViewModel by viewModels {
        RecordViewModelFactory((application as RecordsApplication).repository)
    }
    /** 리사이클러뷰  */
    private val audioAdapter by lazy {
        RecordingAdapter(this)
    }
    private var isPlaying = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        titleBarSetting()
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.pauseButton.visibility = View.GONE

        timer = Timer(this)
        binding.recordButton.setOnClickListener {
            when (state) {
                State.RELEASE -> {
                    binding.pauseButton.visibility = View.GONE
                    recordPermission()
                }

                State.RECORDING -> {
                    onRecord(false)
                }

                State.PAUSE -> {

                }
            }
        }
        binding.pauseButton.setOnClickListener {
            pauseRecording()
        }
    }

    private fun titleBarSetting() {
        binding.backBtn.visibility = View.GONE
        binding.recordList.visibility = View.VISIBLE
        adapterSetting()

        binding.recordList.setOnClickListener {
            binding.doRecording.visibility = View.GONE
            binding.showRecordingList.visibility = View.VISIBLE
            binding.backBtn.visibility = View.VISIBLE
            binding.recordList.visibility = View.GONE
        }

        binding.backBtn.setOnClickListener {
            binding.doRecording.visibility = View.VISIBLE
            binding.showRecordingList.visibility = View.GONE
            binding.backBtn.visibility = View.GONE
            binding.recordList.visibility = View.VISIBLE
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when(keyCode){
            KeyEvent.KEYCODE_BACK ->{
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        if (recorder != null) {
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
    } else {
        stopRecording()
    }

    private fun startRecording() {
        binding.pauseButton.visibility = View.VISIBLE
        recordingStopped = false
        val recordPath = getExternalFilesDir("/")!!.absolutePath
        // 파일 이름 변수를 현재 날짜가 들어가도록 초기화. 그 이유는 중복된 이름으로 기존에 있던 파일이 덮어 쓰여지는 것을 방지하고자 함.
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        fileName = recordPath + "/" + "record" + timeStamp + "_" + "audio.mp3"
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
                this, R.drawable.new2
            )
        )

    }

    private fun pauseRecording() {
        if (!recordingStopped) {
            recorder?.pause()
            recordingStopped = true
            timer.stop(true)
            binding.pauseButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this, R.drawable.baseline_play_arrow_24
                )
            )
        } else {
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

    private fun stopRecording() {
        binding.pauseButton.visibility = View.GONE

        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        state = State.RELEASE
        timer.stop(false)
        Toast.makeText(this, "녹음을 저장 했습니다..", Toast.LENGTH_SHORT).show()
        binding.timerTextView.text = "00:00:00"
        binding.waveformView.clearWave()

        binding.recordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record))
        uriViewModel = RecordUri(fileName)
            recordViewModel.insert(uriViewModel!!)
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


    private fun adapterSetting() {
        binding.recordRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = audioAdapter
        }
        recordViewModel.allRecords.observe(this) { record ->
            record.let { audioAdapter.submitList(it) }
        }

        audioAdapter.setOnItemClickListener(object : RecordingAdapter.OnRecordingClickListener {
            override fun onItemClick(view: View, position: Int) {
                val uriName = recordViewModel.allRecords.value?.get(position)?.uri
                val file = File(uriName.toString())
                if (isPlaying) {
                    stopRecording()
                } else {
                    startPlaying(file)
                }
            }

        })

    }

    private fun startPlaying(file: File) {
        isPlaying = true
        Toast.makeText(this, "재생", Toast.LENGTH_SHORT).show()

        player = MediaPlayer().apply {
            println("file " +file.absolutePath)

            try {
                setDataSource(file.absolutePath)
                prepare()
            } catch (e: IOException) {
                Log.e("APP", "media playter prepare fail $e")
            }

        }
        playClicked()

        player?.setOnCompletionListener {
            println("<>>> 이거 타니 ?")
            stopPlaying()
            isPlaying = false

        }

    }
    private fun playClicked() {
        binding.playListSeekBar.max = player!!.duration // 음악의 총 길이를 시크바 최대값에 적용
        binding.playListSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) // 사용자가 시크바를 움직이면
                    player!!.seekTo(progress) // 재생위치를 바꿔준다(움직인 곳에서의 음악재생)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        player!!.start()

        Thread {
            while (player!!.isPlaying) {  // 음악이 실행중일때 계속 돌아가게 함
                try {
                    Thread.sleep(100) // 1초마다 시크바 움직이게 함
                    binding.playListSeekBar.progress = player!!.currentPosition

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()

    }

    private fun stopPlaying() {
        isPlaying = false
        player?.stop()

        if (!player?.isPlaying!!){
            binding.playListSeekBar.progress = 0
        }
    }

    override fun onTick(duration: Long) {
        val millisecond = duration % 1000
        val second = (duration / 1000) % 60
        val minute = (duration / 1000 / 60)

        binding.timerTextView.text =
            String.format("%02d:%02d:%02d", minute, second, millisecond / 10)

        if (state == State.RECORDING) {
            binding.waveformView.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f)
        }
    }
}