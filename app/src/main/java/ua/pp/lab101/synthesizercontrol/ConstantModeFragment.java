package ua.pp.lab101.synthesizercontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import ua.pp.lab101.synthesizercontrol.ADRegisters.ADBoardController;

/**
 * Fragment that presents Constant frequency operation mode.
 * User just sets frequency and starts the synthesizer.
 * The fragment does not shuts down the synthesizer when loses focus.
 */
public class ConstantModeFragment extends Fragment {
    /*Constants*/
    private static final String TAG = "SynthesizerControl";

    /*View elements: */
    ToggleButton mToggleBtn = null;
    EditText mFrequencyValue;

    /*System elements. Context and usbdevice*/
    static Context DeviceConstantModeContext;
    D2xxManager ftdid2xx = null;
    FT_Device ftDev = null;
    int DevCount = -1;

    /*Logic workflow variables*/
    public ConstantModeFragment() {
        // Required empty public constructor
    }

    /*Overloaded consrtuctor. This anit good but we need contxt and FTDManager to control
    * the device*/
    public ConstantModeFragment(Context parentContext, D2xxManager ftdid2xx) {
        this.ftdid2xx = ftdid2xx;
        DeviceConstantModeContext = parentContext;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.constant_mode, container, false);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        DeviceConstantModeContext.getApplicationContext().registerReceiver(mUsbPlugEvents, filter);
        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToggleBtn = (ToggleButton) getActivity().findViewById(R.id.applyBtn);
        mFrequencyValue = (EditText) getActivity().findViewById(R.id.frequencyValue);
        if (mToggleBtn != null) {
            mToggleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonSendPressed();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        DevCount = -1;
        ConnectFunction();
    }

    @Override
    public void onStop() {
        if (ftDev != null && true == ftDev.isOpen()) {
            ftDev.close();
        }
        super.onStop();
    }

    public void ConnectFunction() {
        int openIndex = 0;
        if (DevCount > 0)
            return;

        DevCount = ftdid2xx.createDeviceInfoList(DeviceConstantModeContext);
        if (DevCount > 0) {
            ftDev = ftdid2xx.openByIndex(DeviceConstantModeContext, openIndex);

            if (ftDev == null) {
                Toast.makeText(DeviceConstantModeContext, "ftDev == null", Toast.LENGTH_LONG).show();
                return;
            }

            if (true == ftDev.isOpen()) {
                Toast.makeText(DeviceConstantModeContext,
                        "devCount:" + DevCount + " open index:" + openIndex,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DeviceConstantModeContext,
                        "Need to get permission!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("j2xx", "DevCount <= 0");
        }
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//
//    }

    /*Main logic methods*/
    public void buttonSendPressed() {
        if (ftDev != null) {
            ADBoardController adf = new ADBoardController();
            if (mToggleBtn.isChecked()) {
                ftDev.resetDevice();
                ftDev.setBaudRate(115200);
                ftDev.setLatencyTimer((byte) 16);
                ftDev.setBitMode((byte) 0x0f, D2xxManager.FT_BITMODE_ASYNC_BITBANG);
                Log.i(TAG, "Button toggled on!");
                double frequencyValue = Double.parseDouble(mFrequencyValue.getText().toString());
                Log.i(TAG, "Value to be set: " + Double.toString(frequencyValue) + " MHz");
                writeData(adf.geiInitianCommanSequence());
                writeData(adf.setFrequency(frequencyValue));
                writeData(adf.turnOnDevice());
            } else {
                writeData(adf.turnOffTheDevice());
                Log.i(TAG, "Button toggled off");
            }
        } else {
            Log.e(TAG, "No device present. Bitches!");
            mToggleBtn.setChecked(false);
        }


    }

    private void writeData(byte[][] commands) {
        for (int i = 0; i < commands.length; i++) {
            int result = writeDataToRegister(commands[i]);
            Log.i(TAG, Integer.toString(result) + " bytes wrote to reg" + Integer.toString(i));
        }
    }

    private int writeDataToRegister(byte[] data) {
        int result = ftDev.write(data);
        return result;
    }

    /*Broadcast receiver realization for hotplug realization*/
    private BroadcastReceiver mUsbPlugEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                try {
                    int devCount = 0;
                    int openIndex = 0;
                    devCount = ftdid2xx.createDeviceInfoList(DeviceConstantModeContext);
                    Log.i("FtdiModeControl",
                            "Device number = " + Integer.toString(devCount));
                    ftDev = ftdid2xx.openByIndex(DeviceConstantModeContext, openIndex);
                    Toast toast = Toast.makeText(DeviceConstantModeContext,
                            "Device connected",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    String s = e.getMessage();
                    if (s != null) {
                        //Error_Information.setText(s);
                    }
                    e.printStackTrace();
                }
            }
        }
    };

}
