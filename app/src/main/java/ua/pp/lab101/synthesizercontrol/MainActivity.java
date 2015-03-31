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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, mOperationModeFragment);
            fragmentTransaction.commit();
            mFragmentManager.executePendingTransactions();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);
        this.registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
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

