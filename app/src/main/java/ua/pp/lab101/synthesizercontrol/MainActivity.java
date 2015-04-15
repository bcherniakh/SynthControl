package ua.pp.lab101.synthesizercontrol;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import ua.pp.lab101.synthesizercontrol.service.BoardManagerService;

public class MainActivity extends ActionBarActivity
        implements OperationModeFragment.OperationModeListener, IMainConstants, IServiceDistributor {
    /*Service members*/
    private BoardManagerService mService;
    private BoardManagerService.LocalBinder mBinder;
    private boolean mBound;

    public static String[] ModesTitleArray;
    public static final int UNSELECTED = -1;

    private FragmentManager mFragmentManager;
    private final OperationModeFragment mOperationModeFragment = new OperationModeFragment();
    private ConstantModeFragment mConstantModeFragment = null;
    private SchedulerModeFragment mSchedulerModeFragment = new SchedulerModeFragment();

    private static final String LOG_TAG = "SynthControlMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");

        setContentView(R.layout.activity_main);
        ModesTitleArray = getResources().getStringArray(R.array.OperationModes);

        mFragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            Log.d(LOG_TAG, "Rebuilding fragments");
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, mOperationModeFragment);
            fragmentTransaction.commit();
            mFragmentManager.executePendingTransactions();
        }

        startService(new Intent(this, BoardManagerService.class).putExtra("Text", "Create title"));

        /*binding the BoardManagerService */
        Intent intent = new Intent(this, BoardManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (mBound) {
            mService.changeNotificationText("Fucking bound!");
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        /*Creating constant mode fragment*/
        Bundle bundle = new Bundle();
        ReferenceContainer fragmentData = new ReferenceContainer(mService, mBinder);
        bundle.putSerializable(TAG_REF_DATA, fragmentData);
        mConstantModeFragment = new ConstantModeFragment();
        mConstantModeFragment.setArguments(bundle);
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Saving instance");
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "On fucking stop acts!");
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onModeSelected(int currentIndex) {
        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();
        if (currentIndex == 0) {
            fragmentTransaction.replace(R.id.fragment_container, mConstantModeFragment);
        } else if (currentIndex == 1) {
            fragmentTransaction.replace(R.id.fragment_container, mSchedulerModeFragment);
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        getFragmentManager().executePendingTransactions();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mBinder = (BoardManagerService.LocalBinder) service;
            mService = mBinder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public BoardManagerService getService() {
        return mService;
    }
}