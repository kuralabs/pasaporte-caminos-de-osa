package io.kuralabs.caminosdeosa.passport.flip;

import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.view.MotionEvent;
import android.util.Log;

import com.eschao.android.widget.pageflip.Page;
import com.eschao.android.widget.pageflip.PageFlip;
import com.eschao.android.widget.pageflip.PageFlipException;
import com.eschao.android.widget.pageflip.PageFlipState;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class BookView extends GLSurfaceView implements Renderer {

    final static int DRAW_MOVING_FRAME = 0;
    final static int DRAW_ANIMATING_FRAME = 1;
    final static int DRAW_FULL_PAGE = 2;

    int pageNo;
    int drawCommand;
    ReentrantLock drawLock;

    PageFlip pageFlip;
    BookManager bookManager;

    public BookView(Context context, BookManager bookManager) {
        super(context);

        pageNo = 0;
        drawCommand = DRAW_FULL_PAGE;
        drawLock = new ReentrantLock();

        // Store book renderer
        this.bookManager = bookManager;

        // Create PageFlip
        pageFlip = new PageFlip(context);
        pageFlip.setSemiPerimeterRatio(0.8f)
                .setShadowWidthOfFoldEdges(5, 60, 0.3f)
                .setShadowWidthOfFoldBase(5, 80, 0.4f)
                .setPixelsOfMesh(10)
                .setListener(bookManager)
                .enableAutoPage(false);
        setEGLContextClientVersion(2);

        // Configure renderer
        // IMPORTANT:
        //      All methods implemented by this renderer are called from the OpenGL rendering
        //      thread. This call also starts that thread.
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /*
     * RENDERING MANAGEMENT
     *
     * IMPORTANT:
     *      All this calls are executed in the OpenGL rendering thread.
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        try {
            drawLock.lock();

            // 1. Delete unused textures.
            pageFlip.deleteUnusedTextures();
            Page page = pageFlip.getFirstPage();

            // 2. Handle drawing command triggered from finger moving and animating.
            if (drawCommand == DRAW_MOVING_FRAME || drawCommand == DRAW_ANIMATING_FRAME) {

                // Is forward flip.
                if (pageFlip.getFlipState() == PageFlipState.FORWARD_FLIP) {
                    Log.d("FORWARD_FLIP", Integer.toString(pageNo));

                    // Check if second texture of first page is valid, if not, create new one.
                    if (!page.isSecondTextureSet()) {
                        try {
                            bookManager.getLock().lock();
                            Log.d("setSecondTexture", "PageNo + 1: " + Integer.toString(pageNo + 1));
                            Bitmap bitmap = bookManager.getPage(pageNo + 1);
                            page.setSecondTexture(bitmap);
                        } finally {
                            bookManager.getLock().unlock();
                        }
                    }

                // Is backward flip
                } else {
                    Log.d("BACKWARD_FLIP", Integer.toString(pageNo));
                    if (!page.isSecondTextureSet()) {
                        page.setSecondTextureWithFirst();
                        try {
                            bookManager.getLock().lock();
                            Bitmap bitmap = bookManager.getPage(pageNo - 1);
                            page.setFirstTexture(bitmap);
                        } finally {
                            bookManager.getLock().unlock();
                        }
                    }
                }

                // Draw frame for page flip.
                pageFlip.drawFlipFrame();

            } else if (drawCommand == DRAW_FULL_PAGE) {
                // Draw stationary page without flipping.
                if (!page.isFirstTextureSet()) {
                    try {
                        bookManager.getLock().lock();
                        Bitmap bitmap = bookManager.getPage(pageNo);
                        page.setFirstTexture(bitmap);
                        Log.d("DRAW_FULL_PAGE", "setFirstTexture: " + Integer.toString(pageNo));
                    } finally {
                        bookManager.getLock().unlock();
                    }
                }

                pageFlip.drawPageFrame();
            }

            //////////
            if (drawCommand != DRAW_ANIMATING_FRAME) {
                return;
            }

            // continue animating
            if (pageFlip.animating()) {
                drawCommand = DRAW_ANIMATING_FRAME;
                requestRender();
                return;
            }

            // animation is finished
            final PageFlipState state = pageFlip.getFlipState();

            // update page number for backward flip
            if (state == PageFlipState.END_WITH_BACKWARD) {
                Log.d("END_WITH_BACKWARD", Integer.toString(pageNo));
                try {
                    bookManager.getLock().lock();
                    pageNo--;
                    bookManager.setPageNo(pageNo);
                    Log.d("setPageNo", Integer.toString(pageNo));
                } finally {
                    bookManager.getLock().unlock();
                }
                Log.d("END_WITH_BACKWARD", Integer.toString(pageNo));
            } else if (state == PageFlipState.END_WITH_FORWARD) {
                Log.d("END_WITH_FORWARD", "setFirstTextureWithSecond: " + Integer.toString(pageNo));
                page.setFirstTextureWithSecond();

                try {
                    bookManager.getLock().lock();
                    pageNo++;
                    bookManager.setPageNo(pageNo);
                    Log.d("setPageNo", Integer.toString(pageNo));
                } finally {
                    bookManager.getLock().unlock();
                }
            }

            drawCommand = DRAW_FULL_PAGE;
            Log.d("DRAW_FULL_PAGE", Integer.toString(pageNo));
            requestRender();
        }
        finally {
            drawLock.unlock();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        try {
            pageFlip.onSurfaceChanged(width, height);
        } catch (PageFlipException e) {
            Log.e("BookView", "Failed to run PageFlipFlipRender:onSurfaceChanged");
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        try {
            pageFlip.onSurfaceCreated();
        } catch (PageFlipException e) {
            Log.e("BookView", "Failed to run PageFlipFlipRender:onSurfaceCreated");
        }
    }

    /*
     * GESTURE MANAGEMENT
     *
     * IMPORTANT:
     *      All this calls are executed in the main thread.
     */
    protected void onFingerDown(float x, float y) {
        // If the animation is going, we should ignore this event to avoid mess drawing on screen
        if (!pageFlip.isAnimating() && pageFlip.getFirstPage() != null) {
            pageFlip.onFingerDown(x, y);
        }
    }

    protected void onFingerMove(float x, float y) {
        if (pageFlip.isAnimating()) {
            // Nothing to do during animating
            return;
        }

        if (pageFlip.canAnimate(x, y)) {
            // If the point is out of current page, try to start animating
            onFingerUp(x, y);
            return;
        }

        // Move page by finger
        if (pageFlip.onFingerMove(x, y)) {
            try {
                drawLock.lock();
                drawCommand = DRAW_MOVING_FRAME;
                requestRender();
            }
            finally {
                drawLock.unlock();
            }
        }
    }

    protected void onFingerUp(float x, float y) {
        if (!pageFlip.isAnimating()) {
            pageFlip.onFingerUp(x, y, 1000);

            try {
                drawLock.lock();
                if (pageFlip.animating()) {
                    drawCommand = DRAW_ANIMATING_FRAME;
                    requestRender();
                }
            } finally {
                drawLock.unlock();
            }
        }
    }


    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_UP) {
            onFingerUp(event.getX(), event.getY());
            return true;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            onFingerDown(event.getX(), event.getY());
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            onFingerMove(event.getX(), event.getY());
            return true;
        }

        return false;
    }
}
