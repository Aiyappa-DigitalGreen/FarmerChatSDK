package com.farmerchat.sdk

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.farmerchat.sdk.R
import com.farmerchat.sdk.activity.FarmerChatActivity

/**
 * Home-screen widget that opens FarmerChat with a single tap.
 *
 * Register in your AndroidManifest.xml:
 *
 *   <receiver
 *       android:name="com.farmerchat.sdk.FarmerChatWidgetProvider"
 *       android:exported="true">
 *       <intent-filter>
 *           <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
 *       </intent-filter>
 *       <meta-data
 *           android:name="android.appwidget.provider"
 *           android:resource="@xml/farmerchat_widget_info" />
 *   </receiver>
 */
class FarmerChatWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_farmerchat)

        // Apply config label if SDK is initialized
        val config = runCatching { FarmerChatSdk.config }.getOrNull()
        val title = config?.chatTitle ?: "FarmChat AI"
        views.setTextViewText(R.id.widget_title, title)

        // Tap on entire widget → open chat
        val intent = Intent(context, FarmerChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            widgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }
}
