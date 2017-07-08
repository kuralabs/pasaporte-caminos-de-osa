package io.kuralabs.caminosdeosa.passport;

import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.support.design.widget.FloatingActionButton;

import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.integration.android.IntentIntegrator;

import io.kuralabs.caminosdeosa.passport.flip.PageFlipView;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton addStampButton;
    PageFlipView mPageFlipView;
    View decorView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPageFlipView = new PageFlipView(this);

        FrameLayout passportView = new FrameLayout(this);
        LinearLayout passportWidgets = new LinearLayout(this);

        addStampButton = new FloatingActionButton(this);
        addStampButton.setCompatElevation(4.0f);

        passportWidgets.addView(addStampButton);

        passportView.addView(mPageFlipView);
        passportView.addView(passportWidgets);

        setContentView(passportView);

        decorView = getWindow().getDecorView();

        addStampButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.initiateScan();
            }
        });
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

        mPageFlipView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPageFlipView.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_UP) {
            mPageFlipView.onFingerUp(event.getX(), event.getY());
            return true;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            mPageFlipView.onFingerDown(event.getX(), event.getY());
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            mPageFlipView.onFingerMove(event.getX(), event.getY());
            return true;
        }

        return false;
    }
}
