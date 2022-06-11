package com.x.manager.dialog;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.content.*;

import java.util.*;
import java.io.*;
import java.text.*;

import android.widget.AdapterView.*;

import com.x.manager.view.LogDataActivity;
import com.x.manager.R;
import com.x.manager.utility.SanctionBase;
import com.x.manager.utility.Tools;

public class LogsDialog extends Activity implements View.OnClickListener {
    private static final int REQUEST_CONFIRM_DELETE = 4;

    private ListView lvwLogs;
    private List<LogItem> logItems;
    private LogListAdapter adapter;

    private ImageView btnSelectAll, btnUnselectAll, btnDelete;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_logs);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtTitle.setOnClickListener(this);
        ((TextView) findViewById(R.id.txtTotal)).setTypeface(Tools.DefaultFont(this, false));
        ((TextView) findViewById(R.id.txtSelected)).setTypeface(Tools.DefaultFont(this, false));
        lvwLogs = (ListView) findViewById(R.id.lvwLogs);
        lvwLogs.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(LogsDialog.this, LogDataActivity.class);
                intent.putExtra(LogDataActivity.PARAM_LOGPATH, logItems.get(position).LogFile);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            }
        });
        btnSelectAll = (ImageView) findViewById(R.id.btnCheckAll);
        btnSelectAll.setOnClickListener(this);
        btnUnselectAll = (ImageView) findViewById(R.id.btnUncheckAll);
        btnUnselectAll.setOnClickListener(this);
        btnDelete = (ImageView) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(this);

        Tools.SetDialogPosition(this, Gravity.BOTTOM);

        logItems = new ArrayList<LogItem>();
        adapter = new LogListAdapter(this, R.layout.log_list_row);
        lvwLogs.setAdapter(adapter);
        loadLogs();
    }

    @Override
    public void onClick(View v) {
        if (v == btnSelectAll) {
            for (LogItem i : logItems) i.IsSelected = true;
            adapter.notifyDataSetChanged();
            updateSelectedSize();
        } else if (v == btnUnselectAll) {
            for (LogItem i : logItems) i.IsSelected = false;
            adapter.notifyDataSetChanged();
            updateSelectedSize();
        } else if (v == txtTitle) {
            Exit(RESULT_OK);
        } else if (v == btnDelete) {
            if (getSelectedCount() <= 0) {
                Tools.Toast(this, "No log selected", false, false);
                return;
            }
            Intent intent = new Intent(this, ConfirmYesNoDialog.class);
            intent.putExtra(ConfirmYesNoDialog.PARAM_TITLE, "Confirm Delete");
            intent.putExtra(ConfirmYesNoDialog.PARAM_MESSAGE, "Delete selected logs?");
            startActivityForResult(intent, REQUEST_CONFIRM_DELETE);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        }
    }

    @Override
    public void onBackPressed() {
        Exit(RESULT_CANCELED); // make sure animation will run
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONFIRM_DELETE &&
                resultCode == RESULT_OK) {
            for (LogItem l : logItems) {
                if (l.IsSelected) {
                    if (new File(l.LogFile).delete()) {
                        SanctionBase.WriteLog(this, SanctionBase.LogType.NORMAL,
                                "Log deleted: " + l.LogFile);
                    } else {
                        SanctionBase.WriteLog(this, SanctionBase.LogType.ERROR,
                                "Log delete failed: " + l.LogFile);
                    }
                }
            }
            Parcelable state = lvwLogs.onSaveInstanceState();
            loadLogs();
            lvwLogs.onRestoreInstanceState(state);
        }
    }

    private void loadLogs() {
        long sizeTotal = 0;
        logItems.clear();
        File[] logs = SanctionBase.GetLogsList();
        Tools.SortFileList(logs, Tools.Sorting.TYPE_FILENAME,
                Tools.Sorting.GROUPING_FOLDER_FIRST, true);
        for (File f : logs) {
            LogItem item = new LogItem();
            String name = f.getName();
            name = name.substring(0, name.length() - 4);
            item.Title = name;
            item.ActualSize = f.length();
            sizeTotal += item.ActualSize;
            item.SubTitle = Tools.ToDataUnitString(item.ActualSize);
            item.IsSelected = false;
            item.LogFile = f.getAbsolutePath();
            logItems.add(item);
        }

        adapter.notifyDataSetChanged();
        ((TextView) findViewById(R.id.txtTotal))
                .setText(NumberFormat.getIntegerInstance().format(logItems.size()) + " total items (" +
                        Tools.ToDataUnitString(sizeTotal) + ")");
        updateSelectedSize();
    }

    private void updateSelectedSize() {
        long size = 0;
        int selCount = 0;
        for (LogItem l : logItems) {
            if (l.IsSelected) {
                selCount++;
                size += l.ActualSize;
            }
        }
        ((TextView) findViewById(R.id.txtSelected))
                .setText(NumberFormat.getIntegerInstance().format(selCount) + " selected items (" +
                        Tools.ToDataUnitString(size) + ")");
    }

    private int getSelectedCount() {
        int selCount = 0;
        for (LogItem l : logItems) {
            if (l.IsSelected) selCount++;
        }
        return selCount;
    }

    private void Exit(int resCode) {
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    public static class LogItem {
        public String Title = "";
        public String SubTitle = "";
        public boolean IsSelected = false;
        public long ActualSize = 0;
        public String LogFile;
    }

    public class LogListAdapter extends ArrayAdapter<LogItem> {
        private final int resourceLayout;
        private final Context mContext;

        public LogListAdapter(Context context, int resource) {
            super(context, resource, logItems);
            this.resourceLayout = resource;
            this.mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = LayoutInflater.from(mContext);
                v = vi.inflate(resourceLayout, null);
            }

            LogItem p = getItem(position);
            if ((position % 2) != 0) v.setBackgroundColor(Color.parseColor("#44444444"));
            else v.setBackgroundColor(Color.parseColor("#00000000"));
            if (p != null) {
                // logname
                TextView txtTitle = (TextView) v.findViewById(R.id.txtLogname);
                txtTitle.setTypeface(Tools.DefaultFont(LogsDialog.this, false));
                txtTitle.setText(p.Title);
                // size
                TextView txtSubtitle = (TextView) v.findViewById(R.id.txtSize);
                txtSubtitle.setTypeface(Tools.DefaultFont(LogsDialog.this, false));
                txtSubtitle.setText(p.SubTitle);
                // selected state
                ImageView imgChecked = (ImageView) v.findViewById(R.id.imgCheckbox);
                imgChecked.setTag(position);
                imgChecked.setImageResource(p.IsSelected ? R.mipmap.ic_selected : R.mipmap.ic_unselected);
                imgChecked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = (int) v.getTag();
                        LogItem sel = logItems.get(pos);
                        sel.IsSelected = !sel.IsSelected;
                        adapter.notifyDataSetChanged();
                        updateSelectedSize();
                    }
                });
            }

            return v;
        }
    }
}
