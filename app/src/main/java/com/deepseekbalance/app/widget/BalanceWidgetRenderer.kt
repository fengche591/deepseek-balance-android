package com.deepseekbalance.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.deepseekbalance.app.MainActivity
import com.deepseekbalance.app.R
import com.deepseekbalance.app.data.SharedPreferencesBalanceCache
import java.text.DateFormat
import java.util.Date

object BalanceWidgetRenderer {
    fun updateAll(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, BalanceWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        render(context, appWidgetManager, appWidgetIds)
    }

    fun render(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val state = SharedPreferencesBalanceCache(context).read()
        val snapshot = state.snapshot

        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.balance_widget)
            views.setTextViewText(
                R.id.widget_balance,
                snapshot?.let { "¥${it.totalBalance}" } ?: "¥ --",
            )
            views.setTextViewText(
                R.id.widget_status,
                statusText(state.errorMessage, snapshot?.fetchedAtEpochMillis, snapshot?.isAvailable),
            )
            views.setOnClickPendingIntent(R.id.widget_root, refreshPendingIntent(context))
            views.setOnClickPendingIntent(R.id.widget_open, openAppPendingIntent(context))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun statusText(
        errorMessage: String?,
        fetchedAtEpochMillis: Long?,
        isAvailable: Boolean?,
    ): String {
        if (errorMessage != null) {
            return if (fetchedAtEpochMillis == null) {
                errorMessage
            } else {
                "${fetchedAtEpochMillis.toDisplayTime()} · 更新失败"
            }
        }
        if (fetchedAtEpochMillis == null) {
            return "点击刷新"
        }
        if (isAvailable == false) {
            return "${fetchedAtEpochMillis.toDisplayTime()} · 账户不可用"
        }
        return fetchedAtEpochMillis.toDisplayTime()
    }

    private fun refreshPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, BalanceWidgetProvider::class.java).apply {
            action = BalanceWidgetProvider.ACTION_REFRESH
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun openAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun Long.toDisplayTime(): String {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(this))
    }
}
