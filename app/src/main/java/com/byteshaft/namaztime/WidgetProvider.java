package com.byteshaft.namaztime;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;


public class WidgetProvider extends AppWidgetProvider {

    public static void setupWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Intent receiver = new Intent(context, WidgetReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, receiver, 0);
        RemoteViews mRemoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget);
        mRemoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        appWidgetManager.updateAppWidget(new ComponentName(context, WidgetProvider.class),
                mRemoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        setupWidget(context);
    }
}
