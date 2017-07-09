package io.kuralabs.caminosdeosa.passport;

import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.support.design.widget.FloatingActionButton;

import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.integration.android.IntentIntegrator;

import com.vansuita.pickimage.bean.PickResult;

import io.kuralabs.caminosdeosa.passport.flip.BookView;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    BookView bookView;
    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        decorView = getWindow().getDecorView();
        bookView = new BookView(this, new Passport(this));

        FrameLayout passportView = new FrameLayout(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View passportWidgets = inflater.inflate(R.layout.passport_widgets, null, false);

        passportView.addView(bookView);
        passportView.addView(passportWidgets);

        setContentView(passportView);

        initMenu();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String message = result.getContents() == null ?
                    "Cancelled" :
                    "Scanned: " + result.getContents();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    // Menu Setup
    private void initMenu() {
        Map<String, LinearLayout> floatingButtons = new HashMap<String, LinearLayout>();
        floatingButtons.put("share", (LinearLayout) findViewById(R.id.shareFabLayout));
        floatingButtons.put("add", (LinearLayout) findViewById(R.id.addStampFabLayout));
        floatingButtons.put("photo", (LinearLayout) findViewById(R.id.addPhotoFabLayout));
        floatingButtons.put("edit", (LinearLayout) findViewById(R.id.editFabLayout));

        FloatingMenu menu = new FloatingMenu(this, floatingButtons);
        menu.onShareClick((FloatingActionButton) findViewById(R.id.shareFab));
        menu.onEditClick((FloatingActionButton) findViewById(R.id.editFab));
        menu.onAddPhotoClick((FloatingActionButton) findViewById(R.id.addPhotoFab), this);
        menu.onAddStampClick((FloatingActionButton) findViewById(R.id.addStampFab), this);
        menu.onOpenMenuClick((FloatingActionButton) findViewById(R.id.menuFab));
        menu.onOverlayClick(findViewById(R.id.fabBGLayout));
    }
}
