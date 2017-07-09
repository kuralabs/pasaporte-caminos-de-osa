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

    public BookView(Context context, BookManager manager) {
        super(context);

        pageNo = 0;
        drawCommand = DRAW_FULL_PAGE;
        drawLock = new ReentrantLock();

        // Store book renderer
        bookManager = manager;

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
            int drawCommandCache = drawCommand;
            if (drawCommandCache == DRAW_MOVING_FRAME || drawCommandCache == DRAW_ANIMATING_FRAME) {

                // Is forward flip.
                if (pageFlip.getFlipState() == PageFlipState.FORWARD_FLIP) {
                    // Check if second texture of first page is valid, if not, create new one.
                    if (!page.isSecondTextureSet()) {
                        Bitmap bitmap = bookManager.getPage(pageNo + 1);
                        page.setSecondTexture(bitmap);
                    }

                    // Is backward flip
                } else if (!page.isFirstTextureSet()) {
                    // In backward flip, check first texture of first page is valid.
                    Bitmap bitmap = bookManager.getPage(--pageNo);
                    page.setFirstTexture(bitmap);
                }

                // Draw frame for page flip.
                pageFlip.drawFlipFrame();

            } else if (drawCommandCache == DRAW_FULL_PAGE) {
                // Draw stationary page without flipping.
                if (!page.isFirstTextureSet()) {
                    Bitmap bitmap = bookManager.getPage(pageNo);
                    page.setFirstTexture(bitmap);
                }

                pageFlip.drawPageFrame();
            }

            //////////
            if (drawCommandCache != DRAW_ANIMATING_FRAME) {
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
                // don't do anything on page number since mPageNo is always
                // represents the FIRST_TEXTURE no;
            } else if (state == PageFlipState.END_WITH_FORWARD) {
                // update page number and switch textures for forward flip
                pageFlip.getFirstPage().setFirstTextureWithSecond();
                pageNo++;
                Log.d("FOO", Integer.toString(pageNo));
            }

            drawCommand = DRAW_FULL_PAGE;
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
            }
            finally {
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
