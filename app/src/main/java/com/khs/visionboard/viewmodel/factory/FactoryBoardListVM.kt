package com.khs.visionboard.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khs.visionboard.viewmodel.BoardListVM

class FactoryBoardListVM(private val mApplication: Application, private val param1: Int) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BoardListVM(mApplication, param1) as T
    }
}