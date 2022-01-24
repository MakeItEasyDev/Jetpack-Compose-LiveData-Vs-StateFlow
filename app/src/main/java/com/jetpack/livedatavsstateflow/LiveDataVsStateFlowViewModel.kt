package com.jetpack.livedatavsstateflow

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class LiveDataVsStateFlowViewModel(
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val repository = LiveDataStateFlowRepository(
        SafeMutableLiveData(
            savedStateHandle = savedStateHandle,
            "TriggerLiveDataKey",
            "Initial"
        ),
        SaveableMutableSaveStateFlow(
            savedStateHandle,
            "TriggerStateFlowKey",
            "Initial"
        )
    )

    init {
        if (!savedStateHandle.contains("TriggerLiveDataKey")) triggerLive()
        if (!savedStateHandle.contains("TriggerStateFlowKey")) triggerState()
    }

    val liveData: LiveData<String> = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        suspend fun loading(): String {
            emit("Nothing")
            return repository.loadDataLive()
        }

        val data = savedStateHandle.get("LoadLiveDataKey") ?: loading()
        savedStateHandle.set("LoadLiveDataKey", data)
        emit(data)
    }

    val stateFlow = flow {
        val data = savedStateHandle.get("LoadStateFlowKey") ?: repository.loadDataState()
        savedStateHandle.set("LoadStateFlowKey", data)
        emit(data)
    }.stateIn(
        scope = viewModelScope + Dispatchers.IO,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = "Nothing"
    )

    val liveDataTrigger: LiveData<String> = repository
        .triggerLive
        .distinctUntilChanged()
        .switchMap {
            liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
                emit(it)
            }
        }

    val stateFlowTrigger = repository
        .triggerState.asStateFlow()
        .mapLatest {
            it
        }.stateIn(
            scope = viewModelScope + Dispatchers.IO,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = "Nothing"
        )

    fun triggerLive() {
        viewModelScope.launch(Dispatchers.Default) {
            repository.getLiveData()
        }
    }

    fun triggerState() {
        viewModelScope.launch(Dispatchers.Default) {
            repository.getStateData()
        }
    }
}

class LiveDataStateFlowRepository(
    liveData: MutableLiveData<String>,
    stateFLow: SaveableMutableSaveStateFlow<String>
) {
    val triggerLive = liveData
    val triggerState = stateFLow

    private fun generateRandom() = (1..1000).random().toString().padEnd(4, '0')

    suspend fun loadDataLive(): String {
        delay(2000)
        return generateRandom()
    }

    suspend fun loadDataState(): String {
        delay(2000)
        return generateRandom()
    }

    suspend fun getLiveData() {
        delay(2000)
        val value = generateRandom()
        triggerLive.postValue(value)
    }

    suspend fun getStateData() {
        delay(2000)
        val value = generateRandom()
        triggerState.value = value
    }
}

























