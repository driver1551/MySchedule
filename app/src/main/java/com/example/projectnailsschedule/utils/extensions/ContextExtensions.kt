package com.example.projectnailsschedule.utils.extensions

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

/** Extension-функция к Context для вывода диалогового окна */

fun Context.showAlertDialog(
    title: String?,
    message: String?,
    positiveText: String?,
    onPositiveClick: (() -> Unit)? = null,
    negativeText: String? = null,
    onNegativeClick: (() -> Unit)? = null,
    onDismiss: () -> Unit = {},
) {
    AlertDialog.Builder(this).apply {
        setTitle(title)
        setMessage(message)

        if (!positiveText.isNullOrEmpty()) { // если positiveText не передан - не создаем кнопку
            setPositiveButton(positiveText) { dialog, _ ->
                onPositiveClick?.invoke()
                dialog.dismiss()
            }
        }

        if (negativeText != null) { // Не создаем кнопку, если negativeText == null
            setNegativeButton(negativeText) { dialog, _ ->
                onNegativeClick?.invoke()
                dialog.dismiss()
            }
        }
        setOnDismissListener { onDismiss() }
        show()
    }
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}


