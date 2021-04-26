package com.example.task.data

data class Data  (
    val name:String,
    val latitude:Double,
    val longitude:Double
        ){
    constructor():this("",0.0,0.0)
}


