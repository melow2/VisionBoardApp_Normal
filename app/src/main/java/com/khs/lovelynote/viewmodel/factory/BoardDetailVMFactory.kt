package com.khs.lovelynote.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khs.lovelynote.viewmodel.BoardDetailVM

class BoardDetailVMFactory(private val mApplication: Application, private val param1: Long) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BoardDetailVM(mApplication, param1) as T
    }
}