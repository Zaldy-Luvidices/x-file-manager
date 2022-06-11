package com.x.manager.dialog;

import android.app.*;
import android.view.*;
import android.widget.*;
import android.os.*;

import com.x.manager.R;
import com.x.manager.utility.Tools;

public class ProgressDialog extends Activity implements View.OnClickListener {
    public static final String PARAM_TITLE = "param_title";
    public static final String PARAM_MESSAGE = "param_message";
    public static final String PARAM_SHOW_CANCEL = "param_show_cancel";
    public static final String PARAM_HAS_PROGRESS = "param_has_progress";
    //public static final String PARAM_BAR_MAX = "param_bar_max";

    private TextView txtMessage;
    private ImageView btnCancel;

    private boolean hasProgress = true;

    public static String ProgressMessage = "Initializing...";
    public static boolean ProgressCancelEnabled = true;
    public static boolean ProgressCompleted = false;
    public static boolean ProgressCancelled = false;
    public static boolean ProgressChanged = false;
    public static int ProgressValue = 0;
    public static int ProgressMax = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtTitle.setText(getIntent().getStringExtra(PARAM_TITLE));
        txtTitle.setOnClickListener(this);
        txtMessage = findViewById(R.id.txtMessage);
        txtMessage.setTypeface(Tools.DefaultFont(this, false));
        //txtMessage.setText(getIntent().getStringExtra(PARAM_MESSAGE));
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        if (!getIntent().getBooleanExtra(PARAM_SHOW_CANCEL, true))
            btnCancel.setVisibility(View.GONE);
        hasProgress = getIntent().getBooleanExtra(PARAM_HAS_PROGRESS, true);
        if (!hasProgress) findViewById(R.id.relBarSize).setVisibility(View.INVISIBLE);

        Tools.SetDialogPosition(this, Gravity.BOTTOM);

        new Thread(() -> {
            while (!ProgressCompleted && !ProgressCancelled) {
                if (ProgressChanged) {
                    ProgressChanged = false;
                    txtMessage.post(() -> {
                        txtMessage.setText(ProgressMessage);
                        btnCancel.setEnabled(ProgressCancelEnabled);

                        if (!hasProgress) return;
                        int barMax = findViewById(R.id.relBarSize).getMeasuredWidth();
                        int barSize = barMax;
                        if (ProgressMax > 0)
                            barSize = (ProgressValue * barMax) / ProgressMax;
                        View v = findViewById(R.id.viewBarSizeFill);
                        v.setLayoutParams(new RelativeLayout.LayoutParams(
                                barSize, RelativeLayout.LayoutParams.MATCH_PARENT));
                        findViewById(R.id.relBarSize).invalidate();
                    });
                }
            }

            if (ProgressCompleted) {
                txtMessage.post(() -> Exit(RESULT_OK));
            } else {
                txtMessage.post(() -> {
                    txtMessage.setText("Cancelling...");
                    // wait for UI thread to dispatch this process
                });
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        if (v == btnCancel) {
            ProgressCancelled = true;
            btnCancel.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        // ignore back press
    }

    private void Exit(int resCode) {
        setResult(resCode);
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    public static void InitProgress() {
        ProgressMessage = "Initializing...";
        ProgressCancelEnabled = true;
        ProgressCompleted = false;
        ProgressCancelled = false;
        ProgressChanged = false;
        ProgressValue = 0;
        ProgressMax = 0;
    }

    public static void ChangeProgress(String message, boolean cancellable, boolean increment) {
        ProgressMessage = message;
        if (increment) {
            ProgressValue++;
        }
        ProgressChanged = true;
        ProgressCancelEnabled = cancellable;
    }
}

