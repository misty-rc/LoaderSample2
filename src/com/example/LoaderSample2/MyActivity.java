package com.example.LoaderSample2;

import android.app.*;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class MyActivity extends Activity {

    private String[] _PlanetTitle;
    private DrawerLayout _DrawerLayout;
    private ListView _DrawerList;
    private ActionBarDrawerToggle _DrawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //loader
        LoaderManager.enableDebugLogging(true);
        getLoaderManager().initLoader(0, null, callbacks);


        _PlanetTitle = getResources().getStringArray(R.array.planets_array);
        _DrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        _DrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        _DrawerList = (ListView) findViewById(R.id.left_drawer_list);
        _DrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, _PlanetTitle));
        _DrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        _DrawerToggle = new ActionBarDrawerToggle(
                this,
                _DrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };

        _DrawerLayout.setDrawerListener(_DrawerToggle);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        _DrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _DrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(_DrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            return false;
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    }

    private final LoaderManager.LoaderCallbacks<String> callbacks = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            ProgressDialogFragment dialog = new ProgressDialogFragment();
            Bundle arg = new Bundle();
            arg.putString(Ref.PROGRESS_MESSAGE, "データほげほげ");
            dialog.setArguments(arg);
            dialog.show(getFragmentManager(), Ref.PROGRESS_TAG);
            return new MyLoader(MyActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            ProgressDialogFragment dialog =
                    (ProgressDialogFragment)getFragmentManager().findFragmentByTag(Ref.PROGRESS_TAG);
            if(dialog != null) {
                dialog.onDismiss(dialog.getDialog());
            }

            TextView tv = (TextView)findViewById(R.id.tv);
            tv.setText(data);
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };



    public static class MyLoader extends AsyncTaskLoader<String> {

        private String _data;

        public MyLoader(Context context) {
            super(context);
        }

        @Override
        public String loadInBackground() {
            StringBuilder buf = new StringBuilder();

            try {
                URL url = new URL("https://qiita.com/api/v1/users/misty_rc");
                HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Connection", "close");
                conn.setFixedLengthStreamingMode(0);
                conn.connect();

                Log.d("QIITA", "response code: " + conn.getResponseCode());

                int code = conn.getResponseCode();
                InputStream is = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String tmp;

                while((tmp = reader.readLine()) != null) {
                    buf.append(tmp);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return buf.toString();
        }

        @Override
        public void deliverResult(String data) {
            if(isReset()) {
                return;
            }

            _data = data;
            super.deliverResult(data);
        }

        @Override
        protected void onStartLoading() {
            if(_data != null) {
                deliverResult(_data);
            } else {
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        @Override
        protected void onReset() {
            super.onReset();
            onStopLoading();
            _data = null;
        }
    }

    public static class ProgressDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getArguments().getString(Ref.PROGRESS_MESSAGE));
            return dialog;
        }
    }
}
