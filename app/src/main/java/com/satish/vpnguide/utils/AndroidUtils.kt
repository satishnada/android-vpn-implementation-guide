
package com.satish.vpnguide.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

fun Context.getPendingIntentForActivity(intent: Intent, flags: Int): PendingIntent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.getActivity(this, 0, intent, flags or PendingIntent.FLAG_IMMUTABLE)
    } else {
        PendingIntent.getActivity(this, 0, intent, flags)
    }
}

@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

fun now() = System.currentTimeMillis()