package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by s9iper1 on 11/4/16.
 */

public class DismissReceiver extends BroadcastReceiver {

    private InterstitialAd mInterstitialAd;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("DismissReceiver", "Dismiss Receiver");
        mInterstitialAd = new InterstitialAd(context);
        //context.getResources().getString(R.string.interstitial_ad_id)
        mInterstitialAd.setAdUnitId(context.getResources().getString(R.string.interstitial_ad_id));
        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }

            @Override
            public void onAdClosed() {

            }
        });

        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }
}
