package io.kuralabs.caminosdeosa.passport.flip;

import android.graphics.Bitmap;

import com.eschao.android.widget.pageflip.OnPageFlipListener;

public interface BookManager extends OnPageFlipListener {
    Bitmap getPage(int pageNo);
}
