package com.satish.vpnguide.service

import android.app.AlertDialog
import android.content.DialogInterface
import com.satish.vpnguide.R
import com.satish.vpnguide.utils.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


object DialogService {

    private val context by lazy { ContextService }

    private var displayedDialog: AlertDialog? = null

    suspend fun showAlert(
        message: String, header: String? = null,
        okText: String? = null, okAction: () -> Any? = {}
    ): Flow<Boolean> {
        return callbackFlow {
            if (displayedDialog != null) {
                Logger.w("Dialog", "Ignoring new dialog request, one is already being displayed")
                trySendBlocking(false)
                channel.close()
            } else {
                val ctx = context.requireContext()
                val builder = AlertDialog.Builder(ctx)
                builder.setTitle(header ?: ctx.getString(R.string.alert_error_header))
                builder.setMessage(message)

                builder.setPositiveButton(okText) { dialog, _ ->
                    dismiss()
                    okAction()
                }
                builder.setNeutralButton(ctx.getString(R.string.universal_action_close)) { dialog, _ ->
                    dismiss()
                }

                builder.setOnDismissListener {
                    dismiss(it)
                    trySendBlocking(true)
                    channel.close()
                }

                displayedDialog = builder.showButNotCrash()

                awaitClose { dismiss() }
            }
        }
    }

    fun dismiss(dialog: DialogInterface? = displayedDialog) {
        displayedDialog?.let {
            // Android calls dismiss listener with a delay. The usual.
            if (it == dialog) {
                it.dismiss()
                displayedDialog = null
            }
        }
    }

    private fun AlertDialog.Builder.showButNotCrash(): AlertDialog? {
        return try { this.show() } catch (ex: Exception) {
            Logger.e("Dialog", "Could not show dialog, ignoring: ${ex.message}")
            null
        }
    }

}