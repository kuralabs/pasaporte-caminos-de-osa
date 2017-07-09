package io.kuralabs.caminosdeosa.passport.flip;

import android.graphics.Bitmap;

import com.eschao.android.widget.pageflip.OnPageFlipListener;

import java.util.concurrent.locks.ReentrantLock;

public interface BookManager extends OnPageFlipListener {
    ReentrantLock getLock();

    Bitmap getPage(int pageNo);
    BookManager setPage(int pageNo, Bitmap page);
    Bitmap createPage();

    BookManager setPageNo(int pageNo);
    int getPageNo();
}
