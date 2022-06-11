package com.x.manager.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.x.manager.R;
import com.x.manager.utility.SanctionBase;
import com.x.manager.utility.Tools;
import com.x.manager.view.MainActivity;

public class PasteOptionsDialog extends Activity
        implements View.OnClickListener, View.OnLongClickListener {
    public static final String PARAM_TEXT_ORIGINAL = "PARAM_TEXT_ORIGINAL";
    public static final String PARAM_TEXT_NEW = "PARAM_TEXT_NEW";
    public static final String PARAM_OP_TYPE = "PARAM_OP_TYPE";

    public static final String RESULT_SELECTED_OPTION = "RESULT_SELECTED_OPTION";
    public static final String RESULT_REMEMBER_ALL = "RESULT_REMEMBER_ALL";
    public static final String RESULT_OPERATION_TYPE = "RESULT_OPERATION_TYPE";

    public static final int OP_TYPE_FILE_TO_FILE = 0;
    public static final int OP_TYPE_FILE_TO_FOLDER = 1;
    public static final int OP_TYPE_FOLDER_TO_FOLDER = 2;
    public static final int OP_TYPE_FOLDER_TO_FILE = 3;
    public static final int OP_TYPE_SAME_FILE = 4;

    public static final int OPTION_NONE = 0; // normaly copy
    public static final int OPTION_COPY = 1; // rename and copy
    public static final int OPTION_OVERWRITE = 2; // overwrite existing
    public static final int OPTION_SKIP = 3; // skip current file
    public static final int OPTION_ABORT = 4; // abort operation
    public static final int OPTION_MERGE = 5; // merge folder to folder

    private ImageView chkRemember;
    private Integer operationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_paste_options);

        TextView txtSource, txtDest;
        txtSource = findViewById(R.id.txtSource);
        txtSource.setText(filenameTag(true) + getIntent().getStringExtra(PARAM_TEXT_ORIGINAL));
        txtSource.setTypeface(Tools.DefaultFont(this, false));
        txtDest = findViewById(R.id.txtDest);
        txtDest.setText(filenameTag(false) + getIntent().getStringExtra(PARAM_TEXT_NEW));
        txtDest.setTypeface(Tools.DefaultFont(this, false));
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtTitle.setOnClickListener(this);
        chkRemember = findViewById(R.id.chkAll);
        chkRemember.setTag("false");
        chkRemember.setOnClickListener(this);

        findViewById(R.id.btnCopy).setOnClickListener(this);
        findViewById(R.id.btnCopy).setOnLongClickListener(this);
        findViewById(R.id.btnOverwrite).setOnClickListener(this);
        findViewById(R.id.btnOverwrite).setOnLongClickListener(this);
        findViewById(R.id.btnMerge).setOnClickListener(this);
        findViewById(R.id.btnMerge).setOnLongClickListener(this);
        findViewById(R.id.btnSkip).setOnClickListener(this);
        findViewById(R.id.btnSkip).setOnLongClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnLongClickListener(this);
        findViewById(R.id.chkAllText).setOnClickListener(this);

        operationType = getIntent().getIntExtra(PARAM_OP_TYPE, 0);
        if (operationType == OP_TYPE_FILE_TO_FOLDER
                || operationType == OP_TYPE_FOLDER_TO_FILE
                || operationType == OP_TYPE_SAME_FILE) {
            findViewById(R.id.btnOverwrite).setVisibility(View.GONE);
        } else if (operationType == OP_TYPE_FOLDER_TO_FOLDER) {
            findViewById(R.id.btnOverwrite).setVisibility(View.GONE);
            findViewById(R.id.btnMerge).setVisibility(View.VISIBLE);
        }

        Tools.SetDialogPosition(this, Gravity.BOTTOM);
    }

    @Override
    public void onBackPressed() {
        exit(RESULT_CANCELED, -1); // make sure animation will run
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCopy:
                exit(RESULT_OK, OPTION_COPY);
                break;
            case R.id.btnOverwrite:
                exit(RESULT_OK, OPTION_OVERWRITE);
                break;
            case R.id.btnMerge:
                exit(RESULT_OK, OPTION_MERGE);
                break;
            case R.id.btnSkip:
                exit(RESULT_OK, OPTION_SKIP);
                break;
            case R.id.btnCancel:
                exit(RESULT_OK, OPTION_ABORT);
                break;
            case R.id.chkAll:
            case R.id.chkAllText:
                toggleRememberState();
                break;
        }
    }

    private void toggleRememberState() {
        boolean isSelected = Boolean.parseBoolean(chkRemember.getTag().toString());
        isSelected = !isSelected;
        if (isSelected) {
            chkRemember.setTag("true");
            chkRemember.setImageResource(R.mipmap.ic_selected);
        } else {
            chkRemember.setTag("false");
            chkRemember.setImageResource(R.mipmap.ic_unselected);
        }
    }

    private void exit(int resCode, int selOption) {
        if (resCode == RESULT_OK) {
            Intent result = new Intent();
            result.putExtra(RESULT_SELECTED_OPTION, selOption);
            result.putExtra(RESULT_REMEMBER_ALL, Boolean.parseBoolean(chkRemember.getTag().toString()));
            result.putExtra(RESULT_OPERATION_TYPE, operationType);
            this.setResult(resCode, result);
        }
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    private String filenameTag(boolean isSource) {
        switch (getIntent().getIntExtra(PARAM_OP_TYPE, 0)) {
            case OP_TYPE_FILE_TO_FILE:
            case OP_TYPE_SAME_FILE:
                return "File: ";
            case OP_TYPE_FILE_TO_FOLDER:
                return (isSource ? "File: " : "Folder: ");
            case OP_TYPE_FOLDER_TO_FOLDER:
                return "Folder: ";
            case OP_TYPE_FOLDER_TO_FILE:
                return (isSource ? "Folder: " : "File: ");
            default:
                return "Unknown: ";
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Tools.ShowButtonTip(this, v);
        return true;
    }
}
