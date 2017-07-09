package io.kuralabs.caminosdeosa.passport;

import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import io.kuralabs.caminosdeosa.passport.flip.BookManager;

public class Passport implements BookManager {

    int pageNo;
    ReentrantLock pagesLock;

    Context context;
    ArrayList<Bitmap> pages = new ArrayList<>();

    public Passport(Context context) {
        pageNo = 0;
        pagesLock = new ReentrantLock();

        this.context = context;

        // First page
        pages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.p1_480));
        // FIXME: Do not hardwire two pages
        pages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.p1_480));
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
        if (pageNo < 0 || pageNo > pages.size()) {
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
        if (pageNo < 0 || pageNo > pages.size()) {
            throw new InvalidParameterException("Invalid page number " + Integer.toString(pageNo));
        }

        Canvas canvas = new Canvas(pages.get(pageNo));
        canvas.drawBitmap(page, 0, 0, null);
        // page.recycle();
        return this;
    }

    @Override
    public Bitmap createPage() {
        return Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);
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
        this.pageNo = pageNo;
        int currentSize = pages.size();

        // Create pages on the fly
        if (pageNo > currentSize) {
            for (int i = 0; i < (currentSize - pageNo); i++) {
                pages.add(createPage());
            }
        }
        return this;
    }

    @Override
    public int getPageNo() {
        return this.pageNo;
    }
}
