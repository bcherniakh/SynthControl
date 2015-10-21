package ua.pp.lab101.synthesizercontrol.activity.main.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.math.RoundingMode;
import java.math.BigDecimal;

import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.main.IServiceDistributor;
import ua.pp.lab101.synthesizercontrol.service.BoardManagerService;
import ua.pp.lab101.synthesizercontrol.service.ServiceStatus;
import ua.pp.lab101.synthesizercontrol.service.task.Task;

/**
 * Fragment that presents Constant frequency operation mode.
 * User just sets frequency and starts the synthesizer.
 * The fragment does not shuts down the synthesizer when loses focus.
 */
public class ConstantModeFragment extends Fragment {
    private BoardManagerService mService;
    private boolean mBound;

    /*Constants*/
    private static final String LOG_TAG = "SControlConstant";

    /*View elements: */
    private ToggleButton mPowerBtn = null;
    private EditText mFrequencyValueEt;

    private String mFrequencyValueData;

    public ConstantModeFragment() {
    }

    /*Fragment lifecycle methods */

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_constant_mode, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPowerBtn = (ToggleButton) getActivity().findViewById(R.id.powerBtn);
        mFrequencyValueEt = (EditText) getActivity().findViewById(R.id.frequencyValue);
        if (mPowerBtn != null) {
            mPowerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonSendPressed();
                }
            });
        }

        if (mFrequencyValueEt != null) {
            mFrequencyValueEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (mFrequencyValueEt.isFocused()) {
                        mFrequencyValueData = mFrequencyValueEt.getText().toString();
                        mFrequencyValueEt.getText().clear();
                    } else {
                        if (mFrequencyValueEt.getText().toString().isEmpty()) {
                            mFrequencyValueEt.setText(mFrequencyValueData);
                        }
                    }
                }
            });

            mFrequencyValueEt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFrequencyValueEt.clearFocus();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mService = getService();
        if (mService != null) {
            ServiceStatus currentStatus = mService.getCurrentStatus();
            if (currentStatus.equals(ServiceStatus.CONSTANT_MODE)) {
                mFrequencyValueEt.setText(String.valueOf(mService.getCurrentFrequency()));
                mFrequencyValueEt.setEnabled(false);
                mPowerBtn.setChecked(true);
            } else {
                mService.stopAnyWorkingTask();
                mFrequencyValueEt.setText(String.valueOf(0.0));
                mFrequencyValueEt.setEnabled(true);
                mPowerBtn.setChecked(false);
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /*Main logic methods*/
    public void buttonSendPressed() {
        if (mService == null) {
            mService = getService();
        }

        if (mPowerBtn.isChecked()) {
            double frequencyValue = 0;
            try {
                frequencyValue = Double.parseDouble(mFrequencyValueEt.getText().toString());
                frequencyValue = new BigDecimal(frequencyValue).setScale(3, RoundingMode.HALF_EVEN).doubleValue();
            } catch (Exception parseException) {
                Log.e(LOG_TAG, "Parse double error occurred");
                showToast(getString(R.string.const_msg_frequency_input_err));
                mPowerBtn.setChecked(false);
                return;
            }

            if ((frequencyValue < 35) || (frequencyValue > 4400)) {
                showToast(getString(R.string.const_msg_frequency_range_err));
                mPowerBtn.setChecked(false);
                return;
            }

            if (mService == null) {
                showToast("Service is dead!");
                mPowerBtn.setChecked(false);
                return;
            }

            if (!mService.isDeviceConnected()) {
                showToast(getString(R.string.const_msg_no_device));
                mPowerBtn.setChecked(false);
                return;
            }

            Log.i(LOG_TAG, "Value to be set: " + Double.toString(frequencyValue) + " MHz");
            Task task = new Task(frequencyValue);
            mService.performTask(task);
            mFrequencyValueEt.setEnabled(false);
        } else {
            //stopping service
            Log.i(LOG_TAG, "Button toggled off");
            mFrequencyValueEt.setEnabled(true);
            mService.stopAnyWorkingTask();
        }
    }

    private BoardManagerService getService(){
        BoardManagerService service = null;
        try {
            IServiceDistributor serviceDistributor = (IServiceDistributor) getActivity();
            service = serviceDistributor.getService();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + "must implement OnFragmentInteractionListener");
        }
        return service;
    }

    private void showToast(String textToShow) {
        Toast toast = Toast.makeText(getActivity(),
                textToShow,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
