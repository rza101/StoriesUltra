package com.rhezarijaya.storiesultra.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.rhezarijaya.storiesultra.R

object Utils {
    fun getPlaceholderImage(context: Context): Bitmap? {
        return AppCompatResources.getDrawable(context, R.drawable.ic_baseline_broken_image_24)
            ?.toBitmap(256, 256)
    }

    // nama fungsi harus checkPermission agar tdk muncul warning permission check
    fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}