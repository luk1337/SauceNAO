package com.luk.saucenao.ext

import android.content.SharedPreferences

var SharedPreferences.apiKey
    get() = getString("api_key", null) ?: ""
    set(value) = edit().putString("api_key", value).apply()

var SharedPreferences.usePhotoPicker
    get() = if (contains("use_photo_picker")) {
        getBoolean("use_photo_picker", false)
    } else {
        null
    }
    set(value) = edit().putBoolean("use_photo_picker", value!!).apply()
