package com.example.saurabh.chat.layouts;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.saurabh.chat.R;

public class StatusLayout extends LinearLayout {
    ImageView errorSign;
    ProgressBar spinner;
    TextView description;

    Resources res;

    LayoutInflater mInflater;

    public StatusLayout(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        res = context.getResources();
        init();
    }

    public StatusLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        res = context.getResources();
        init();
    }

    public StatusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        res = context.getResources();
        init();
    }

    public void init() {
        mInflater.inflate(R.layout.layout_status, this, true);

        errorSign = (ImageView) findViewById(R.id.iv_error_sign);
        spinner = (ProgressBar) findViewById(R.id.pb_spinner);
        description = (TextView) findViewById(R.id.txt_description);
    }

    public void setLoading() {
        Log.i("StatusLayout", "setLoading()");
        this.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        errorSign.setVisibility(View.GONE);
        description.setText(res.getString(R.string.loading));
    }

    public void setError(String message) {
        this.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);
        errorSign.setVisibility(View.VISIBLE);
        description.setText(message);
    }

    public void hide() {
        this.setVisibility(View.GONE);
    }
}
