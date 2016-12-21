package ru.yourok.m3u8loader;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;

import go.m3u8.M3u8;
import go.m3u8.State;
import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderService;
import ru.yourok.loader.LoaderServiceHandler;
import ru.yourok.loader.Options;


public class MainActivity extends AppCompatActivity implements LoaderService.LoaderServiceCallbackUpdate {
    public static AdaptorLoadresList loadersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoaderService.registerOnUpdateLoader(this);
        LoaderService.startService(this);
        if (loadersList == null) {
            loadersList = new AdaptorLoadresList(this);
        } else {
            loadersList.setContext(this);
            UpdateList();
        }
        ListView listView = ((ListView) findViewById(R.id.listViewLoaders));
        listView.setAdapter(loadersList);

        if (loadersList.getSelected() == -1)
            findViewById(R.id.itemLoaderMenu).setVisibility(View.GONE);

        setMenuClickListener();
        requestPermissionWithRationale();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LoaderService.registerOnUpdateLoader(this);
    }

    @Override
    protected void onStop() {
        LoaderService.registerOnUpdateLoader(null);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoaderService.registerOnUpdateLoader(this);
        UpdateList();
    }

    @Override
    protected void onPause() {
        LoaderService.registerOnUpdateLoader(null);
        super.onPause();
    }

    public void UpdateList() {
        if (loadersList != null)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadersList.notifyDataSetChanged();
                    if (loadersList.getSelected() == -1)
                        findViewById(R.id.itemLoaderMenu).setVisibility(View.GONE);
                }
            });
    }

    public void onAddClick(View view) {
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
    }

    public void onDownloadClick(View view) {
        view.setEnabled(false);
        view.invalidate();
        view.refreshDrawableState();

        LoaderServiceHandler.loadersQueue.clear();
        for (int i = 0; i < LoaderServiceHandler.SizeLoaders(); i++) {
            State st = LoaderServiceHandler.GetLoader(i).GetState();
            if (st == null || st.getStage() != M3u8.Stage_Finished)
                LoaderServiceHandler.AddQueue(i);
        }
        LoaderService.load(this);

        view.setEnabled(true);
    }

    public void onStopClick(View view) {
        LoaderService.stop(this);
    }

    public void onSettingsClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onUpdateLoader(int id) {
        if (id == -1)
            return;
        UpdateList();
    }

    public void requestPermissionWithRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Snackbar.make(findViewById(R.id.main_layout), R.string.permission_msg, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.permission_btn, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void setMenuClickListener() {
        //Menu
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int sel = loadersList.getSelected();
                Loader loader = LoaderServiceHandler.GetLoader(sel);
                if (loader == null) {
                    loadersList.setSelected(-1);
                    return;
                }
                if (sel == -1)
                    return;
                switch (view.getId()) {
                    case R.id.buttonItemMenuStart:
                        LoaderServiceHandler.AddQueue(sel);
                        LoaderService.load(MainActivity.this);
                        break;
                    case R.id.buttonItemMenuStop:
                        if (loader.IsWorking())
                            loader.Stop();
                        break;
                    case R.id.buttonItemMenuRemove:
                        LoaderService.stop(MainActivity.this);
                        loader.RemoveTemp();
                        LoaderServiceHandler.RemoveLoader(sel);
                        if (sel >= LoaderServiceHandler.SizeLoaders())
                            sel--;
                        loadersList.setSelected(sel);
                        Options.getInstance(MainActivity.this).SaveList();
                        break;
                    case R.id.buttonItemMenuEdit:
                        Intent intent = new Intent(MainActivity.this, ListEditActivity.class);
                        intent.putExtra("LoaderID", sel);
                        MainActivity.this.startActivity(intent);
                        break;
                }
                UpdateList();
            }
        };

        findViewById(R.id.buttonItemMenuStart).setOnClickListener(clickListener);
        findViewById(R.id.buttonItemMenuStop).setOnClickListener(clickListener);
        findViewById(R.id.buttonItemMenuRemove).setOnClickListener(clickListener);
        findViewById(R.id.buttonItemMenuEdit).setOnClickListener(clickListener);
    }
}