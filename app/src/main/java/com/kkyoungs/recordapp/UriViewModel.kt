package com.kkyoungs.recordapp

import android.net.Uri
import androidx.lifecycle.ViewModel

data class UriViewModel(
    val uri : Uri
){
    constructor() : this(Uri.parse(""))
}
