package com.x.manager.dialog;

import android.widget.*;
import android.os.*;
import android.app.*;
import android.view.*;
import android.content.*;

import com.x.manager.R;
import com.x.manager.utility.Tools;

public class SortDialog extends Activity implements View.OnClickListener {
    public static final String PARAM_SORT_TYPE = "PARAM_SORT_TYPE";
    public static final String PARAM_SORT_GROUP = "PARAM_SORT_GROUP";
    public static final String PARAM_REVERSE = "PARAM_REVERSE";

    private static final int GROUP_SORT = 0;
    private static final int GROUP_GROUP = 1;
    private static final int GROUP_UNKNOWN = 2;

    private TextView txtTitle;
    private ImageView btnSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_sort);
        txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtTitle.setOnClickListener(this);
        ((TextView) findViewById(R.id.txtTitle2)).setTypeface(Tools.DefaultFont(this, true));
        btnSort = findViewById(R.id.btnSort);
        btnSort.setOnClickListener(this);

        ImageView chk;
        TextView txt;
        chk = findViewById(R.id.chkReverse);
        chk.setTag(false);
        chk.setOnClickListener(this);
        txt = findViewById(R.id.chkReverseText);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setOnClickListener(this);
        txt.setTag(GROUP_UNKNOWN);
        if (getIntent().getBooleanExtra(PARAM_REVERSE, false))
            toggleCheckboxState(chk);

        // sort types
        int i = getIntent().getIntExtra(PARAM_SORT_TYPE, Tools.Sorting.TYPE_FILENAME);
        chk = findViewById(R.id.chkName);
        chk.setTag(false);
        chk.setOnClickListener(this);
        txt = findViewById(R.id.chkNameText);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setOnClickListener(this);
        txt.setTag(GROUP_SORT);
        if (i == Tools.Sorting.TYPE_FILENAME) setCheckboxState(chk, true);
        chk = findViewById(R.id.chkFileSize);
        chk.setTag(false);
        chk.setOnClickListener(this);
        txt = findViewById(R.id.chkFileSizeText);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setOnClickListener(this);
        txt.setTag(GROUP_SORT);
        if (i == Tools.Sorting.TYPE_FILE_SIZE) setCheckboxState(chk, true);
        chk = findViewById(R.id.chkFolderItems);
        chk.setTag(false);
        chk.setOnClickListener(this);
        txt = findViewById(R.id.chkFolderItemsText);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setOnClickListener(this);
        txt.setTag(GROUP_SORT);
        if (i == Tools.Sorting.TYPE_FOLDER_ITEMS) setCheckboxState(chk, true);
        chk = findViewById(R.id.chkModDate);
        chk.setTag(false);
        chk.setOnClickListener(this);
        txt = findViewById(R.id.chkModDateText);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setOnClickListener(this);
        txt.setTag(GROUP_SORT);
        if (i == Tools.Sorting.TYPE_DATE_MODIFIED) setCheckboxState(chk, true);

        // sort grouping
        i = getIntent().getIntExtra(PARAM_SORT_GROUP, Tools.Sorting.GROUPING_FOLDER_FIRST);
        chk = findViewById(R.id.chkCombined);
        chk.setTag(false);
        chk.setOnClickListener(this);
        txt = findViewById(R.id.chkCombinedText);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setOnClickListener(this);
        txt.setTag(GROUP_GROUP);
        if (i == Tools.Sorting.GROUPING_COMBINED) setCheckboxState(chk, true);
        chk = findViewById(R.id.chkFolderFirst);
        chk.setTag(false);
        chk.setOnClickListener(this);
        txt = findViewById(R.id.chkFolderFirstText);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setOnClickListener(this);
        txt.setTag(GROUP_GROUP);
        if (i == Tools.Sorting.GROUPING_FOLDER_FIRST) setCheckboxState(chk, true);
        chk = findViewById(R.id.chkFileFirst);
        chk.setTag(false);
        chk.setOnClickListener(this);
        txt = findViewById(R.id.chkFileFirstText);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setOnClickListener(this);
        txt.setTag(GROUP_GROUP);
        if (i == Tools.Sorting.GROUPING_FILE_FIRST) setCheckboxState(chk, true);

        Tools.SetDialogPosition(this, Gravity.BOTTOM);
    }

    @Override
    public void onBackPressed() {
        Exit(RESULT_CANCELED); // make sure animation will run
    }

    @Override
    public void onClick(View v) {
        if (v == txtTitle)
            Exit(RESULT_CANCELED);
        else if (v.getId() == R.id.chkReverse || v.getId() == R.id.chkReverseText)
            toggleCheckboxState(findViewById(R.id.chkReverse));
        else if (v.getId() == R.id.chkName || v.getId() == R.id.chkNameText)
            selectSortTypes(findViewById(R.id.chkName));
        else if (v.getId() == R.id.chkFileSize || v.getId() == R.id.chkFileSizeText)
            selectSortTypes(findViewById(R.id.chkFileSize));
        else if (v.getId() == R.id.chkFolderItems || v.getId() == R.id.chkFolderItemsText)
            selectSortTypes(findViewById(R.id.chkFolderItems));
        else if (v.getId() == R.id.chkModDate || v.getId() == R.id.chkModDateText)
            selectSortTypes(findViewById(R.id.chkModDate));
        else if (v.getId() == R.id.chkCombined || v.getId() == R.id.chkCombinedText)
            selectSortGroup(findViewById(R.id.chkCombined));
        else if (v.getId() == R.id.chkFolderFirst || v.getId() == R.id.chkFolderFirstText)
            selectSortGroup(findViewById(R.id.chkFolderFirst));
        else if (v.getId() == R.id.chkFileFirst || v.getId() == R.id.chkFileFirstText)
            selectSortGroup(findViewById(R.id.chkFileFirst));
        else if (v == btnSort)
            Exit(RESULT_OK);
    }

    private void selectSortTypes(ImageView selected) {
        setCheckboxState(findViewById(R.id.chkName), false);
        setCheckboxState(findViewById(R.id.chkFileSize), false);
        setCheckboxState(findViewById(R.id.chkFolderItems), false);
        setCheckboxState(findViewById(R.id.chkModDate), false);
        setCheckboxState(selected, true);
    }

    private void selectSortGroup(ImageView selected) {
        setCheckboxState(findViewById(R.id.chkCombined), false);
        setCheckboxState(findViewById(R.id.chkFolderFirst), false);
        setCheckboxState(findViewById(R.id.chkFileFirst), false);
        setCheckboxState(selected, true);
    }

    private void setCheckboxState(ImageView chk, boolean isChecked) {
        chk.setTag(isChecked);
        if (isChecked) chk.setImageResource(R.mipmap.ic_selected);
        else chk.setImageResource(R.mipmap.ic_unselected);
    }

    private void toggleCheckboxState(ImageView chk) {
        boolean state = (boolean) chk.getTag();
        state = !state;
        chk.setTag(state);
        if (state) chk.setImageResource(R.mipmap.ic_selected);
        else chk.setImageResource(R.mipmap.ic_unselected);
    }

    private void Exit(int resCode) {
        if (resCode == RESULT_OK) {
            Intent result = new Intent();
            // sort type
            int i = Tools.Sorting.TYPE_FILENAME;
            if (findViewById(R.id.chkFileSize).getTag().equals(true))
                i = Tools.Sorting.TYPE_FILE_SIZE;
            else if (findViewById(R.id.chkFolderItems).getTag().equals(true))
                i = Tools.Sorting.TYPE_FOLDER_ITEMS;
            else if (findViewById(R.id.chkModDate).getTag().equals(true))
                i = Tools.Sorting.TYPE_DATE_MODIFIED;
            result.putExtra(PARAM_SORT_TYPE, i);
            // sort grouping
            i = Tools.Sorting.GROUPING_FOLDER_FIRST;
            if (findViewById(R.id.chkCombined).getTag().equals(true))
                i = Tools.Sorting.GROUPING_COMBINED;
            else if (findViewById(R.id.chkFileFirst).getTag().equals(true))
                i = Tools.Sorting.GROUPING_FILE_FIRST;
            result.putExtra(PARAM_SORT_GROUP, i);
            // is reverse
            result.putExtra(PARAM_REVERSE, findViewById(R.id.chkReverse).getTag().equals(true));
            setResult(resCode, result);
        }
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }
}
