package com.byteshaft.namaztime;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TextView;

public class UiUpdateHelpers {

    private Activity mActivity = null;

    public UiUpdateHelpers(Activity context) {
        mActivity = context;
    }

    public void setDate(String date) {
        TextView dateLabel = (TextView) mActivity.findViewById(R.id.dateLabel);
        setupLabels(dateLabel, date);
    }

    public void setNamazNames(String namaz) {
        TextView namazLabel = (TextView) mActivity.findViewById(R.id.namazLabel);
        setupLabels(namazLabel, namaz);
    }

    public void setNamazTimesLabel(String namazTimes) {
        TextView namazTimesLabel = (TextView) mActivity.findViewById(R.id.namazTimesLabel);
        setupLabels(namazTimesLabel, namazTimes);
    }

    private void setupLabels(TextView textView, String text) {
        int TEXT_SIZE = 20;
        textView.setTypeface(getTypeface());
        textView.setTextSize(TEXT_SIZE);
        textView.setTextColor(getCustomTextColorCode());
        textView.setText(text);
    }

    private Typeface getTypeface() {
        return Typeface.create("sans-serif", Typeface.BOLD);
    }

    private int getCustomTextColorCode() {
        return Color.parseColor("#FFFFFF");
    }
}
