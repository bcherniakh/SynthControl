package ua.pp.lab101.synthesizercontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

public class MainActivity extends ActionBarActivity implements OperationModeFragment.OperationModeListener {

    public static String[] ModesTitleArray;
    public static final int UNSELECTED = -1;

    private FragmentManager mFragmentManager;
    private final OperationModeFragment mOperationModeFragment = new OperationModeFragment();
    private ConstantModeFragment mConstantModeFragment = null;
    private SchedulerModeFragment mSchedulerModeFragment = new SchedulerModeFragment();

    private FT_Device ftdiDevice = null;
    private D2xxManager ftdiManager = null;
    private static final String TAG = "SynthControl";

    private int mCurrentIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        try {
            ftdiManager = D2xxManager.getInstance(this);
            Log.i(TAG, "TFDevice found");
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
            Log.e(TAG, "No FTDevice conected");
        }

        mConstantModeFragment = new ConstantModeFragment(this, ftdiManager);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ModesTitleArray = getResources().getStringArray(R.array.OperationModes);
        mFragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, mOperationModeFragment);
        fragmentTransaction.commit();
        getFragmentManager().executePendingTransactions();

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt("CurrentIndex");
            FragmentTransaction fragmentTransaction1 = mFragmentManager
                    .beginTransaction();
            if (mCurrentIndex == 0 ) {
                fragmentTransaction1.replace(R.id.fragment_container, mConstantModeFragment);
            } else if (mCurrentIndex == 1) {
                fragmentTransaction1.replace(R.id.fragment_container, mSchedulerModeFragment);
            }
            fragmentTransaction1.addToBackStack(null);
            fragmentTransaction1.commit();
            getFragmentManager().executePendingTransactions();
        }


        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);
        this.registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public void onPause() {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.remove(mConstantModeFragment);
            fragmentTransaction.remove(mSchedulerModeFragment);
            fragmentTransaction.remove(mOperationModeFragment);
        fragmentTransaction.commit();
        getFragmentManager().executePendingTransactions();
        mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        super.onPause();
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("CurrentIndex", mCurrentIndex);
        super.onSaveInstanceState(savedInstanceState);
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
            mCurrentIndex = currentIndex;
            fragmentTransaction.replace(R.id.fragment_container, mConstantModeFragment);
        } else if (currentIndex == 1) {
            mCurrentIndex =currentIndex;
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
            mCurrentIndex = -1;
        } else {
            getFragmentManager().popBackStack();
            mCurrentIndex = -1;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
        {
            Log.i(TAG, "Device attached! I'a activity");
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
            {
                Log.i(TAG,"Device detached!");
            }
        }
    };

    public D2xxManager getFtdiManager() {
        return this.ftdiManager;
    }
}

