package io.kuralabs.caminosdeosa.passport;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;

import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.integration.android.IntentIntegrator;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.listeners.IPickResult;

import io.kuralabs.caminosdeosa.passport.flip.BookView;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements IPickResult {

    View decorView;

    // Passport
    Passport passport;
    BookView bookView;

    // Menu
    Handler handler;
    FloatingMenu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        decorView = getWindow().getDecorView();

        passport = new Passport(this);
        bookView = new BookView(this, passport);

        FrameLayout rootView = new FrameLayout(this);

        // Add passport view
        rootView.addView(bookView);

        // Load and add menu widgets
        LayoutInflater inflater = LayoutInflater.from(this);
        View passportWidgets = inflater.inflate(R.layout.passport_widgets, null, false);
        rootView.addView(passportWidgets);

        // Set the view for this activity
        setContentView(rootView);

        // Initialize the menu and connect the page change notification
        initMenu();

        handler = new Handler() {
            public void handleMessage(Message msg) {
                int pageNo = msg.arg1;
                int pages = msg.arg2;
                String page;

                if (pageNo == 0) {
                    page = "cover";
                } else if (pageNo == 1) {
                    page = "profile";
                } else if (pageNo == 2) {
                    page = "manifesto";
                } else if (pageNo == 3) {
                    page = "manifesto";
                } else if (pageNo < pages - 1) {
                    page = "stamps";
                } else {
                    page = "stamps_empty";
                }

                Log.d(
                    "MainActivity",
                    "pageNo: " + Integer.toString(pageNo) +
                    " , pages: " + Integer.toString(pages) +
                    " , page: " + page
                );

                menu.setCurrentPage(page);
            }
        };
        passport.addOnPageChangeListener(handler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
        bookView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bookView.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return bookView.onTouchEvent(event);
    }

    // Menu Setup
    private void initMenu() {
        Map<String, LinearLayout> floatingButtons = new HashMap<>();
        floatingButtons.put("share", (LinearLayout) findViewById(R.id.shareFabLayout));
        floatingButtons.put("add", (LinearLayout) findViewById(R.id.addStampFabLayout));
        floatingButtons.put("photo", (LinearLayout) findViewById(R.id.addPhotoFabLayout));
        floatingButtons.put("edit", (LinearLayout) findViewById(R.id.editFabLayout));

        menu = new FloatingMenu(this, floatingButtons);
        menu.onShareClick((FloatingActionButton) findViewById(R.id.shareFab), passport);
        menu.onEditClick((FloatingActionButton) findViewById(R.id.editFab));
        menu.onAddPhotoClick((FloatingActionButton) findViewById(R.id.addPhotoFab), this);
        menu.onAddStampClick((FloatingActionButton) findViewById(R.id.addStampFab), this);
        menu.onOpenMenuClick((FloatingActionButton) findViewById(R.id.menuFab));
        menu.onOverlayClick(findViewById(R.id.fabBGLayout));

        menu.setCurrentPage("cover");
    }

    /**
     * Button callbacks
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        String contents = result.getContents();

        if (contents == null) {
            return;
        }

        if (!contents.startsWith("cdo://")) {
            Toast.makeText(this, "Not an OSA QR Code", Toast.LENGTH_LONG).show();
            return;
        }

        String stamp = contents.substring("cdo://".length());
        menu.hideFabMenu();
        passport.drawStamp(stamp);
        bookView.requestRender();
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() != null) {
            Toast.makeText(this, r.getError().toString(), Toast.LENGTH_LONG).show();
            return;
        }
        Bitmap photo = r.getBitmap();
        menu.hideFabMenu();
        passport.drawPhoto(photo);
        bookView.requestRender();
    }
}
