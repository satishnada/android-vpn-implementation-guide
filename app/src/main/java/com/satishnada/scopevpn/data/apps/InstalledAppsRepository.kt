package com.satishnada.scopevpn.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.satishnada.scopevpn.domain.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsRepository(
    private val context: Context,
) {
    suspend fun loadLaunchableApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedActivities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
        }

        resolvedActivities
            .mapNotNull { resolveInfo ->
                val applicationInfo = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null
                if (applicationInfo.packageName == context.packageName) return@mapNotNull null

                InstalledApp(
                    label = resolveInfo.loadLabel(packageManager).toString().ifBlank {
                        applicationInfo.packageName.substringAfterLast('.')
                    },
                    packageName = applicationInfo.packageName,
                    isSystemApp = applicationInfo.isSystemOrUpdatedSystemApp(),
                )
            }
            .distinctBy(InstalledApp::packageName)
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }
}

private fun ApplicationInfo.isSystemOrUpdatedSystemApp(): Boolean {
    val mask = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
    return flags and mask != 0
}
