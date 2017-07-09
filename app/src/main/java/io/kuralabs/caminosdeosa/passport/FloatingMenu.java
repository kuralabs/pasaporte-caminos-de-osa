package io.kuralabs.caminosdeosa.passport;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.HashMap;
import java.util.Map;

public class FloatingMenu implements IPickResult {

    LinearLayout menuFabLayout, editFabLayout, shareFabLayout, addPhotoFabLayout, addStampFabLayout;
    FloatingActionButton menuFab;
    Map<String, String[]> floatingMenuConfig;
    Map<String, LinearLayout> floatingLayouts;
    Context context;
    View fabOverlay;

    //TODO: Pull the current page from the actual PageFlip component
    String currentPage = "cover";

    public FloatingMenu setCurrentPage(String page) {
        this.currentPage = page;

        boolean hideMenuButton = page.equals("cover") || page.equals("manifesto");
        menuFabLayout.setVisibility(hideMenuButton? View.GONE : View.VISIBLE);

        return this;
    }

    boolean isMenuOpen = false;

    int floatingButtonsPosition[] = {
        R.dimen.standard_55, R.dimen.standard_100, R.dimen.standard_145, R.dimen.standard_190
    };

    public FloatingMenu(Context mainContext, Map<String, LinearLayout> buttonsLayouts) {
        floatingLayouts = buttonsLayouts;
        context = mainContext;
        setMenuConfig();
    }


    public void onShareClick(FloatingActionButton button) {
        shareFabLayout = floatingLayouts.get("share");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    public void onEditClick(FloatingActionButton button) {
        editFabLayout = floatingLayouts.get("edit");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    public void onAddStampClick(FloatingActionButton button, final Activity activity) {
        addStampFabLayout = floatingLayouts.get("add");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.initiateScan();
            }
        });
    }

    public void onAddPhotoClick(FloatingActionButton button, final Activity activity) {
        addPhotoFabLayout = floatingLayouts.get("photo");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pick Image
                PickSetup setup = new PickSetup().setSystemDialog(true);
                PickImageDialog.build(setup).show((FragmentActivity) activity);
            }
        });
    }

    public void onOpenMenuClick(FloatingActionButton button) {
        menuFabLayout = floatingLayouts.get("menu");
        menuFab = button;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isMenuOpen) {
                    showFabMenu(floatingMenuConfig.get(currentPage));
                } else {
                    hideFabMenu();
                }
            }
        });
    }

    public void onOverlayClick(View overlay) {
        fabOverlay = overlay;
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideFabMenu();
            }
        });
    }

    private void setMenuConfig() {
        floatingMenuConfig = new HashMap<String, String[]>();
        floatingMenuConfig.put("cover", null);
        floatingMenuConfig.put("data", new String[]{ "edit" });
        floatingMenuConfig.put("manifesto", null);
        floatingMenuConfig.put("stamps", new String[]{ "share", "photo" });
        floatingMenuConfig.put("stamps_empty", new String[]{ "add" });
    }

    private void showFabMenu(String[] actions) {
        isMenuOpen = true;

        if (actions != null) {

            fabOverlay.setVisibility(View.VISIBLE);
            menuFab.animate().rotationBy(180);

            for (int i = 0; i < actions.length; i++) {
                LinearLayout button = floatingLayouts.get(actions[i]);
                button.setVisibility(View.VISIBLE);
                button.animate().translationY(-context.getResources().getDimension(floatingButtonsPosition[i]));
            }
        }
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
                    for (LinearLayout l : floatingLayouts.values()) {
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
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            //If you want the Uri.
            //Mandatory to refresh image from Uri.
            //getImageView().setImageURI(null);

            //Setting the real returned image.
            //getImageView().setImageURI(r.getUri());

            //If you want the Bitmap.

            // getImageView().setImageBitmap(r.getBitmap());
            Toast.makeText(context, r.getPath(), Toast.LENGTH_LONG).show();
            //Image path
            //r.getPath();
        } else {
            //Handle possible errors
            //TODO: do what you have to do with r.getError();
            Toast.makeText(context, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
