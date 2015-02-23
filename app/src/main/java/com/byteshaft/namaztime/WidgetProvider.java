package com.byteshaft.namaztime;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;


public class WidgetProvider extends AppWidgetProvider {

    private static Context mContext;

    public static void setupWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        Intent receiver = new Intent(mContext, WidgetReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, 0, receiver, 0);
        RemoteViews mRemoteViews = new RemoteViews(mContext.getPackageName(),
                R.layout.widget);
        mRemoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        appWidgetManager.updateAppWidget(new ComponentName(mContext, WidgetProvider.class),
                mRemoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        mContext = context;
        setupWidget();
    }
}
