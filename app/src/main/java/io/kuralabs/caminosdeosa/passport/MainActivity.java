package io.kuralabs.caminosdeosa.passport;

import android.animation.Animator;
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
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;
import com.vansuita.pickimage.bundle.PickSetup;

import io.kuralabs.caminosdeosa.passport.flip.PageFlipView;

public class MainActivity extends AppCompatActivity implements IPickResult {

    FloatingActionButton menuFab, editFab, shareFab, addPhotoFab, addStampFab;
    LinearLayout fabLayouts[], editFabLayout, shareFabLayout, addPhotoFabLayout, addStampFabLayout;
    PageFlipView pageFlipView;
    View decorView;

    View fabOverlay;
    boolean isMenuOpen = false;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        decorView = getWindow().getDecorView();

        pageFlipView = new PageFlipView(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View passportWidgets= inflater.inflate(R.layout.passport_widgets, null, false);

        FrameLayout passportView = new FrameLayout(this);
        passportView.addView(pageFlipView);
        passportView.addView(passportWidgets);

        setContentView(passportView);

        setupMenuFab();

    }

    private void setupMenuFab() {
        shareFabLayout = (LinearLayout) findViewById(R.id.shareFabLayout);
        addPhotoFabLayout = (LinearLayout) findViewById(R.id.addPhotoFabLayout);
        addStampFabLayout = (LinearLayout) findViewById(R.id.addStampFabLayout);
        editFabLayout = (LinearLayout) findViewById(R.id.editFabLayout);

        shareFab = (FloatingActionButton) findViewById(R.id.shareFab);
        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        addPhotoFab= (FloatingActionButton) findViewById(R.id.addPhotoFab);
        addPhotoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pick Image
                PickSetup setup = new PickSetup().setSystemDialog(true);
                PickImageDialog.build(setup).show(MainActivity.this);
            }
        });

        addStampFab = (FloatingActionButton) findViewById(R.id.addStampFab);
        addStampFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Scan QR
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.initiateScan();
            }
        });

        editFab = (FloatingActionButton) findViewById(R.id.editFab);
        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        menuFab = (FloatingActionButton) findViewById(R.id.menuFab);
        menuFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isMenuOpen){
                    showFabMenu();
                } else {
                    hideFabMenu();
                }
            }
        });

        fabOverlay = findViewById(R.id.fabBGLayout);
        fabOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideFabMenu();
            }
        });

        fabLayouts = new LinearLayout[] {
            shareFabLayout, addStampFabLayout, addPhotoFabLayout, editFabLayout
        };
    }

    private void showFabMenu() {
        isMenuOpen = true;

        for (LinearLayout l : fabLayouts) {
            l.setVisibility(View.VISIBLE);
        }
        fabOverlay.setVisibility(View.VISIBLE);

        menuFab.animate().rotationBy(180);
        shareFabLayout.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        addPhotoFabLayout.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
        addStampFabLayout.animate().translationY(-getResources().getDimension(R.dimen.standard_145));
        editFabLayout.animate().translationY(-getResources().getDimension(R.dimen.standard_190));
    }

    private void hideFabMenu() {
        isMenuOpen = false;

        fabOverlay.setVisibility(View.GONE);
        menuFab.animate().rotationBy(-180);
        shareFabLayout.animate().translationY(0);
        addPhotoFabLayout.animate().translationY(0);
        addStampFabLayout.animate().translationY(0);
        editFabLayout.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isMenuOpen) {
                    for (LinearLayout l : fabLayouts) {
                        l.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

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

        pageFlipView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        pageFlipView.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_UP) {
            pageFlipView.onFingerUp(event.getX(), event.getY());
            return true;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            pageFlipView.onFingerDown(event.getX(), event.getY());
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            pageFlipView.onFingerMove(event.getX(), event.getY());
            return true;
        }

        return false;
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            //If you want the Uri.
            //Mandatory to refresh image from Uri.
            //getImageView().setImageURI(null);

            //Setting the real returned image.
            //getImageView().setImageURI(r.getUri());

            //If you want the Bitmap.

            // getImageView().setImageBitmap(r.getBitmap());
            Toast.makeText(this, r.getPath(), Toast.LENGTH_LONG).show();
            //Image path
            //r.getPath();
        } else {
            //Handle possible errors
            //TODO: do what you have to do with r.getError();
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
