package com.example.saurabh.chat.layouts;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.saurabh.chat.R;

public class StatusLayout extends LinearLayout {
    ImageView errorSign;
    ProgressBar spinner;
    TextView description;
    Button btn;

    boolean showBtn = false;

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
        btn = (Button) findViewById(R.id.btn_respond_to_status);
    }

    public void setLoading() {
        this.setVisibility(VISIBLE);
        spinner.setVisibility(VISIBLE);
        errorSign.setVisibility(GONE);
        description.setText(res.getString(R.string.loading));
        btn.setVisibility(GONE);
    }

    public void setError(String message) {
        this.setVisibility(VISIBLE);
        spinner.setVisibility(GONE);
        errorSign.setVisibility(VISIBLE);
        description.setText(message);
        if(showBtn) {
            btn.setVisibility(VISIBLE);
        }
    }

    public void setActionButton(String value, OnClickListener onClickListener) {
        showBtn = value != null && onClickListener != null;
        btn.setText(value);
        btn.setOnClickListener(onClickListener);
    }

    public void hide() {
        this.setVisibility(View.GONE);
    }
}
