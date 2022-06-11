package com.x.manager.view;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.widget.*;
import android.view.*;
import android.content.*;

import java.util.*;

import java.io.*;
import java.text.*;

import android.content.res.*;
import android.webkit.*;
import android.net.*;

import androidx.core.content.FileProvider;

import com.x.manager.function.ChangeExt;
import com.x.manager.function.FileCopy;
import com.x.manager.function.FileMove;
import com.x.manager.utility.MimeManager;
import com.x.manager.R;
import com.x.manager.utility.SanctionBase;
import com.x.manager.utility.Tools;
import com.x.manager.adapter.FileListAdapter;
import com.x.manager.dialog.ConfirmYesNoDialog;
import com.x.manager.dialog.LogsDialog;
import com.x.manager.dialog.MenuDialog;
import com.x.manager.dialog.SortDialog;
import com.x.manager.dialog.TextInputDialog;
import com.x.manager.function.ExportListing;
import com.x.manager.function.FileDelete;
import com.x.manager.model.FileListItem;
import com.x.manager.model.FileTabItem;
import com.x.manager.model.MenuItem;

public class MainActivity extends Activity implements View.OnClickListener {
    public static final int REQUEST_NEW_FOLDER = 72;
    public static final int REQUEST_TOOL_CODE = 73;
    public static final int REQUEST_SORT = 74;
    public static final int REQUEST_MENU = 75;
    public static final int REQUEST_EXPORT_LIST_NAME = 76;
    public static final int REQUEST_PROGRESS = 77;
    public static final int REQUEST_CONFIRM_DELETE = 78;
    public static final int REQUEST_RENAME = 79;
    public static final int REQUEST_NEW_FILE = 80;
    public static final int REQUEST_CHANGE_EXT = 81;
    public static final int REQUEST_PASTE_OPTION_MOVE = 82;
    public static final int REQUEST_PASTE_OPTION_COPY = 83;

    public static final int LIST_DISPLAY_NORMAL = 300;
    public static final int LIST_DISPLAY_VAULT = 301;

    // menu ids - other menu option
    public static int MID_SEPARATOR = 0;
    public static int MID_NEW_FILE = 1;
    public static int MID_SEARCH = 2;
    public static int MID_LOGS = 3;
    public static int MID_REFRESH = 4;
    public static int MID_LISTING = 5;
    public static int MID_MULTI_SELECT = 6;
    public static int MID_FIND_EMPTY = 7;
    public static int MID_VIEW_DOT_HIDDEN = 8;
    public static int MID_EXIT = 99;

    // menu ids - single select option
    public static int MSID_DETAILS = 100;
    public static int MSID_COPY = 101;
    public static int MSID_MOVE = 102;
    public static int MSID_RENAME = 103;
    public static int MSID_DELETE = 104;
    public static int MSID_SEQ_RENAME = 105;
    public static int MSID_BATCH_EXT = 106;

    // mime types menu
    public static int MMID_TEXT = 200;
    public static int MMID_IMAGE = 201;
    public static int MMID_AUDIO = 202;
    public static int MMID_VIDEO = 203;
    public static int MMID_ANY = 299;

    public static MainActivity Self = null;

    private FileTabItem currTab;
    private Tools.Sorting currSorting;
    private Stack<Parcelable> scrollStates;
    private boolean toExit = false;
    private int listDisplay = LIST_DISPLAY_NORMAL;

    private FileListAdapter adapter;
    private ImageView btnMenuNewFolder, btnMenuTools, btnMenuSort, btnMenuTabs, btnMenuOthers;
    private ImageView btnSelMenuCopy, btnSelMenuMove, btnSelMenuSelectAll, btnSelMenuUnselectAll;
    private ImageView btnDestMenuNewFolder, btnDestMenuPaste, btnDestMenuCancel;
    private TextView txtFolderInfo;

    public File currFolder;
    public final List<FileListItem> selFiles = new ArrayList<>();
    public List<FileListItem> fileItems;
    public boolean IsSelectMode = false;
    public boolean IsDestMode = false;

    public TextView txtRoot;
    public ListView lvwContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        Self = this;

        SanctionBase.Initialize();
        SanctionBase.LogNormal(this, "Reading app settings...");
        boolean ret = SanctionBase.SettingsLoadValues();
        if (ret) SanctionBase.LogInformation(this, "Done");
        else SanctionBase.LogError(this, "Failed");
        SanctionBase.LogNormal(this, "Application started");

