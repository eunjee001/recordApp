package com.kkyoungs.recordapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kkyoungs.recordapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object{
        private const val REQUEST_RECORD_AUDIO_CODE = 200
    }
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recordButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 실제로 녹음을 시작하면 됨
                }

                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) -> {
                    showPermissionRationalDialog()
                }

                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        REQUEST_RECORD_AUDIO_CODE)
                }
            }
        }
    }
    //원리
    private fun showPermissionRationalDialog(){
        AlertDialog.Builder(this)
            .setMessage("녹음 권한을 켜주세요")
            .setPositiveButton("권한 허용하기"){_,_->
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE)
            }.setNegativeButton("취소"){dialogInterface, _ -> dialogInterface.cancel() }
            .show()
    }

    private fun showPermissionSettingDialog(){
        AlertDialog.Builder(this)
            .setMessage("녹음 권한을 켜주셔야지 앱을 정상적으로 사용할 수 있습니다. 앱 설정 화면으로 진입해서 권한 켜주세요")
            .setPositiveButton("권한 변경하기"){_,_->
                navigateToAppSetting()
            }.setNegativeButton("취소"){dialogInterface, _ -> dialogInterface.cancel() }
            .show()
    }

    private fun navigateToAppSetting(){
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
        if (audioRecordPermissionGranted){
            // 녹음 시작
        }else {
            // 권한 팝업 체크
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)){
                showPermissionRationalDialog()
            }else{
                showPermissionSettingDialog()
            }
        }
    }
}