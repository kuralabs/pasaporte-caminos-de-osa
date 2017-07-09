package io.kuralabs.caminosdeosa.passport.flip;

import android.os.Handler;
import android.graphics.Bitmap;

import com.eschao.android.widget.pageflip.OnPageFlipListener;

import java.util.concurrent.locks.ReentrantLock;

public interface BookManager extends OnPageFlipListener {
    ReentrantLock getLock();

    Bitmap getPage(int pageNo); // Requires locking
    BookManager setPage(int pageNo, Bitmap page); // Requires locking
    Bitmap createPage(); // Requires cleanup after

    BookManager setPageNo(int pageNo); // Requires locking
    BookManager addOnPageChangeListener(Handler listener);
}
