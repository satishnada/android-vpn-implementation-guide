package com.satish.vpnguide.service

import android.app.Activity
import android.app.Application
import android.content.Context
import java.lang.ref.WeakReference

object ContextService {

    private var app: Application? = null
    private var context: Context? = null
    private var activityContext = WeakReference<Activity?>(null)

    fun setApp(app: Application) {
        /**
         * I assume it's ok to keep a strong reference to the app context (and not activity context),
         * since its lifespan is same as app's.
         */
        ContextService.app = app
        context = app.applicationContext
    }

    // Used only for BackupAgent
    fun setAppContext(appContext: Context) {
        context = appContext
    }

    fun setActivityContext(context: Activity) {
        activityContext = WeakReference(context)
    }

    fun unsetActivityContext() {
        activityContext = WeakReference(null)
    }

    fun requireContext(): Context {
        return activityContext.get() ?: context ?: throw Exception("No context set in ContextService")
    }

    fun requireActivity(): Activity {
        return activityContext.get() ?: throw Exception("No activity context set in ContextService")
    }

    fun requireAppContext(): Context {
        return context ?: throw Exception("No context set in ContextService")
    }

    fun hasActivityContext(): Boolean {
        return activityContext.get() != null
    }

    fun requireApp(): Application {
        return app ?: throw Exception("No app set in ContextService")
    }

}