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
    private ToggleButton mToggleBtn = null;
    private EditText mFrequencyValue;

    /*System elements. Context and usbdevice*/
    private static Context mDeviceConstantModeContext;
    private D2xxManager mFtdid2xx = null;
    private FT_Device mFtDev = null;
    private int mDevCount = -1;

    /**/
    private ADBoardController adf;
    /*Logic workflow variables*/

    public ConstantModeFragment() {
        // Required empty public constructor
    }

    /*Overloaded constructor. This anit good but we need context and FTDManager to control
    * the device*/
    public ConstantModeFragment(Context parentContext, D2xxManager ftdid2xx) {
        this.mFtdid2xx = ftdid2xx;
        mDeviceConstantModeContext = parentContext;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.constant_mode, container, false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mDeviceConstantModeContext.getApplicationContext().registerReceiver(mUsbPlugEvents, filter);
        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToggleBtn = (ToggleButton) getActivity().findViewById(R.id.applyBtn);
        mFrequencyValue = (EditText) getActivity().findViewById(R.id.frequencyValue);
//        mFrequencyValue.setFilters(new InputFilter[] {
//                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
//                    int beforeDecimal = 4, afterDecimal = 3;
//
//                    @Override
//                    public CharSequence filter(CharSequence source, int start, int end,
//                                               Spanned dest, int dstart, int dend) {
//                        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
//
//                        String ds = String.valueOf(dfs.getDecimalSeparator());
//                        String temp = mFrequencyValue.getText() + source.toString();
//
//                        if (temp.equals(ds)) {
//                            return "0".concat(ds);
//                        }
//                        else if (temp.toString().indexOf(ds) == -1) {
//                            if (temp.length() > beforeDecimal) {
//                                return "";
//                            }
//                        } else {
//                            temp = temp.substring(temp.indexOf(ds) + 1);
//                            if (temp.length() > afterDecimal) {
//                                return "";
//                            }
//                        }
//
//                        return super.filter(source, start, end, dest, dstart, dend);
//                    }
//                }
//        });
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
        adf = ADBoardController.getInstance();
        mDevCount = -1;
        if (mFtdid2xx == null) {
            try {
                mFtdid2xx = D2xxManager.getInstance(mDeviceConstantModeContext);
            } catch (D2xxManager.D2xxException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed open FT245 device.");
            }
        }
        connectTheDevice();
    }

    @Override
    public void onStop() {
        if (mFtDev != null && mFtDev.isOpen()) {
            writeData(adf.turnOffTheDevice());
            mFtDev.close();
            mFtDev = null;
            mToggleBtn.setChecked(false);
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curDevCount", mDevCount);
    }

    public void connectTheDevice() {
        int openIndex = 0;
        if (mDevCount > 0)
            return;

        mDevCount = mFtdid2xx.createDeviceInfoList(mDeviceConstantModeContext);
        if (mDevCount > 0) {
            mFtDev = mFtdid2xx.openByIndex(mDeviceConstantModeContext, openIndex);

            if (mFtDev == null) {
                showToast(getString(R.string.const_msg_connection_err));
                return;
            }

            if (mFtDev.isOpen()) {
                mFtDev.resetDevice();
                mFtDev.setBaudRate(9600);
                mFtDev.setLatencyTimer((byte) 16);
                mFtDev.setBitMode((byte) 0x0f, D2xxManager.FT_BITMODE_ASYNC_BITBANG);
                writeData(adf.geiInitianCommanSequence());
                showToast(getString(R.string.const_msg_device_found));
            } else {
                showToast(getString(R.string.const_msg_usb_permission_err));
            }
        } else {
            Log.e(TAG, "mDevCount <= 0");
            showToast(getString(R.string.const_msg_connection_err));
        }
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//    }

//    @Override
//    public void onDetach() {
//        writeData(adf.turnOffTheDevice());
//        mFtDev.close();
//        mFtDev = null;
//        super.onDetach();
//
//    }

    /*Main logic methods*/
    public void buttonSendPressed() {
        if (mFtDev != null) {
            if (mToggleBtn.isChecked()) {
                double frequencyValue = 0;
                try {
                    frequencyValue = Double.parseDouble(mFrequencyValue.getText().toString());
                } catch (Exception parseException) {
                    Log.e(TAG, "Parse double error occurred");
                    showToast(getString(R.string.const_msg_frequency_input_err));
                    mToggleBtn.setChecked(false);
                    return;
                }

                if ((frequencyValue < 35) || (frequencyValue > 4400)) {
                    showToast(getString(R.string.const_msg_frequency_range_err));
                    mToggleBtn.setChecked(false);
                    return;
                }

                Log.i(TAG, "Value to be set: " + Double.toString(frequencyValue) + " MHz");
                writeData(adf.setFrequency(frequencyValue));
                writeData(adf.turnOnDevice());
            } else {
                writeData(adf.turnOffTheDevice());
                Log.i(TAG, "Button toggled off");
            }
        } else {
            Log.e(TAG, "No device present.");
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
        return mFtDev.write(data);
    }

    private void showToast(String textToShow) {
        Toast toast = Toast.makeText(mDeviceConstantModeContext,
                textToShow,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /*Broadcast receiver realization for hotplug realization*/
    private BroadcastReceiver mUsbPlugEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                try {
                    mDevCount = -1;
                    mFtDev = null;
                    showToast(getString(R.string.const_msg_device_disconnected));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
