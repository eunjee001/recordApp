package com.kkyoungs.recordapp

import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class UriViewModel(
    val uri : Uri
) :Parcelable
