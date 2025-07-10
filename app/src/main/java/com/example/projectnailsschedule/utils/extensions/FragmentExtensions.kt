package com.example.projectnailsschedule.utils.extensions

import android.widget.Toast
import androidx.fragment.app.Fragment

/** Toast-extension */

fun Fragment.showToast(message: String, lengthLong: Boolean = false) {
    Toast.makeText(
        requireContext(),
        message,
        if (lengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    ).show()
}
