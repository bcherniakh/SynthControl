package ua.pp.lab101.synthesizercontrol.activity.main;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.main.fragments.ConstantModeFragment;
import ua.pp.lab101.synthesizercontrol.activity.main.fragments.FrequencyScanModeFragment;
import ua.pp.lab101.synthesizercontrol.activity.main.fragments.OperationModeFragment;
import ua.pp.lab101.synthesizercontrol.activity.main.fragments.SchedulerModeFragment;
import ua.pp.lab101.synthesizercontrol.service.BoardManagerService;
import ua.pp.lab101.synthesizercontrol.service.ServiceStatus;

public class MainActivity extends AppCompatActivity
        implements OperationModeFragment.OperationModeListener, IServiceDistributor{
    /*Service members*/
    private BoardManagerService mService;
    private BoardManagerService.BoardManagerBinder mBinder;
    private boolean mBound;

    public static String[] ModesTitleArray;
    public static final int UNSELECTED = -1;

    private FragmentManager mFragmentManager;
    private final OperationModeFragment mOperationModeFragment = new OperationModeFragment();
    private ConstantModeFragment mConstantModeFragment = new ConstantModeFragment();
    private SchedulerModeFragment mSchedulerModeFragment = new SchedulerModeFragment();
    private FrequencyScanModeFragment mFrequencyScanFragment = new FrequencyScanModeFragment();
    private ActionBar mMainActionBar;

    private static final String LOG_TAG = "SControlMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");

        setContentView(R.layout.activity_main);
        ModesTitleArray = getResources().getStringArray(R.array.operation_modes);
        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            Log.d(LOG_TAG, "Rebuilding fragments");
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, mOperationModeFragment);
            fragmentTransaction.commit();
            mFragmentManager.executePendingTransactions();
        }

        startService(new Intent(this, BoardManagerService.class));


        //Making and enabling the custom layout for ActionBar to display current frequency and state
        mMainActionBar = getSupportActionBar();
        mMainActionBar.setDisplayShowHomeEnabled(false);
        mMainActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.action_bar_main, null);
        mMainActionBar.setCustomView(mCustomView);
        mMainActionBar.setDisplayShowCustomEnabled(true);
        /*binding the BoardManagerService */
        Intent intent = new Intent(this, BoardManagerService.class);
        if (!mBound) {
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilterDeviceUnplugged = new IntentFilter(BoardManagerService.INTENT_DEVICE_UNPLUGGED);
        registerReceiver(mDeviceUnpluggedReceiver, intentFilterDeviceUnplugged);
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause");
        unregisterReceiver(mDeviceUnpluggedReceiver);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(LOG_TAG, "Saving instance");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "On stop acts!");
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }
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
        int id = item.getItemId();
        if (id == R.id.action_stop_service) {
            if (mBound) {
            unbindService(mConnection);
            mBound = false;
            }
            Intent intent = new Intent(this, BoardManagerService.class);
            stopService(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onModeSelected(int currentIndex) {
        if (!mBound) {
            return;
        }

        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();

        if (!mService.isDeviceConnected()) {
            if (currentIndex == -1) {
                fragmentTransaction.replace(R.id.fragment_container, mOperationModeFragment);
            } else {
//                showDeviceUnpluggedDialog();
//                Log.d(LOG_TAG, "No device present");
//                return;
/*                TODO  Fix this*/
                fragmentTransaction.replace(R.id.fragment_container, mOperationModeFragment);
            }
        }

        if (currentIndex == 0) {
            fragmentTransaction.replace(R.id.fragment_container, mConstantModeFragment);
        } else if (currentIndex == 1) {
            fragmentTransaction.replace(R.id.fragment_container, mSchedulerModeFragment);
        } else if (currentIndex == 2) {
            fragmentTransaction.replace(R.id.fragment_container, mFrequencyScanFragment);
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
            mBinder = (BoardManagerService.BoardManagerBinder) service;
            mService = mBinder.getService();
            mBound = true;
            ServiceStatus currentStatus = mService.getCurrentStatus();
            switch (currentStatus) {
                case CONSTANT_MODE:
                    onModeSelected(0);
                    break;
                case SCHEDULE_MODE:
                    onModeSelected(1);
                    break;
                case FREQUENCY_SCAN_MODE:
                    onModeSelected(2);
                    break;
            }
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

    private final BroadcastReceiver mDeviceUnpluggedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onModeSelected(UNSELECTED);
            showDeviceUnpluggedDialog();
        }
    };

    private void showToast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showDeviceUnpluggedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.main_dialog_device_unplugged_title);
        builder.setMessage(getString(R.string.main_dialog_device_unplugged_message));
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.main_dialog_device_unplugged_cancel_btn,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}