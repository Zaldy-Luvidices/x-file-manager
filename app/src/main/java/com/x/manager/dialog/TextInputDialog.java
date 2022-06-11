package com.x.manager.dialog;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.*;

import com.x.manager.R;
import com.x.manager.utility.Tools;

public class TextInputDialog extends Activity implements View.OnClickListener {
    public static final String RETURN_INPUT = "return_key_input";
    public static final String PARAM_TITLE = "param_title";
    public static final String PARAM_DEFAULT_VALUE = "param_default_value";
    public static final String PARAM_HINT = "param_hint";
    public static final String PARAM_SEL_START = "param_sel_start";
    public static final String PARAM_SEL_END = "param_sel_end";

    private TextView txtTitle;
    private EditText editInput;
    private ImageView btnEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_text_input);

        int sStart = getIntent().getIntExtra(PARAM_SEL_START, -1);
        int sEnd = getIntent().getIntExtra(PARAM_SEL_END, -1);
        txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtTitle.setText(getIntent().getStringExtra(PARAM_TITLE));
        txtTitle.setOnClickListener(this);
        editInput = findViewById(R.id.editInput);
        editInput.setTypeface(Tools.DefaultFont(this, false));
        String def = getIntent().getStringExtra(PARAM_DEFAULT_VALUE);
        if (def != null && !def.equals("")) {
            editInput.setText(def);
            if (sStart == -1 || (sStart >= sEnd)) sStart = 0;
            if (sEnd == -1 || sEnd > (def.length() - 1)) sEnd = def.length();
            editInput.setSelection(sStart, sEnd);
        }
        editInput.setHint(getIntent().getStringExtra(PARAM_HINT));
        editInput.setOnEditorActionListener((v, actionId, event) -> {
            Exit(RESULT_OK);
            return true;
        });
        btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(this);

        Tools.SetDialogPosition(this, Gravity.BOTTOM);
    }

    @Override
    public void onClick(View v) {
        if (v == btnEnter) {
            Exit(RESULT_OK);
        } else if (v == txtTitle) {
            Exit(RESULT_CANCELED);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Tools.ShowSoftKeyboard(this, true, editInput);
    }

    @Override
    public void onBackPressed() {
        Exit(RESULT_CANCELED); // make sure animation will run
    }

    private void Exit(int resCode) {
        if (resCode == RESULT_OK) {
            String txt = editInput.getText().toString();
            if (txt == null || txt.equals("")) return; // must have input

            Intent result = new Intent();
            result.putExtra(RETURN_INPUT, txt);
            setResult(resCode, result);
        }
        Tools.ShowSoftKeyboard(this, false, editInput);
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }
}
