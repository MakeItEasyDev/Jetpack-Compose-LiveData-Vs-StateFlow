package com.jetpack.livedatavsstateflow

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Suppress("UNCHECKED_CAST")
class SafeMutableLiveData<T : Any>(
    savedStateHandle: SavedStateHandle,
    key: String,
    defaultValue: T
) : MutableLiveData<T>(savedStateHandle.getLiveData(key, defaultValue).value) {

    private val mutableLiveData: MutableLiveData<T> =
        savedStateHandle.getLiveData(key, defaultValue)

    override fun getValue(): T = super.getValue() as T
    override fun setValue(value: T) {
        super.setValue(value)
        mutableLiveData.value = value
    }

    override fun postValue(value: T) {
        super.postValue(value)
        mutableLiveData.postValue(value)
    }
}

class SaveableMutableSaveStateFlow<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    defaultValue: T
) {
    private val _state: MutableStateFlow<T> =
        MutableStateFlow(savedStateHandle.get<T>(key) ?: defaultValue)

    var value: T
        get() = _state.value
        set(value) {
            _state.value = value
            savedStateHandle.set(key, value)
        }

    fun asStateFlow(): StateFlow<T> = _state
}