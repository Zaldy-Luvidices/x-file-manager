package com.x.manager.view;

import android.app.*;
import android.view.*;
import android.os.*;
import android.widget.*;
import android.content.*;

import java.util.*;

import android.graphics.*;

import java.io.*;
import java.text.*;

import android.widget.AdapterView.*;

import com.x.manager.R;
import com.x.manager.utility.SanctionBase;
import com.x.manager.utility.Tools;

public class LogDataActivity extends Activity implements View.OnClickListener {
    public static final String PARAM_LOGPATH = "param_log_path";

    private static final int TYPE_NORMAL = 1;
    private static final int TYPE_INFORMATION = 2;
    private static final int TYPE_WARNING = 3;
    private static final int TYPE_ERROR = 4;

    private TextView txtCount, txtSize, txtTitle;
    private int colNormal, colInfo, colWarning, colError;
    private ListView lvwContent;

    private List<LogData> logData;
    private List<LogData> logAll;
    private LogDataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_log_data);

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtCount = (TextView) findViewById(R.id.txtCount);
        txtCount.setTypeface(Tools.DefaultFont(this, false));
        txtSize = (TextView) findViewById(R.id.txtSize);
        txtSize.setTypeface(Tools.DefaultFont(this, false));
        ImageView btn;
        btn = (ImageView) findViewById(R.id.btnError);
        btn.setOnClickListener(this);
        btn.setTag(true);
        btn = (ImageView) findViewById(R.id.btnWarning);
        btn.setOnClickListener(this);
        btn.setTag(true);
        btn = (ImageView) findViewById(R.id.btnInfo);
        btn.setOnClickListener(this);
        btn.setTag(true);
        btn = (ImageView) findViewById(R.id.btnNormal);
        btn.setOnClickListener(this);
        btn.setTag(true);
        btn = (ImageView) findViewById(R.id.btnCopy);
        btn.setOnClickListener(this);
        lvwContent = (ListView) findViewById(R.id.lvwContent);
        lvwContent.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                LogData dat = logData.get(position);
                String txt;
                if (dat.Type == TYPE_NORMAL) txt = "Normal";
                else if (dat.Type == TYPE_INFORMATION) txt = "Information";
                else if (dat.Type == TYPE_WARNING) txt = "Warning";
                else if (dat.Type == TYPE_ERROR) txt = "Error";
                else txt = "Unknown";
                txt = "[" + dat.Title + "]" + txt + ":" + dat.SubTitle;
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", txt);
                clipboard.setPrimaryClip(clip);
                Tools.Toast(LogDataActivity.this, "Data was copied to clipboard", false, false);
            }
        });

        colNormal = Color.parseColor("#ffaaaaaa");
        colInfo = Color.parseColor("#ff90caf9");
        colWarning = Color.parseColor("#ffff6e40");
        colError = Color.parseColor("#ffef5350");

        logData = new ArrayList<LogData>();
        logAll = new ArrayList<LogData>();
        adapter = new LogDataAdapter(this, R.layout.log_entry_row);
        lvwContent.setAdapter(adapter);
        loadLogContent(getIntent().getStringExtra(PARAM_LOGPATH));
    }

    @Override
    public void onClick(View v) {
        if (v == txtTitle) {
            Exit(RESULT_CANCELED);
        } else if (v.getId() == R.id.btnError ||
                v.getId() == R.id.btnWarning ||
                v.getId() == R.id.btnInfo ||
                v.getId() == R.id.btnNormal) {
            toggleCheckboxState((ImageView) v);
        } else if (v.getId() == R.id.btnCopy) {
            copyAllData();
        }
    }

    @Override
    public void onBackPressed() {
        Exit(RESULT_CANCELED); // make sure animation will run
    }

    private void copyAllData() {
        String allDat = "";
        for (LogData d : logData) {
            String txt;
            if (d.Type == TYPE_NORMAL) txt = "Normal";
            else if (d.Type == TYPE_INFORMATION) txt = "Information";
            else if (d.Type == TYPE_WARNING) txt = "Warning";
            else if (d.Type == TYPE_ERROR) txt = "Error";
            else txt = "Unknown";
            txt = "[" + d.Title + "]" + txt + ":" + d.SubTitle;
            allDat += txt + "\n";
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", allDat);
        clipboard.setPrimaryClip(clip);
        Tools.Toast(LogDataActivity.this, "All data were copied to clipboard", false, false);
    }

    private void toggleCheckboxState(ImageView chk) {
        boolean state = (boolean) chk.getTag();
        state = !state;
        chk.setTag(state);
        if (state) chk.setImageResource(R.mipmap.ic_selected);
        else chk.setImageResource(R.mipmap.ic_unselected);
        filterData();
    }

    private void Exit(int resCode) {
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    private void loadLogContent(String logFile) {
        logAll.clear();
        File file = new File(logFile);
        if (!file.exists()) {
            Tools.Toast(this, "Log file doesn't exists!", true, true);
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            line = reader.readLine(); // log name
            txtTitle.setText(line);
            while ((line = reader.readLine()) != null) {
                LogData data = new LogData();
                data.DataCount = line.length();
                data.Type = TYPE_ERROR;
                data.Title = "(Error)";
                data.SubTitle = "(Error)";
                String[] col = line.split(SanctionBase.SEPARATOR_LOG);
                if (col != null) {
                    if (col.length > 0) { // type
                        if (col[0].equals(SanctionBase.ID_NORMAL))
                            data.Type = TYPE_NORMAL;
                        else if (col[0].equals(SanctionBase.ID_INFO))
                            data.Type = TYPE_INFORMATION;
                        else if (col[0].equals(SanctionBase.ID_WARNING))
                            data.Type = TYPE_WARNING;
                        else if (col[0].equals(SanctionBase.ID_ERROR))
                            data.Type = TYPE_ERROR;
                    }
                    if (col.length > 1) { // time stamp
                        data.Title = col[1];
                    }
                    if (col.length > 2) { // content
                        data.SubTitle = col[2];
                    }
                }
                logAll.add(data);
            }
            reader.close();
        } catch (FileNotFoundException ex) {
            Tools.Toast(this, "Cannot find file: " + ex.toString(), true, true);
            return;
        } catch (IOException ex) {
            Tools.Toast(this, "Failed to read file: " + ex.toString(), true, true);
            return;
        }

        filterData();
    }

    private void filterData() {
        logData.clear();
        long totalSize = 0;
        for (LogData d : logAll) {
            if (d.Type == TYPE_NORMAL &&
                    findViewById(R.id.btnNormal).getTag().equals(false)) {
                continue;
            }
            if (d.Type == TYPE_INFORMATION &&
                    findViewById(R.id.btnInfo).getTag().equals(false)) {
                continue;
            }
            if (d.Type == TYPE_WARNING &&
                    findViewById(R.id.btnWarning).getTag().equals(false)) {
                continue;
            }
            if (d.Type == TYPE_ERROR &&
                    findViewById(R.id.btnError).getTag().equals(false)) {
                continue;
            }

            logData.add(d);
            totalSize += d.DataCount;
        }

        txtCount.setText("Entries: " + NumberFormat.getIntegerInstance().format(logData.size()));
        txtSize.setText("Data Size: " + Tools.ToDataUnitString(totalSize));
        adapter.notifyDataSetChanged();
    }

    public static class LogData {
        public int Type = LogDataActivity.TYPE_NORMAL;
        public String Title = "";
        public String SubTitle = "";
        public long DataCount = 0;
    }

    public class LogDataAdapter extends ArrayAdapter<LogData> {
        private final int resourceLayout;
        private final Context mContext;

        public LogDataAdapter(Context context, int resource) {
            super(context, resource, logData);
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

            LogData p = getItem(position);
            if ((position % 2) != 0) v.setBackgroundColor(Color.parseColor("#44444444"));
            else v.setBackgroundColor(Color.parseColor("#00000000"));
            if (p != null) {
                int col;
                if (p.Type == TYPE_NORMAL) col = colNormal;
                else if (p.Type == TYPE_INFORMATION) col = colInfo;
                else if (p.Type == TYPE_WARNING) col = colWarning;
                else if (p.Type == TYPE_ERROR) col = colError;
                else col = Color.parseColor("#ffffffff");
                // color
                View bar = v.findViewById(R.id.viewColor);
                bar.setBackgroundColor(col);
                // timestamp
                TextView txtStamp = (TextView) v.findViewById(R.id.txtTimeStamp);
                txtStamp.setTypeface(Tools.DefaultFont(LogDataActivity.this, false));
                txtStamp.setText(p.Title);
                // content
                TextView txtSubtitle = (TextView) v.findViewById(R.id.txtContent);
                txtSubtitle.setTypeface(Tools.DefaultFont(LogDataActivity.this, false));
                txtSubtitle.setText(p.SubTitle);
                txtSubtitle.setTextColor(col);
            }

            return v;
        }
    }
}
