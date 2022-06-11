package com.x.manager.dialog;

import android.widget.*;
import android.app.*;
import android.view.*;
import android.os.*;

import java.util.*;

import android.content.*;
import android.widget.AdapterView.*;
import android.graphics.*;

import com.x.manager.model.MenuItem;
import com.x.manager.R;
import com.x.manager.utility.Tools;

public class MenuDialog extends Activity implements View.OnClickListener {
    public static final String RETURN_MENU_ID = "return_menu_id";
    public static final String PARAM_IDS = "param_ids";
    public static final String PARAM_ICONS = "param_icons";
    public static final String PARAM_TITLES = "param_titles";
    public static final String PARAM_IS_DEVS = "param_is_dev";
    public static final String PARAM_MODE = "param_mode";
    public static final String PARAM_TITLE = "param_title";

    public static final int MODE_GENERAL = 0;
    public static final int MODE_SINGLE = 1;
    public static final int MODE_MULTI = 2;

    private List<MenuItem> menuItems;
    private MenuListAdapter adapter;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_menu);
        int mode = getIntent().getIntExtra(PARAM_MODE, MODE_GENERAL);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtTitle.setOnClickListener(this);
        txtTitle.setText(getIntent().getStringExtra(PARAM_TITLE));
        ListView lvwMenu = (ListView) findViewById(R.id.lvwMenu);
        lvwMenu.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Exit(menuItems.get(position).MenuID, RESULT_OK);
            }
        });

        int grav = Gravity.BOTTOM;
        if (mode == MODE_GENERAL) grav = Gravity.RIGHT;
        else if (mode == MODE_SINGLE) grav = Gravity.BOTTOM;
        Tools.SetDialogPosition(this, grav);

        menuItems = new ArrayList<MenuItem>();
        adapter = new MenuListAdapter(this, R.layout.menu_list_row);
        lvwMenu.setAdapter(adapter);
        loadMenu();
    }

    private void loadMenu() {
        menuItems.clear();
        // parse ids
        String ids = getIntent().getStringExtra(PARAM_IDS);
        String[] dat = ids.split(MenuItem.Separator);
        //Tools.Toast(this, ids, false, false);
        for (String s : dat) {
            MenuItem item =
                    new MenuItem(Integer.parseInt(s), "(Error)", R.mipmap.ic_menu, false);
            menuItems.add(item);
        }
        // parse icons
        ids = getIntent().getStringExtra(PARAM_ICONS);
        dat = ids.split(MenuItem.Separator);
        for (int i = 0; i < menuItems.size(); ++i) {
            if (i >= dat.length) break;
            menuItems.get(i).IconResource = Integer.parseInt(dat[i]);
        }
        // parse titles
        ids = getIntent().getStringExtra(PARAM_TITLES);
        dat = ids.split(MenuItem.Separator);
        for (int i = 0; i < menuItems.size(); ++i) {
            if (i >= dat.length) break;
            menuItems.get(i).MenuName = dat[i];
        }
        // parse IsDev flags
        ids = getIntent().getStringExtra(PARAM_IS_DEVS);
        dat = ids.split(MenuItem.Separator);
        for (int i = 0; i < menuItems.size(); ++i) {
            if (i >= dat.length) break;
            menuItems.get(i).IsDevMenu = dat[i].equals("1");
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v == txtTitle) {
            Exit(-1, RESULT_CANCELED);
        }
    }

    @Override
    public void onBackPressed() {
        Exit(-1, RESULT_CANCELED); // make sure animation will run
    }

    private void Exit(int selMenuId, int resCode) {
        if (resCode == RESULT_OK) {
            Intent result = new Intent();
            result.putExtra(RETURN_MENU_ID, selMenuId);
            setResult(resCode, result);
        }
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    public class MenuListAdapter extends ArrayAdapter<MenuItem> {
        private final int resourceLayout;
        private final Context mContext;

        public MenuListAdapter(Context context, int resource) {
            super(context, resource, menuItems);
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

            MenuItem p = getItem(position);
            if (p.IsDevMenu) v.setBackgroundColor(Color.parseColor("#44444444"));
            else v.setBackgroundColor(Color.parseColor("#00000000"));
            if (p != null) {
                // icon
                ImageView imgIcon = (ImageView) v.findViewById(R.id.imgMenuIcon);
                imgIcon.setImageResource(p.IconResource);
                TextView txtTitle = (TextView) v.findViewById(R.id.txtMenuName);
                txtTitle.setTypeface(Tools.DefaultFont(MenuDialog.this, false));
                txtTitle.setText(p.MenuName);
            }

            return v;
        }
    }
}
