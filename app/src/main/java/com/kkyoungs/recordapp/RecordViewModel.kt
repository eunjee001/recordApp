package com.kkyoungs.recordapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class RecordViewModel(private val repository: RecordRepository):ViewModel() {
    val allRecords : LiveData<List<RecordUri>> = repository.allRecords.asLiveData()

    fun insert(recordUri: RecordUri) = viewModelScope.launch {
        repository.insert(recordUri)
    }
}

class RecordViewModelFactory(private val repository: RecordRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return RecordViewModel(repository) as T
        }
        throw IllegalArgumentException("UnKnown ViewModel Class")
    }
}