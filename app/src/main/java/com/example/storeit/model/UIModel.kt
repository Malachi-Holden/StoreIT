package com.example.storeit.model

import androidx.lifecycle.ViewModel

class UIModel: ViewModel() {
    var currentTreeId: String? = ROOT_ID
    var currentItemId: String? = null
}