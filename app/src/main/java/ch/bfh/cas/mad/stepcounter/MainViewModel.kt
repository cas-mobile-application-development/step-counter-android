package ch.bfh.cas.mad.stepcounter

import androidx.lifecycle.ViewModel
import ch.bfh.cas.mad.stepcounter.MainViewModel.SensorState.AVAILABLE
import ch.bfh.cas.mad.stepcounter.MainViewModel.SensorState.NOT_AVILABLE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    enum class SensorState {
        AVAILABLE, NOT_AVILABLE
    }

    private val _numSteps = MutableStateFlow(0)
    val numSteps: StateFlow<Int> = _numSteps

    private val _sensorState = MutableStateFlow<SensorState>(NOT_AVILABLE)
    val sensorState: StateFlow<SensorState> = _sensorState

    fun stepDetected() {
        _numSteps.value += 1
    }

    fun sensorNotAvailable() {
        _sensorState.value = NOT_AVILABLE
    }

    fun sensorAvailable() {
        _sensorState.value = AVAILABLE
    }
}