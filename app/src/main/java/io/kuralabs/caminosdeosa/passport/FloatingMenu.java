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

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public class FloatingMenu {

    LinearLayout editFabLayout, shareFabLayout, addPhotoFabLayout, addStampFabLayout;
    FloatingActionButton menuFab;
    Map<String, String[]> floatingMenuConfig;
    Map<String, LinearLayout> floatingLayouts;
    boolean isMenuOpen = false;
    boolean isAnimating = false;
    Context context;
    View fabOverlay;
    String currentPage = "cover";
    int floatingButtonsPosition[] = {
        R.dimen.standard_55,
        R.dimen.standard_100,
        R.dimen.standard_145,
        R.dimen.standard_190
    };

    public FloatingMenu(Context mainContext, Map<String, LinearLayout> buttonsLayouts) {
        floatingLayouts = buttonsLayouts;
        context = mainContext;
        setMenuConfig();
    }

    public FloatingMenu setCurrentPage(String page) {

        if (!floatingMenuConfig.containsKey(page)) {
            throw new InvalidParameterException("Unknown page type " + page);
        }
        this.currentPage = page;

        boolean hideMenuButton = page.equals("cover") || page.equals("manifesto");
        menuFab.setVisibility(hideMenuButton ? View.GONE : View.VISIBLE);

        return this;
    }

    public void onShareClick(FloatingActionButton button) {
        shareFabLayout = floatingLayouts.get("share");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAnimating) {
                    return;
                }
            }
        });
    }

    public void onEditClick(FloatingActionButton button) {
        editFabLayout = floatingLayouts.get("edit");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAnimating) {
                    return;
                }
            }
        });
    }

    public void onAddStampClick(FloatingActionButton button, final Activity activity) {
        addStampFabLayout = floatingLayouts.get("add");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAnimating) {
                    return;
                }
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
                if (isAnimating) {
                    return;
                }
                // Pick Image
                PickSetup setup = new PickSetup().setSystemDialog(true);
                PickImageDialog.build(setup).show((FragmentActivity) activity);
            }
        });
    }

    public void onOpenMenuClick(FloatingActionButton button) {
        menuFab = button;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAnimating) {
                    return;
                }
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
                if (isAnimating) {
                    return;
                }
                hideFabMenu();
            }
        });
    }

    private void setMenuConfig() {
        floatingMenuConfig = new HashMap<String, String[]>();
        floatingMenuConfig.put("cover", null);
        floatingMenuConfig.put("profile", new String[]{ "edit" });
        floatingMenuConfig.put("manifesto", null);
        floatingMenuConfig.put("stamps", new String[]{ "share", "photo" });
        floatingMenuConfig.put("stamps_empty", new String[]{ "add" });
    }

    public void showFabMenu(String[] actions) {
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

    public void hideFabMenu() {
        isMenuOpen = false;

        fabOverlay.setVisibility(View.GONE);
        menuFab.animate().rotationBy(-180);
        shareFabLayout.animate().translationY(0);
        addPhotoFabLayout.animate().translationY(0);
        addStampFabLayout.animate().translationY(0);
        editFabLayout.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimating = false;
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
}
