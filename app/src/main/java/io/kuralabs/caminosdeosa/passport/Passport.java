package io.kuralabs.caminosdeosa.passport;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import io.kuralabs.caminosdeosa.passport.flip.BookManager;

public class Passport implements BookManager {

    Context context;
    Bitmap background;

    public Passport(Context context) {
        this.context = context;
        this.background = BitmapFactory.decodeResource(context.getResources(), R.drawable.p1_480);
    }

    @Override
    public Bitmap getPage(int pageNo) {
        return this.background;
    }

    @Override
    public boolean canFlipForward() {
        return true;
    }

    @Override
    public boolean canFlipBackward() {
        return false;
    }
}
