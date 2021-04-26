package com.example.task.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.task.data.Data
import com.example.task.data.mapRepo
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
    fun getAllDrivers():LiveData<ArrayList<Data>>{
       repo.getallDrivers()
        return  repo.allDriversLiveData
    }



}