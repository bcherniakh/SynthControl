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
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, mOperationModeFragment);
        fragmentTransaction.commit();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);
        this.registerReceiver(mUsbReceiver, filter);


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
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
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
        String action = intent.getAction();
        if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
            /* TODO add some code to handle this intent in fragment */
        {
//            switch (currect_index)
//            {
//                case 4:
//                    ((OpenDeviceFragment)currentFragment).notifyUSBDeviceAttach(intent);
//                    break;
//                case 5:
//                    ((DeviceUARTFragment)currentFragment).notifyUSBDeviceAttach();
//                    break;
//                case 7:
//                    ((EEPROMFragment)currentFragment).notifyUSBDeviceAttach();
//                    break;
//                case 8:
//                    ((EEPROMUserAreaFragment)currentFragment).notifyUSBDeviceAttach();
//                    break;
//                default:
//                    break;
//            }
        }
    }
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String TAG = "FragL";
            String action = intent.getAction();
            if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
            {
                Log.i(TAG,"DETACHED...");
                /* TODO add some code to handle this intent in fragment */
//                if (currentFragment != null)
//                {
//                    switch (currect_index)
//                    {
//
//                        case 5:
//                            ((DeviceUARTFragment)currentFragment).notifyUSBDeviceDetach();
//                            break;
//                        default:
//                            //((DeviceInformationFragment)currentFragment).onStart();
//                            break;
//                    }
//                }
            }
        }
    };
}

