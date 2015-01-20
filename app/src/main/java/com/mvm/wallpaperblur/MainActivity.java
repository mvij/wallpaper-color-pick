package com.mvm.wallpaperblur;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends ActionBarActivity {


    private static final int INITIAL = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private float mWidth = 0;
    private float mHeight = 0;
    private int states = INITIAL;
    private int mWholeColor;
    private int mTopColor;

    private String mTopString, mWholeString;
    private Menu mMenu;

    boolean first = false;

    Comparator<Object> mComparator = new Comparator<Object>() {
        @Override
        public int compare(Object lhs, Object rhs) {
            Palette.Swatch l = (Palette.Swatch)lhs;
            Palette.Swatch r = (Palette.Swatch)rhs;
            if(l.getPopulation()<r.getPopulation())
                return 1;
            else if(l.getPopulation()==r.getPopulation())
                return 0;
            else
                return -1;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        init();
        setSupportProgressBarIndeterminateVisibility(false);
    }

    private void init() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mHeight = dm.heightPixels * 0.3f;

        getValuesFromBackground();

    }

    private void getValuesFromBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WallpaperManager myWallpaperManager = WallpaperManager.getInstance(
                        getApplicationContext());
                try {
                    final BitmapDrawable d = (BitmapDrawable) myWallpaperManager.getDrawable();

                    Bitmap src = d.getBitmap();

                    Palette.generateAsync(src, new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            Log.d("TTTTTTT","ongen whole");
                            if (palette != null) {
                                List<Palette.Swatch> swatches = palette.getSwatches();
                                Object[] objects = swatches.toArray();
                                Arrays.sort(objects,mComparator);
                                for (Object obj : objects) {
                                    Palette.Swatch s = (Palette.Swatch) obj;
                                    if (isSuitableOnWhite(s.getRgb())) {
                                        mWholeColor = s.getRgb();
                                        mWholeString = s.toString();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((TextView) findViewById(R.id.text_bottom))
                                                        .setTextColor(mWholeColor);
                                                Log.d("TTTTTTT","ongen whole before");
                                                changeMenuButton();
                                                Log.d("TTTTTTT","ongen whole after");
                                            }
                                        });
                                        break;
                                    }
                                }
                            }
                        }
                    });

                    Bitmap b = Bitmap.createBitmap(src, 0, 0, src.getWidth(), (int) mHeight);

                    Palette.generateAsync(b, new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            Log.d("TTTTTTT","ongen top");
                            if (palette != null) {
                                List<Palette.Swatch> swatches = palette.getSwatches();
                                Object[] objects = swatches.toArray();
                                Arrays.sort(objects,mComparator);

                                for (Object obj : objects) {
                                    Palette.Swatch s = (Palette.Swatch) obj;
                                    if (isSuitableOnWhite(s.getRgb())) {
                                        mTopColor = s.getRgb();
                                        mTopString = s.toString();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((TextView) findViewById(R.id.text_top))
                                                        .setTextColor(mTopColor);
                                                Log.d("TTTTTTT", "ongen top before");
                                                changeMenuButton();
                                                Log.d("TTTTTTT", "ongen top after");
                                            }
                                        });
                                        break;
                                    }
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private synchronized void changeMenuButton() {
        if (first) {
            if(mMenu!=null)
            mMenu.findItem(R.id.action_change).setVisible(true);
        } else {
            first = true;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_change) {
            switch (states) {
                case INITIAL:
                    TextView t = (TextView) findViewById(R.id.text_bottom);
                    t.setBackgroundColor(Color.WHITE);
                    SpannableStringBuilder s = new SpannableStringBuilder(mWholeString);
                    s.setSpan(new UnderlineSpan(), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    t.setText(s);
                    ((TextView) findViewById(R.id.text_top)).setText("");
                    ((TextView) findViewById(R.id.text_top)).setBackgroundColor(Color.TRANSPARENT);
                    ((TextView) findViewById(R.id.text_bottom)).setVisibility(View.VISIBLE);
                    states = ONE;
                    break;
                case ONE:
                    TextView t1 = (TextView) findViewById(R.id.text_top);
                    t1.setBackgroundColor(Color.WHITE);
                    SpannableStringBuilder s1 = new SpannableStringBuilder(mTopString);
                    s1.setSpan(new UnderlineSpan(), 0, s1.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    t1.setText(s1);
                    ((TextView) findViewById(R.id.text_bottom)).setVisibility(View.GONE);
                    states = INITIAL;
                    break;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isSuitableOnWhite(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.green(color);

        int yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return (yiq < 128);
    }
}
