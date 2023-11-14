package ch.bfh.cas.mad.stepcounter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var textViewData: TextView
    private lateinit var textViewSensorState: TextView
    private lateinit var actiivtyRegcognitionPermissionRequest: ActivityResultLauncher<String>
    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private lateinit var eventListener: SensorEventListener
    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textViewData = findViewById(R.id.textview_data)
        textViewSensorState = findViewById(R.id.textview_sensor_state)
        actiivtyRegcognitionPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                when {
                    isGranted -> startStepCounter()
                    else -> {
                        requestActitivityRecognitionPermission()
                    }
                }
            }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (stepCounter == null) {
            mainViewModel.sensorNotAvailable()
        } else {
            mainViewModel.sensorAvailable()
        }
        eventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
               mainViewModel.stepDetected()
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            mainViewModel.numSteps.collectLatest {count ->
                textViewData.text = count.toString()
            }
        }
        lifecycleScope.launch {
            mainViewModel.sensorState.collectLatest { state ->
                when(state) {
                    MainViewModel.SensorState.AVAILABLE -> {
                        textViewSensorState.text = getString(R.string.sensor_available)
                        textViewSensorState.setBackgroundColor(getColor(R.color.ok))
                    }
                    MainViewModel.SensorState.NOT_AVILABLE -> {
                        textViewSensorState.text = getString(R.string.sensor_not_available)
                        textViewSensorState.setBackgroundColor(getColor(R.color.error))
                    }
                }
            }
        }
        startStepCounter()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(eventListener)
    }

    private fun startStepCounter() {
        stepCounter?.let {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestActitivityRecognitionPermission()
                return
            }

            sensorManager.registerListener(eventListener, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun requestActitivityRecognitionPermission() {
        when {
            shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) ->
                showActivityRecognitionPermissionRationale {
                    actiivtyRegcognitionPermissionRequest.launch(
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )
                }

            else -> actiivtyRegcognitionPermissionRequest.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    private fun showActivityRecognitionPermissionRationale(onOk: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.activity_recognition_rationale_title))
            .setMessage(getString(R.string.activity_recognition_rationale_message))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> onOk() }
            .show()
    }
}