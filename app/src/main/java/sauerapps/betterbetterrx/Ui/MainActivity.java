package sauerapps.betterbetterrx.Ui;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sauerapps.betterbetterrx.Model.VersionModel;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.Adapter.SimpleRecyclerAdapter;
import sauerapps.betterbetterrx.utilities.MyRecyclerScroll;
import sauerapps.betterbetterrx.utilities.Utils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    RecyclerView recyclerView;
    int fabMargin;
    LinearLayout toolbarContainer;
    int toolbarHeight;
    boolean fadeToolbar = false;
    SimpleRecyclerAdapter simpleRecyclerAdapter;

    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

//        List<String> spinnerList = new ArrayList<>();
//        spinnerList.add("Hide on scroll");
//        spinnerList.add("Fade on scroll");

        setSupportActionBar(toolbar);

//        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.toolbar_spinner,
//                toolbar, false);
//        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        toolbar.addView(spinnerContainer, lp);
//
//        MySpinnerAdapter spinnerAdapter = new MySpinnerAdapter();
//        spinnerAdapter.addItems(spinnerList);
//
//        Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.toolbar_spinner);
//        spinner.setAdapter(spinnerAdapter);
//
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                switch (position) {
//                    case 0:
//                        fadeToolbar = false;
//                        break;
//                    case 1:
//                        fadeToolbar = true;
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
        
        fabMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        toolbarHeight = Utils.getToolbarHeight(this);

        toolbarContainer = (LinearLayout) findViewById(R.id.fabhide_toolbar_container);
        recyclerView = (RecyclerView) findViewById (R.id.recyclerview);

        /* Set top padding= toolbar height.
         So there is no overlap when the toolbar hides.
         Avoid using 0 for the other parameters as it resets padding set via XML!*/
        recyclerView.setPadding(recyclerView.getPaddingLeft(), toolbarHeight,
                recyclerView.getPaddingRight(), recyclerView.getPaddingBottom());

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Adding list data thrice for a more comfortable scroll.
        List<String> listData = new ArrayList<String>();
        int ct = 0;
        for (int i = 0; i < VersionModel.data.length * 3; i++) {
            listData.add(VersionModel.data[ct]);
            ct++;
            if (ct == VersionModel.data.length) {
                ct = 0;
            }
        }

        if (simpleRecyclerAdapter == null) {
            simpleRecyclerAdapter = new SimpleRecyclerAdapter(listData);
            recyclerView.setAdapter(simpleRecyclerAdapter);
        }

        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                toolbarContainer .animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                if (fadeToolbar)
                    toolbarContainer.animate().alpha(1).setInterpolator(new DecelerateInterpolator(1)).start();
                fab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }

            @Override
            public void hide() {
                toolbarContainer.animate().translationY(-toolbarHeight).setInterpolator(new AccelerateInterpolator(2)).start();
                if (fadeToolbar)
                    toolbarContainer.animate().alpha(0).setInterpolator(new AccelerateInterpolator(1)).start();
                fab.animate().translationY(fab.getHeight() + fabMargin).setInterpolator(new AccelerateInterpolator(2)).start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    private class MySpinnerAdapter extends BaseAdapter {
//        private List<String> mItems = new ArrayList<>();
//
//        public void clear() {
//            mItems.clear();
//        }
//
//        public void addItems(List<String> yourObjectList) {
//            mItems.addAll(yourObjectList);
//        }
//
//        @Override
//        public int getCount() {
//            return mItems.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return mItems.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getDropDownView(int position, View view, ViewGroup parent) {
//            if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
//                view = getLayoutInflater().inflate(R.layout.toolbar_spinner_item_dropdown, parent, false);
//                view.setTag("DROPDOWN");
//            }
//
//            TextView textView = (TextView) view.findViewById(android.R.id.text1);
//            textView.setText(getTitle(position));
//
//            return view;
//        }
//
//        @Override
//        public View getView(int position, View view, ViewGroup parent) {
//            if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
//                view = getLayoutInflater().inflate(R.layout.
//                        toolbar_spinner_item_actionbar, parent, false);
//                view.setTag("NON_DROPDOWN");
//            }
//            TextView textView = (TextView) view.findViewById(android.R.id.text1);
//            textView.setText(getTitle(position));
//            return view;
//        }
//
//        private String getTitle(int position) {
//            return position >= 0 && position < mItems.size() ? mItems.get(position) : "";
//        }
//    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                animateFAB();
                break;
            case R.id.fab1:


                Log.d("Raj", "Fab 1");
                break;
            case R.id.fab2:


                Log.d("Raj", "Fab 2");
                break;
        }
    }


    public void animateFAB() {


        if (isFabOpen) {


            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
            Log.d("Raj", "close");


        } else {


            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
            Log.d("Raj", "open");


        }
    }

}
