package com.x.manager.customView;

import android.view.*;

import android.graphics.*;
import android.content.*;
import android.util.*;

// source: https://stackoverflow.com/questions/6533942/adding-gif-image-in-an-imageview-in-android
public class GIFView extends View {
    private Movie gifMovie;
    private int movieWidth, movieHeight;
    private long movieDuration;
    private long mMovieStart;

    public GIFView(Context context) {
        super(context);
        init(context);
    }

    public GIFView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GIFView(Context context, AttributeSet attrs,
                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setFocusable(true);
    }

    public void LoadImage(String filename) {
        gifMovie = null;
        gifMovie = Movie.decodeFile(filename);
        movieWidth = gifMovie.width();
        movieHeight = gifMovie.height();
        movieDuration = gifMovie.duration();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        setMeasuredDimension(movieWidth, movieHeight);
    }

    public int getMovieWidth() {
        return movieWidth;
    }

    public int getMovieHeight() {
        return movieHeight;
    }

    public long getMovieDuration() {
        return movieDuration;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long now = android.os.SystemClock.uptimeMillis();

        if (mMovieStart == 0) {   // first time
            mMovieStart = now;
        }

        if (gifMovie != null) {
            int dur = gifMovie.duration();
            if (dur == 0) {
                dur = 1000;
            }
            int relTime = (int) ((now - mMovieStart) % dur);
            gifMovie.setTime(relTime);
            gifMovie.draw(canvas, 0, 0);
            invalidate();
        }
    }
}