        txtRoot = findViewById(R.id.txtRoot);
        txtRoot.setTypeface(Tools.DefaultFont(this, false));
        txtRoot.setOnClickListener(this);
        txtRoot.bringToFront();
        txtFolderInfo = findViewById(R.id.txtFolderInfo);
        txtFolderInfo.setTypeface(Tools.DefaultFont(this, false));
        ((TextView) findViewById(R.id.txtRootSize)).setTypeface(Tools.DefaultFont(this, false));
        btnMenuNewFolder = findViewById(R.id.btnMenuNewFolder);
        btnMenuNewFolder.setOnClickListener(this);
        btnMenuTools = findViewById(R.id.btnMenuTools);
        btnMenuTools.setOnClickListener(this);
        btnMenuSort = findViewById(R.id.btnMenuSort);
        btnMenuSort.setOnClickListener(this);
        btnMenuTabs = findViewById(R.id.btnMenuTabs);
        btnMenuTabs.setOnClickListener(this);
        btnSelMenuCopy = findViewById(R.id.btnSelMenuCopy);
        btnSelMenuCopy.setOnClickListener(this);
        btnSelMenuMove = findViewById(R.id.btnSelMenuMove);
        btnSelMenuMove.setOnClickListener(this);
        btnSelMenuSelectAll = findViewById(R.id.btnSelMenuSelectAll);
        btnSelMenuSelectAll.setOnClickListener(this);
        btnSelMenuUnselectAll = findViewById(R.id.btnSelMenuUnselectAll);
        btnSelMenuUnselectAll.setOnClickListener(this);
        btnDestMenuNewFolder = findViewById(R.id.btnDestMenuNewFolder);
        btnDestMenuNewFolder.setOnClickListener(this);
        btnDestMenuPaste = findViewById(R.id.btnDestMenuPaste);
        btnDestMenuPaste.setOnClickListener(this);
        btnDestMenuCancel = findViewById(R.id.btnDestMenuCancel);
        btnDestMenuCancel.setOnClickListener(this);
        btnMenuOthers = findViewById(R.id.btnMenuOthers);
        btnMenuOthers.setOnClickListener(this);
        lvwContent = findViewById(R.id.lvwContent);
        lvwContent.setOnItemClickListener((parent, view, position, id) -> {
            FileListItem sel = fileItems.get(position);

            if (IsSelectMode) {
                sel.IsSelected = !sel.IsSelected;
                adapter.notifyDataSetChanged();
                enableSelectAction();
                setSelectionText();
                return;
            }

            if (!IsDestMode) {
                selFiles.clear();
                selFiles.add(sel);
            } else if (sel.ItemType != FileListItem.ListItemType.FOLDER) {
                return;
            }

            if (sel.ItemType == FileListItem.ListItemType.FOLDER) {
                if (IsDestMode && isPathSelected(sel.ID))
                    return;
                File folder = new File(sel.ID);
                if (!folder.exists()) {
                    refreshList();
                    Tools.Toast(MainActivity.this, "Folder no longer exists!", true, true);
                    SanctionBase.LogError(MainActivity.this, "Attempt to open non-existent folder: " + folder.getAbsolutePath());
                    return;
                }
                scrollStates.push(lvwContent.onSaveInstanceState());
                loadContent(folder);
                addPathTree(currFolder);
            } else if (sel.ItemType == FileListItem.ListItemType.FILE) {
                File file = new File(sel.ID);
                if (!file.exists()) {
                    refreshList();
                    Tools.Toast(MainActivity.this, "File no longer exists!", true, true);
                    SanctionBase.LogError(MainActivity.this, "Attempt to open non-existent file: " + file.getAbsolutePath());
                    return;
                }
                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                String mimeType = myMime.getMimeTypeFromExtension(Tools.GetFileExt(sel.ID));
                if (mimeType == null || mimeType.equals("")) {
                    showMenuDialog("Open As", createMimeTypeOptions(), MenuDialog.MODE_SINGLE);
                } else
                    launchFileViewer(file, mimeType);
            }
        });
        lvwContent.setOnItemLongClickListener((parent, view, position, id) -> {
            if (IsSelectMode || IsDestMode) return false;
            FileListItem sel = fileItems.get(position);
            sel.IsSelected = true;
            initSelectMode();
            return true;
        });

        currSorting = new Tools.Sorting();
        List<FileTabItem> fileTabs = new ArrayList<>();
        scrollStates = new Stack<>();
        fileItems = new ArrayList<>();
        adapter = new FileListAdapter(this, R.layout.file_list_row);
        lvwContent.setAdapter(adapter);

