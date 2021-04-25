package com.example.task

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class mapRepo @Inject constructor(private val db:FirebaseFirestore) {
  private val _allsourceLiveData=MutableLiveData<ArrayList<Data>>()
    val allsourceLiveData:LiveData<ArrayList<Data>> get() =_allsourceLiveData


    fun getallSources(){
        db.collection("Source").addSnapshotListener { snapshot, error ->

            if (error!=null){
                Log.d("error",error.message.toString())
                return@addSnapshotListener
            }
            if (snapshot!=null){
                val allSources=ArrayList<Data>()
                val documents=snapshot.documents
                documents.forEach {document->
                    val singlesource=document.toObject(Data::class.java)
                    allSources.add(singlesource!!)
                }

                _allsourceLiveData.value=allSources

            }

        }
    }


    fun setNewDestinationItem(data: Data){
        db.collection("destination").document().
        set(data).addOnSuccessListener {
            Log.d("firebase","Data saved successfully")
        }.addOnFailureListener {
            Log.d("firebase","Data failed due to  ${it.message.toString()}")
        }

    }




}