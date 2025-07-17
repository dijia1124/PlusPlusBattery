package com.dijia1124.plusplusbattery.ui.components

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.dijia1124.plusplusbattery.R

fun Context.showRootDeniedToast(
    @StringRes msgRes: Int = R.string.root_access_denied
) {
    Toast.makeText(this, getString(msgRes), Toast.LENGTH_SHORT).show()
}