        // default first tab
        FileTabItem tab = new FileTabItem();
        tab.root = Environment.getExternalStorageDirectory();

        fileTabs.add(tab);
        currTab = fileTabs.get(0);
        ((TextView) findViewById(R.id.txtRoot)).setText(currTab.root.getName());
        loadContent(currTab.root);
    }

    @Override
    public void onBackPressed() {
        if (IsSelectMode) {
            endSelectMode();
            return;
        }

        if (IsDestMode &&
                currFolder.getAbsolutePath().equalsIgnoreCase(currTab.root.getAbsolutePath())) {
            endDestMode();
            return;
        }

        File parent = currFolder.getParentFile();
        if (parent != null &&
                !currFolder.getAbsolutePath().equalsIgnoreCase(currTab.root.getAbsolutePath())) {
            loadContent(parent);
            if (!scrollStates.empty())
                lvwContent.onRestoreInstanceState(scrollStates.pop());
            removePathTree(1);
            return;
        }

        if (toExit) {
            super.onBackPressed();
            return;
        }

        toExit = true;
        Tools.Toast(this, "Press Back again to exit.", false, false);
        new Handler(Looper.myLooper()).postDelayed(() -> toExit = false, 2000);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(btnMenuNewFolder) || v.equals(btnDestMenuNewFolder)) {
            requestTextInput(REQUEST_NEW_FOLDER, "New Folder", "", "new folder name", -1, -1);
        } else if (v.equals(btnMenuTools)) {
            showMenuDialog("Settings", createSettingsMenu(), MenuDialog.MODE_SINGLE);
        } else if (v.equals(btnMenuSort)) {
            Intent intent = new Intent(this, SortDialog.class);
            intent.putExtra(SortDialog.PARAM_SORT_TYPE, currSorting.SortType);
            intent.putExtra(SortDialog.PARAM_SORT_GROUP, currSorting.SortGrouping);
            intent.putExtra(SortDialog.PARAM_REVERSE, currSorting.IsReverse);
            startActivityForResult(intent, REQUEST_SORT);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        } else if (v.equals(btnMenuOthers)) {
            if (IsSelectMode) {
                showMenuDialog("Options", createMenuSelectOptions(getSelCount() == 1), MenuDialog.MODE_GENERAL);
            } else {
                showMenuDialog("Options", createMenuMainOptions(), MenuDialog.MODE_GENERAL);
            }
        } else if (v.equals(txtRoot)) {
            if (IsSelectMode) {
                return;
            }
            if (currFolder.getAbsolutePath().equalsIgnoreCase(currTab.root.getAbsolutePath())) {
                return;
            }
            loadContent(currTab.root);
            LinearLayout parent = (LinearLayout) findViewById(R.id.lnrPathTree);
            parent.removeAllViews();
            parent.requestLayout();
            scrollStates.clear();
        } else if (v.equals(btnSelMenuCopy)) {
            initDestMode(true);
        } else if (v.equals(btnSelMenuMove)) {
            initDestMode(false);
        } else if (v.equals(btnSelMenuSelectAll)) {
            for (FileListItem f : fileItems) {
                f.IsSelected = true;
            }
            adapter.notifyDataSetChanged();
            enableSelectAction();
            setSelectionText();
        } else if (v.equals(btnSelMenuUnselectAll)) {
            for (FileListItem f : fileItems) f.IsSelected = false;
            adapter.notifyDataSetChanged();
            enableSelectAction();
            setSelectionText();
        } else if (v.equals(btnDestMenuCancel)) {
            endDestMode();
        } else if (v.equals(btnDestMenuPaste)) {
            boolean isCopy = ((Boolean) btnDestMenuPaste.getTag());
            SanctionBase.LogNormal(this, "Running operation: " + (isCopy ? "copy" : "move"));
            FileListItem s = selFiles.get(0);
            File f = new File(s.ID);
            String oldPath = f.getAbsolutePath();
            oldPath = oldPath.substring(0, oldPath.lastIndexOf("/") + 1);
            String newPath = currFolder.getAbsolutePath() + "/";
            if (oldPath.equalsIgnoreCase(newPath) && !isCopy) {
                Tools.Toast(this, "Can't paste to same location!", false, true);
                return;
            }

            SanctionBase.LogNormal(this, "Pasting files to: " + currFolder.getAbsolutePath());
            if (isCopy) {
                FileCopy.run(selFiles, currFolder);
            } else {
                FileMove.run(selFiles, currFolder);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_NEW_FOLDER) {
            String folderName = data.getStringExtra(TextInputDialog.RETURN_INPUT);
            File newDir = new File(currFolder, folderName);
            if (newDir.mkdir()) {
                SanctionBase.LogNormal(this, "Dir created: " + newDir.getAbsolutePath());
                if (IsDestMode) {
                    scrollStates.push(lvwContent.onSaveInstanceState());
                    loadContent(newDir);
                    addPathTree(currFolder);
                } else refreshList();
            } else {
                Tools.Toast(this, "Failed to create folder: " + newDir.getAbsolutePath(), true, true);
                SanctionBase.LogError(this, "Failed to create dir: " + newDir.getAbsolutePath());
            }
        } else if (requestCode == REQUEST_NEW_FILE) {
            String filename = data.getStringExtra(TextInputDialog.RETURN_INPUT);
            File newFile = new File(currFolder, filename);
            try {
                if (newFile.createNewFile()) {
                    SanctionBase.LogNormal(this, "File created: " + newFile.getAbsolutePath());
                    refreshList();
                } else {
                    Tools.Toast(this, "Failed to create file: " + newFile.getAbsolutePath(), true, true);
                    SanctionBase.LogError(this, "Failed to create file: " + newFile.getAbsolutePath());
                }
            } catch (IOException ex) {
                Tools.Toast(this, "Failed to create file: " + newFile.getAbsolutePath(), true, true);
                SanctionBase.LogError(this, "Failed to create file: " + newFile.getAbsolutePath());
                SanctionBase.LogError(this, "Exception: " + ex.toString());
            }

        } else if (requestCode == REQUEST_RENAME) {
            File f = new File(selFiles.get(0).ID);
            String oldPath = f.getAbsolutePath();
            String oldName = oldPath.substring(oldPath.lastIndexOf("/") + 1);
            String newName = data.getStringExtra(TextInputDialog.RETURN_INPUT);
            if (oldName.equals(newName)) {
                Tools.Toast(this, "No change in name", false, true);
                endSelectMode();
                return;
            }
            String newPath = oldPath.substring(0, oldPath.lastIndexOf("/") + 1);
            newPath += newName;
            if (Tools.RenameFile(f, new File(newPath))) {
                refreshList();
            } else {
                Tools.Toast(MainActivity.Self, "Failed to rename", false, true);
            }
            endSelectMode();
        } else if (requestCode == REQUEST_EXPORT_LIST_NAME) {
            ExportListing.run(data);
        } else if (requestCode == REQUEST_TOOL_CODE) {

        } else if (requestCode == REQUEST_SORT) {
            currSorting.SortType = data.getIntExtra(SortDialog.PARAM_SORT_TYPE, Tools.Sorting.TYPE_FILENAME);
            currSorting.SortGrouping = data.getIntExtra(SortDialog.PARAM_SORT_GROUP, Tools.Sorting.GROUPING_FOLDER_FIRST);
            currSorting.IsReverse = data.getBooleanExtra(SortDialog.PARAM_REVERSE, false);
            refreshList();
            lvwContent.setSelection(0);
        } else if (requestCode == REQUEST_CHANGE_EXT) {
            listSelections();
            ChangeExt.run(data);
            refreshList();
            endSelectMode();
        } else if (requestCode == REQUEST_MENU) {
            int selMenu = data.getIntExtra(MenuDialog.RETURN_MENU_ID, 0);
            runMenu(selMenu);
        } else if (requestCode == REQUEST_CONFIRM_DELETE) {
            endSelectMode();
            FileDelete.run();
        } else if (requestCode == REQUEST_PASTE_OPTION_MOVE) {
            FileMove.handleResult(resultCode, data);
        } else if (requestCode == REQUEST_PASTE_OPTION_COPY) {
            FileCopy.handleResult(resultCode, data);
        }
    }

    private void runMenu(int selMenu) {
        // general menu
        if (selMenu == MID_LOGS) {
            Intent intent = new Intent(this, LogsDialog.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        } else if (selMenu == MID_REFRESH) {
            refreshList();
        } else if (selMenu == MID_EXIT) {
            this.finish();
        } else if (selMenu == MID_LISTING) {
            String fName = "(" + currFolder.getName() + ")_tree_list.txt";
            int dot = fName.lastIndexOf(".");
            requestTextInput(REQUEST_EXPORT_LIST_NAME,
                    "Exported Filename", fName, "exported filename", 0, dot);
        } else if (selMenu == MID_MULTI_SELECT) {
            initSelectMode();
        } else if (selMenu == MID_NEW_FILE) {
            requestTextInput(REQUEST_NEW_FILE, "New File", "", "new filename", -1, -1);
        } else if (selMenu == MID_VIEW_DOT_HIDDEN) {
            SanctionBase.IsHiddenDotShown = !SanctionBase.IsHiddenDotShown;
            SanctionBase.SettingsWriteValues(false);
            refreshList();
        }
        // single-select menu
        else if (selMenu == MSID_DELETE) {
            listSelections();
            String msg = "";
            if (selFiles.size() == 1) {
                msg = "Delete ";
                FileListItem.ListItemType type = selFiles.get(0).ItemType;
                if (type == FileListItem.ListItemType.FOLDER) {
                    msg += "folder '";
                } else if (type == FileListItem.ListItemType.FILE) {
                    msg += "file '";
                }
                msg += selFiles.get(0).Title + "'?";
            } else {
                msg = "Delete " + String.valueOf(selFiles.size()) + " items?";
            }
            confirmAction(REQUEST_CONFIRM_DELETE, "Confirm Delete", msg);
        } else if (selMenu == MSID_RENAME) {
            listSelections();
            String path = selFiles.get(0).ID;
            String fName = path.substring(path.lastIndexOf("/") + 1);

            boolean isHidden = fName.startsWith(".");
            String f = fName;
            if (isHidden) f = f.substring(1);
            int dot = f.lastIndexOf(".");
            if (dot == -1) dot = fName.length();
            else if (isHidden) dot++;

            requestTextInput(REQUEST_RENAME, "File Rename", fName, "new filename", (isHidden ? 1 : 0), dot);
        }
        // multi-select menu
        else if (selMenu == MSID_BATCH_EXT) {
            requestTextInput(REQUEST_CHANGE_EXT, "Change Extension", "", "new extension", -1, -1);
        }
        // mime types menu
        else if (selMenu == MMID_TEXT) {
            launchFileViewer(new File(selFiles.get(0).ID), "text/*");
        } else if (selMenu == MMID_IMAGE) {
            launchFileViewer(new File(selFiles.get(0).ID), "image/*");
        } else if (selMenu == MMID_AUDIO) {
            launchFileViewer(new File(selFiles.get(0).ID), "audio/*");
        } else if (selMenu == MMID_VIDEO) {
            launchFileViewer(new File(selFiles.get(0).ID), "video/*");
        } else if (selMenu == MMID_ANY) {
            launchFileViewer(new File(selFiles.get(0).ID), "*/*");
        }
    }

    private void launchFileViewer(File file, String mime) {
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        newIntent.setDataAndType(FileProvider.getUriForFile(this, this.getPackageName() + ".provider", file), mime);
        try {
            MainActivity.this.startActivity(Intent.createChooser(newIntent, "Open with"));
        } catch (ActivityNotFoundException e) {
            newIntent.setDataAndType(Uri.fromFile(file), "*/*");
            try {
                MainActivity.this.startActivity(newIntent);
            } catch (ActivityNotFoundException ex) {
                Tools.Toast(MainActivity.this, "Error while loading file!", true, true);
                SanctionBase.LogError(this, "Cannot open file: " + file.getAbsolutePath() + ", Error: " + ex.toString());
            }
        }
    }

    public void refreshList() {
        scrollStates.push(lvwContent.onSaveInstanceState());
        loadContent(currFolder);
        lvwContent.onRestoreInstanceState(scrollStates.pop());
    }

    private void requestTextInput(int reqCode, String title, String defVal, String hint, int selStart, int selEnd) {
        Intent intent = new Intent(this, TextInputDialog.class);
        intent.putExtra(TextInputDialog.PARAM_TITLE, title);
        intent.putExtra(TextInputDialog.PARAM_DEFAULT_VALUE, defVal);
        intent.putExtra(TextInputDialog.PARAM_HINT, hint);
        intent.putExtra(TextInputDialog.PARAM_SEL_START, selStart);
        intent.putExtra(TextInputDialog.PARAM_SEL_END, selEnd);
        startActivityForResult(intent, reqCode);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    private void confirmAction(int reqCode, String title, String msg) {
        Intent intent = new Intent(this, ConfirmYesNoDialog.class);
        intent.putExtra(ConfirmYesNoDialog.PARAM_TITLE, title);
        intent.putExtra(ConfirmYesNoDialog.PARAM_MESSAGE, msg);
        startActivityForResult(intent, reqCode);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    private void loadContent(File root) {
        currFolder = root;
        long total = currFolder.getTotalSpace();
        long used = total - currFolder.getFreeSpace();
        long barMax = Resources.getSystem().getDisplayMetrics().widthPixels;
        int barSize = (int) ((used * barMax) / total);
        View v = findViewById(R.id.viewRootSizeFill);
        v.getLayoutParams().width = barSize;
        String s = Tools.ToDataUnitString(used) + " / " + Tools.ToDataUnitString(total);
        ((TextView) findViewById(R.id.txtRootSize)).setText(s);

        fileItems.clear();
        int cFolder = 0, cFile = 0;
        File[] files = root.listFiles();
        if (files == null) files = new File[]{};
        Tools.SortFileList(files, currSorting.SortType, currSorting.SortGrouping, currSorting.IsReverse);
        for (File f : files) {
            FileListItem item = new FileListItem();
            item.Title = f.getName();
            if (item.Title.startsWith(".") && !SanctionBase.IsHiddenDotShown) {
                continue;
            }
            item.GroupID = MimeManager.TYPE_UNKNOWN;
            item.SubTitle1 = "(...)";
            item.SubTitle2 = "(...)";
            if (f.isDirectory()) {
                cFolder++;
                item.ItemType = FileListItem.ListItemType.FOLDER;
                File[] subs = f.listFiles();
                if (subs == null) {
                    subs = new File[]{};
                }
                item.SubTitle1 = NumberFormat.getIntegerInstance().format(subs.length) + " items";
            } else if (f.isFile()) {
                cFile++;
                item.ItemType = FileListItem.ListItemType.FILE;
                item.SubTitle1 = Tools.ToDataUnitString(f.length());
                String n = f.getName().toLowerCase();
                item.GroupID = MimeManager.GetTypeFromFilename(n);
            } else {
                item.ItemType = FileListItem.ListItemType.UNKNOWN;
                item.SubTitle1 = "[Unknown]";
            }
            long modDate = f.lastModified();
            item.SubTitle2 = new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm:ss a").format(new Date(modDate));
            item.ID = f.getAbsolutePath();

            fileItems.add(item);
        }

        adapter.notifyDataSetChanged();
        if (fileItems.size() > 0) lvwContent.setSelection(0);
        ((TextView) findViewById(R.id.txtFolderInfo)).setText("Folders: " + cFolder + ", Files: " + cFile);
    }

    private void addPathTree(File file) {
        LinearLayout parent = (LinearLayout) findViewById(R.id.lnrPathTree);
        Button btn = new Button(this);
        btn.setTag(file);
        btn.setText(file.getName());
        btn.setTextColor(Color.argb(255, 255, 255, 255));
        btn.setCompoundDrawablesWithIntrinsicBounds(
                R.mipmap.ic_right, 0, 0, 0);
        btn.setBackgroundResource(R.drawable.selector_back_path);
        btn.setMinimumWidth(0);
        btn.setMinWidth(0);
        btn.setClickable(true);
        btn.setAllCaps(false);
        btn.setTextSize(Tools.DpsToPixel(this, 4f));
        btn.setTypeface(Tools.DefaultFont(this, false));
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, Tools.DpsToPixel(this, 22f)));
        btn.setOnClickListener(v -> {
            if (IsSelectMode) return;
            LinearLayout parent1 = (LinearLayout) v.getParent();
            int ind = parent1.indexOfChild(v);
            int total = parent1.getChildCount();
            if (ind == total - 1) return; // last folder was already selected
            loadContent((File) v.getTag());
            for (int i = 0; i < (total - ind - 1); ++i) {
                removePathTree(1);
                if (!scrollStates.empty())
                    lvwContent.onRestoreInstanceState(scrollStates.pop());
            }
        });
        parent.addView(btn);
    }

    private void removePathTree(int count) {
        LinearLayout parent = findViewById(R.id.lnrPathTree);
        int c = parent.getChildCount();
        if (c < count) return;
        for (int i = 0; i < count; ++i) {
            parent.removeViewAt(c - 1); // remove last
            c = parent.getChildCount();
        }
        parent.requestLayout();
    }

    private List<MenuItem> createMenuMainOptions() {
        List<MenuItem> ret = new ArrayList<>();
        ret.add(new MenuItem(MID_NEW_FILE, "New File", R.mipmap.ic_file_new, false));
        ret.add(new MenuItem(MID_SEARCH, "Search", R.mipmap.ic_search, false));
        ret.add(new MenuItem(MID_REFRESH, "Refresh", R.mipmap.ic_refresh, false));
        //ret.add(new MenuItem(MID_MULTI_SELECT, "Multi-Select", R.drawable.ic_select_all, false));
        ret.add(new MenuItem(MID_EXIT, "Exit", R.mipmap.ic_backspace, false));
        ret.add(new MenuItem(MID_LOGS, "Logs", R.mipmap.ic_assignment, true));
        ret.add(new MenuItem(MID_LISTING, "Export List", R.mipmap.ic_open_in_new, true));
        ret.add(new MenuItem(MID_FIND_EMPTY, "Find Empty Folders", R.mipmap.ic_folder_open_, true));
        return ret;
    }

    private List<MenuItem> createMimeTypeOptions() {
        List<MenuItem> ret = new ArrayList<>();
        ret.add(new MenuItem(MMID_TEXT, "Text", R.mipmap.ic_mime_text, true));
        ret.add(new MenuItem(MMID_IMAGE, "Image", R.mipmap.ic_mime_image, true));
        ret.add(new MenuItem(MMID_AUDIO, "Audio", R.mipmap.ic_mime_audio, true));
        ret.add(new MenuItem(MMID_VIDEO, "Video", R.mipmap.ic_mime_video, true));
        ret.add(new MenuItem(MMID_ANY, "Unknown", R.mipmap.ic_mime_any, false));
        return ret;
    }

    private List<MenuItem> createSettingsMenu() {
        List<MenuItem> ret = new ArrayList<>();
        ret.add(new MenuItem(MID_VIEW_DOT_HIDDEN, "Show \" . \" hidden files",
                (SanctionBase.IsHiddenDotShown ? R.mipmap.ic_selected : R.mipmap.ic_unselected),
                true));
        return ret;
    }

    private List<MenuItem> createMenuSelectOptions(boolean isSingle) {
        List<MenuItem> ret = new ArrayList<>();
        ret.add(new MenuItem(MSID_DETAILS, "View Details", R.mipmap.ic_dropdown, false));
        ret.add(new MenuItem(MSID_DELETE, "Delete", R.mipmap.ic_delete, false));

        if (isSingle) {
            ret.add(new MenuItem(MSID_RENAME, "Rename", R.mipmap.ic_create, false));
        } else {
            ret.add(new MenuItem(MSID_SEQ_RENAME, "Sequential Rename", R.mipmap.ic_filter_list, true));
        }

        if (!isSingle && isSelFilesOnly()) {
            ret.add(new MenuItem(MSID_BATCH_EXT, "Change Extension", R.mipmap.ic_restore_page, true));
        }
        return ret;
    }

    private void showMenuDialog(String title, List<MenuItem> menus, int mode) {
        Intent intent = new Intent(this, MenuDialog.class);
        intent.putExtra(MenuDialog.PARAM_TITLE, title);
        intent.putExtra(MenuDialog.PARAM_IDS, MenuItem.toListID(menus));
        intent.putExtra(MenuDialog.PARAM_TITLES, MenuItem.toListNames(menus));
        intent.putExtra(MenuDialog.PARAM_ICONS, MenuItem.toListIcons(menus));
        intent.putExtra(MenuDialog.PARAM_IS_DEVS, MenuItem.toListIsDevs(menus));
        intent.putExtra(MenuDialog.PARAM_MODE, mode);
        startActivityForResult(intent, REQUEST_MENU);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    private void initDestMode(boolean isCopy) {
        listSelections();
        endSelectMode();

        btnMenuNewFolder.setVisibility(View.GONE);
        btnMenuSort.setVisibility(View.INVISIBLE);
        btnMenuTools.setVisibility(View.GONE);
        btnMenuTabs.setVisibility(View.INVISIBLE);
        btnSelMenuCopy.setVisibility(View.GONE);
        btnSelMenuMove.setVisibility(View.GONE);
        btnSelMenuSelectAll.setVisibility(View.GONE);
        btnSelMenuUnselectAll.setVisibility(View.GONE);
        btnMenuOthers.setVisibility(View.GONE);

        btnDestMenuNewFolder.setVisibility(View.VISIBLE);
        btnDestMenuPaste.setVisibility(View.VISIBLE);
        btnDestMenuPaste.setTag(isCopy);
        btnDestMenuCancel.setVisibility(View.VISIBLE);

        LinearLayout lnr = findViewById(R.id.lnrMenu);
        lnr.setBackgroundColor(Color.argb(0xDD, 100, 100, 100));
        IsDestMode = true;
        adapter.notifyDataSetChanged();
    }

    public void endDestMode() {
        btnMenuNewFolder.setVisibility(View.VISIBLE);
        btnMenuSort.setVisibility(View.VISIBLE);
        btnMenuTools.setVisibility(View.VISIBLE);
        btnMenuTabs.setVisibility(View.VISIBLE);
        btnMenuOthers.setVisibility(View.VISIBLE);

        btnDestMenuNewFolder.setVisibility(View.GONE);
        btnDestMenuPaste.setVisibility(View.GONE);
        btnDestMenuCancel.setVisibility(View.GONE);

        LinearLayout lnr = findViewById(R.id.lnrMenu);
        lnr.setBackgroundColor(Color.argb(0xDD, 0x11, 0x11, 0x11));
        IsDestMode = false;
        adapter.notifyDataSetChanged();
    }

    private void initSelectMode() {
        btnMenuNewFolder.setVisibility(View.GONE);
        btnMenuSort.setVisibility(View.GONE);
        btnMenuTools.setVisibility(View.GONE);
        btnMenuTabs.setVisibility(View.GONE);

        btnSelMenuCopy.setVisibility(View.VISIBLE);
        btnSelMenuMove.setVisibility(View.VISIBLE);
        btnSelMenuSelectAll.setVisibility(View.VISIBLE);
        btnSelMenuUnselectAll.setVisibility(View.VISIBLE);

        txtFolderInfo.setTag(txtFolderInfo.getText().toString());
        setSelectionText();

        HorizontalScrollView parent = findViewById(R.id.hScrollPathTree);
        parent.setBackgroundColor(Color.rgb(100, 100, 100));
        LinearLayout lnr = (LinearLayout) findViewById(R.id.lnrMenu);
        lnr.setBackgroundColor(Color.argb(0xDD, 100, 100, 100));
        enableSelectAction();
        IsSelectMode = true;
        adapter.notifyDataSetChanged();
        SanctionBase.LogNormal(this, "Multi-select enabled.");
    }

    private void endSelectMode() {
        btnMenuNewFolder.setVisibility(View.VISIBLE);
        btnMenuSort.setVisibility(View.VISIBLE);
        btnMenuTools.setVisibility(View.VISIBLE);
        btnMenuTabs.setVisibility(View.VISIBLE);

        btnSelMenuCopy.setVisibility(View.GONE);
        btnSelMenuMove.setVisibility(View.GONE);
        btnSelMenuSelectAll.setVisibility(View.GONE);
        btnSelMenuUnselectAll.setVisibility(View.GONE);

        txtFolderInfo.setText(String.valueOf(txtFolderInfo.getTag()));
        txtFolderInfo.setTag(null);

        btnMenuOthers.setEnabled(true);
        HorizontalScrollView parent = findViewById(R.id.hScrollPathTree);
        parent.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout lnr = findViewById(R.id.lnrMenu);
        lnr.setBackgroundColor(Color.argb(0xDD, 0x11, 0x11, 0x11));
        IsSelectMode = false;
        for (FileListItem f : fileItems) f.IsSelected = false;
        adapter.notifyDataSetChanged();
        SanctionBase.LogNormal(this, "Multi-select disabled");
    }

    private void enableSelectAction() {
        btnSelMenuCopy.setEnabled(false);
        btnSelMenuMove.setEnabled(false);
        btnMenuOthers.setEnabled(false);
        for (FileListItem f : fileItems) {
            if (f.IsSelected) {
                btnSelMenuCopy.setEnabled(true);
                btnSelMenuMove.setEnabled(true);
                btnMenuOthers.setEnabled(true);
                break;
            }
        }
    }

    private int getSelCount() {
        int sel = 0;
        for (FileListItem f : fileItems)
            if (f.IsSelected) sel++;
        return sel;
    }

    private void listSelections() {
        selFiles.clear();
        for (FileListItem f : fileItems)
            if (f.IsSelected) selFiles.add(f);
    }

    private boolean isSelFilesOnly() {
        if (getSelCount() <= 0) return false;
        for (FileListItem f : fileItems)
            if (f.IsSelected && f.ItemType == FileListItem.ListItemType.FOLDER)
                return false;
        return true;
    }

    private void setSelectionText() {
        txtFolderInfo.setText("Selected: " + getSelCount() + " of " + fileItems.size());
    }

    public boolean isPathSelected(String path) {
        for (FileListItem f : selFiles) {
            if (f.ID.toLowerCase().equals(path.toLowerCase()))
                return true;
        }
        return false;
    }
}
