package com.x.manager.utility;

import com.x.manager.R;

public class MimeManager {
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_DOC = 3;
    public static final int TYPE_IMAGE = 4;
    public static final int TYPE_CODE = 5;

    public static int GetIconResourceFromType(int type) {
        if (type == TYPE_AUDIO)
            return R.mipmap.ic_file_audio;
        else if (type == TYPE_VIDEO)
            return R.mipmap.ic_file_video;
        else if (type == TYPE_DOC)
            return R.mipmap.ic_file_doc;
        else if (type == TYPE_IMAGE)
            return R.mipmap.ic_file_image;
        else if (type == TYPE_CODE)
            return R.mipmap.ic_file_code;
        else
            return R.mipmap.ic_file;
    }

    public static int GetTypeFromFilename(String filename) {
        filename = filename.toLowerCase();
        if (filename.endsWith(".mp3"))
            return TYPE_AUDIO;
        else if (filename.endsWith(".mp4") ||
                filename.endsWith(".avi"))
            return TYPE_VIDEO;
        else if (filename.endsWith(".txt"))
            return TYPE_DOC;
        else if (filename.endsWith(".png") ||
                filename.endsWith(".jpg") ||
                filename.endsWith(".jpeg") ||
                filename.endsWith(".gif"))
            return TYPE_IMAGE;
        else if (filename.endsWith(".cs") ||
                filename.endsWith(".java") ||
                filename.endsWith(".c"))
            return TYPE_CODE;
        else return TYPE_UNKNOWN;
    }
}
