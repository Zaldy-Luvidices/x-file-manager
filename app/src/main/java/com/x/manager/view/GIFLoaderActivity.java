package com.x.manager.view;

import android.app.*;
import android.view.*;
import android.os.*;
import android.widget.*;

import com.x.manager.R;
import com.x.manager.utility.Tools;

public class GIFLoaderActivity extends Activity implements View.OnClickListener {
    private TextView txtItems, txtPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gif_loader);

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtItems = (TextView) findViewById(R.id.txtItems);
        txtItems.setTypeface(Tools.DefaultFont(this, false));
        txtPages = (TextView) findViewById(R.id.txtPages);
        txtPages.setTypeface(Tools.DefaultFont(this, false));
        ImageView btn;
        btn = (ImageView) findViewById(R.id.btnError);
        btn.setOnClickListener(this);
        btn = (ImageView) findViewById(R.id.btnWarning);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.txtTitle) {
            Exit(RESULT_CANCELED);
        }

    }

    @Override
    public void onBackPressed() {
        Exit(RESULT_CANCELED); // make sure animation will run
    }

    private void Exit(int resCode) {
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }
}
