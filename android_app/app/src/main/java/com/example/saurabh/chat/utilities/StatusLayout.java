package com.example.saurabh.chat.utilities;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.saurabh.chat.R;

public class StatusLayout {
    Activity activity;
    LinearLayout status;
    ImageView errorSign;
    ProgressBar spinner;
    TextView description;
    Resources res;

    public StatusLayout(Activity activity) {
        this.activity = activity;
        res = activity.getResources();

        errorSign = (ImageView) activity.findViewById(R.id.iv_error_sign);
        spinner = (ProgressBar) activity.findViewById(R.id.pb_spinner);
        description = (TextView) activity.findViewById(R.id.txt_description);
        status = (LinearLayout) activity.findViewById(R.id.layout_status);
    }

    public void setLoading() {
        status.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        errorSign.setVisibility(View.GONE);
        description.setText(res.getString(R.string.loading));
    }

    public void setError(String message) {
        status.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);
        errorSign.setVisibility(View.VISIBLE);
        description.setText(message);
    }

    public void hide() {
        status.setVisibility(View.GONE);
    }
}
