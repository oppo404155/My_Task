package com.example.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repo: mapRepo
) : ViewModel() {

    fun getCurrentSources(): LiveData<ArrayList<Data>> {
        repo.getallSources()

        return repo.allsourceLiveData

    }
    fun setNewDestItem(data: Data){
        repo.setNewDestinationItem(data)
    }

}