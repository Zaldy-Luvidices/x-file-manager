package com.x.manager.model;

public class FileListItem {
    public enum ListItemType {
        FILLER,
        FOLDER,
        FILE,
        UNKNOWN,
    }

    public ListItemType ItemType = ListItemType.UNKNOWN;
    public int FillerHeight = 10; // for filler type only
    public String Title = "Title";
    public String SubTitle1 = "Sub-title1";
    public String SubTitle2 = "Sub-title2";
    public String ID = ""; // internal, must be unique
    public int GroupID = 0; // internal, not neccessarily unique
    public boolean IsSelected = false;
    public int State = 0; // internal, not neccessarily unique
}
