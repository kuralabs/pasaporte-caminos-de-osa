package io.kuralabs.caminosdeosa.passport;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

import android.graphics.Color;
import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Message;
import android.util.Log;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import io.kuralabs.caminosdeosa.passport.flip.BookManager;

public class Passport implements BookManager {

    int pageNo;
    ReentrantLock pagesLock;

    Context context;
    ArrayList<Handler> listeners;
    ArrayList<Bitmap> pages;

    public Passport(Context context) {
        pageNo = 0;
        pagesLock = new ReentrantLock();

        this.context = context;
        this.listeners = new ArrayList<>();
        this.pages = new ArrayList<>();

        // Create pages
        // Add cover
        pages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.cover));
        // Add personal data
        pages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.profile));
        // Add manifesto
        pages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.manifesto));
        // Add manifesto second page
        pages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.manifesto2));
        // Empty page
        pages.add(createPage());
    }

    @Override
    public boolean canFlipForward() {
        return pageNo < (pages.size() - 1);
    }

    @Override
    public boolean canFlipBackward() {
        return pageNo > 0;
    }

    @Override
    public ReentrantLock getLock() {
        return pagesLock;
    }

    /**
     *
     * IMPORTANT: You must grab the lock before trying to fetch a page.
     * @param pageNo
     * @return
     */
    @Override
    public Bitmap getPage(int pageNo) {
        if (pageNo < 0 || pageNo >= pages.size()) {
            throw new InvalidParameterException("Invalid page number " + Integer.toString(pageNo));
        }
        return pages.get(pageNo);
    }

    /**
     *
     * IMPORTANT: You must grab the lock before trying to set a page.
     *
     * @param pageNo
     * @param page
     * @return
     */
    @Override
    public Passport setPage(int pageNo, Bitmap page) {
        if (pageNo < 0 || pageNo >= pages.size()) {
            throw new InvalidParameterException("Invalid page number " + Integer.toString(pageNo));
        }

        Canvas canvas = new Canvas(pages.get(pageNo));
        canvas.drawBitmap(page, 0, 0, null);
        return this;
    }

    @Override
    public Bitmap createPage() {
        Bitmap basePage = BitmapFactory.decodeResource(
            context.getResources(), R.drawable.background
        );
        Bitmap copy = basePage.copy(Bitmap.Config.ARGB_8888, true);
        basePage.recycle();
        return copy;
    }

    /**
     *
     * IMPORTANT: You must grab the lock before trying to set the page number.
     *
     * @param pageNo
     * @return
     */
    @Override
    public Passport setPageNo(int pageNo) {

        if (pageNo < 0 || pageNo >= pages.size()) {
            throw new InvalidParameterException("Invalid page number " + Integer.toString(pageNo));
        }

        this.pageNo = pageNo;

        // Notify listeners
        for (Handler h : listeners) {
            Message msg = Message.obtain();
            msg.arg1 = pageNo;
            msg.arg2 = pages.size();
            h.sendMessage(msg);
        }
        return this;
    }

    @Override
    public int getPageNo() {
        return pageNo;
    }

    @Override
    public Passport addOnPageChangeListener(Handler listener) {
        this.listeners.add(listener);
        return this;
    }

    protected int calcFontSize(int size) {
        return (int)(size * context.getResources().getDisplayMetrics().scaledDensity);
    }

    @Override
    public void drawStamp(String stamp) {
        Log.d("Passport", "Got stamp: " + stamp);

        // Search for 'stamp' resource
        Bitmap stampBitmap = BitmapFactory.decodeResource(
            context.getResources(),
            context.getResources().getIdentifier(
                stamp, "drawable",  context.getPackageName()
            )
        );

        // Create temporal buffer
        int currentPageNo = pageNo;
        Bitmap buffer = null;
        try {
            buffer = createPage();
            Canvas canvas = new Canvas(buffer);

            // Copy second buffer into third
            try {
                pagesLock.lock();
                canvas.drawBitmap(pages.get(currentPageNo), 0, 0, null);
            } finally {
                pagesLock.unlock();
            }

            // Paint stamp
            canvas.drawBitmap(
                stampBitmap,
                (buffer.getWidth() / 2) - (stampBitmap.getWidth() / 2),
                (buffer.getHeight() / 2) + (stampBitmap.getHeight() / 2),
                null
            );

            // Add timestamp
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = df.format(c.getTime());

            int fontSize = calcFontSize(32);
            Paint p = new Paint();

            p.setFilterBitmap(true);
            p.setColor(Color.WHITE);
            p.setStrokeWidth(1);
            p.setAntiAlias(true);
            p.setShadowLayer(5.0f, 8.0f, 8.0f, Color.BLACK);
            p.setTextSize(fontSize);
            String text = formattedDate;
            float textWidth = p.measureText(text);
            float y = buffer.getHeight() - p.getTextSize() - 100;
            canvas.drawText(text, (buffer.getWidth() - textWidth) / 2, y, p);

            // Copy third buffer into second
            try {
                pagesLock.lock();
                setPage(currentPageNo, buffer);
                pages.add(createPage());
                setPageNo(currentPageNo); // Trigger listeners
            } finally {
                pagesLock.unlock();
            }

        } finally {
            if (buffer != null) {
                buffer.recycle();
            }
        }
    }

    @Override
    public void drawPhoto(Bitmap photo) {
        // FIXME: Draw photo in canvas here
        Log.d("Passport", "Got photo");
        photo.recycle();
    }
}
