package com.x.manager.dialog;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.x.manager.R;
import com.x.manager.utility.Tools;

public class ConfirmYesNoDialog extends Activity implements View.OnClickListener {
    public static final String PARAM_TITLE = "param_title";
    public static final String PARAM_MESSAGE = "param_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm_yesno);
        TextView txt;
        txt = (TextView) findViewById(R.id.txtTitle);
        txt.setTypeface(Tools.DefaultFont(this, true));
        txt.setText(getIntent().getStringExtra(PARAM_TITLE));
        txt.setOnClickListener(this);
        txt = (TextView) findViewById(R.id.txtMessage);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setText(getIntent().getStringExtra(PARAM_MESSAGE));

        ImageView btn;
        btn = (ImageView) findViewById(R.id.btnYes);
        btn.setOnClickListener(this);
        btn = (ImageView) findViewById(R.id.btnNo);
        btn.setOnClickListener(this);

        Tools.SetDialogPosition(this, Gravity.BOTTOM);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnYes) {
            Exit(RESULT_OK);
        } else if (v.getId() == R.id.txtTitle ||
                v.getId() == R.id.btnNo) {
            Exit(RESULT_CANCELED);
        }
    }

    @Override
    public void onBackPressed() {
        Exit(RESULT_CANCELED); // make sure animation will run
    }

    private void Exit(int resCode) {
        setResult(resCode);
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }
}
