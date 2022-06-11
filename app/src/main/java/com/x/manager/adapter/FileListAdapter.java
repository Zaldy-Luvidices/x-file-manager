package com.x.manager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.x.manager.utility.MimeManager;
import com.x.manager.R;
import com.x.manager.utility.Tools;
import com.x.manager.model.FileListItem;
import com.x.manager.view.MainActivity;

public class FileListAdapter extends ArrayAdapter<FileListItem> {
    private final int resourceLayout;
    private final Context mContext;

    public FileListAdapter(Context context, int resource) {
        super(context, resource, MainActivity.Self.fileItems);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        FileListItem p = getItem(position);
        if (p != null) {
            // select mode
            if (MainActivity.Self.IsSelectMode) {
                if (p.IsSelected) v.setBackgroundColor(Color.rgb(0x29, 0x29, 0x29));
                else v.setBackgroundColor(Color.TRANSPARENT);
            }
            // destination select mode
            else if (MainActivity.Self.IsDestMode) {
                if (MainActivity.Self.isPathSelected(p.ID))
                    v.setBackgroundColor(Color.rgb(0x29, 0x29, 0x29));
                else v.setBackgroundColor(Color.TRANSPARENT);
            }
            // normal mode
            else v.setBackgroundResource(R.drawable.selector_list_main);

            // icon
            ImageView imgIcon = (ImageView) v.findViewById(R.id.imgIcon);
            imgIcon.setTag(position);
            if (p.ItemType == FileListItem.ListItemType.FILE) {
                imgIcon.setImageResource(MimeManager.GetIconResourceFromType(p.GroupID));
            } else if (p.ItemType == FileListItem.ListItemType.FOLDER) {
                imgIcon.setImageResource(Tools.GetFolderIcon(p.Title));
            } else imgIcon.setImageResource(R.mipmap.ic_file);
            imgIcon.setOnClickListener(v12 -> {

            });
            // filename
            TextView txtTitle = (TextView) v.findViewById(R.id.txtFilename);
            txtTitle.setTypeface(Tools.DefaultFont(MainActivity.Self, false));
            txtTitle.setText(p.Title);
            // top line separator
            View sepTop = v.findViewById(R.id.viewSepTop);
            if (position == 0) sepTop.setVisibility(View.VISIBLE);
            else sepTop.setVisibility(View.GONE);
            // file size or folder content count
            TextView txtSub1 = (TextView) v.findViewById(R.id.txtSize);
            txtSub1.setText(p.SubTitle1);
            // last modified date
            TextView txtSub2 = (TextView) v.findViewById(R.id.txtDate);
            txtSub2.setText(p.SubTitle2);
            // single-item options
            ImageView imgOptions = (ImageView) v.findViewById(R.id.imgOptions);
            imgOptions.setTag(position);
            imgOptions.setOnClickListener(v1 -> {

            });
        }

        return v;
    }
}