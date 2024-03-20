package com.kkyoungs.simpleRecorder.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class RecordViewModel(private val repository: RecordRepository):ViewModel() {

    // LiveData :LiveData는 Activity, Fragment 등의 LifeCycle을 인식하여 LifeCycle 내에서만 동작하는 요소로
    // LifeCycle이 종료되면 같이 삭제됩니다.
    // 그러므로 메모리 누출이 없고 수명주기에 따른 데이터 관리를 개발자가 하지 않아도 된다는 점 등 많은 이점을 가지고 있습니다.
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