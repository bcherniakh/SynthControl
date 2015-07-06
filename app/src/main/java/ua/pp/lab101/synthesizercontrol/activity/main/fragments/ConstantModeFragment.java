package ua.pp.lab101.synthesizercontrol.activity.main.fragments;

import android.app.Activity;
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
    private EditText mFrequencyValue;

    public ConstantModeFragment() {
    }

    /*Fragment lifecycle methods */

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_constant_mode, container, false);
        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPowerBtn = (ToggleButton) getActivity().findViewById(R.id.powerBtn);
        mFrequencyValue = (EditText) getActivity().findViewById(R.id.frequencyValue);
        if (mPowerBtn != null) {
            mPowerBtn.setOnClickListener(new View.OnClickListener() {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        mService = getService();
        if (mService != null) {
            ServiceStatus currentStatus = mService.getCurrentStatus();
            if (currentStatus.equals(ServiceStatus.CONSTANT_MODE)) {
                mFrequencyValue.setText(String.valueOf(mService.getCurrentFrequency()));
                mFrequencyValue.setEnabled(false);
                mPowerBtn.setChecked(true);
            } else {
                mService.stopAnyWorkingTask();
                mFrequencyValue.setText(String.valueOf(0.0));
                mFrequencyValue.setEnabled(true);
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

    /*Main logic methods*/
    public void buttonSendPressed() {
        if (mService == null) {
            mService = getService();
        }

        if (mPowerBtn.isChecked()) {
            double frequencyValue = 0;
            try {
                frequencyValue = Double.parseDouble(mFrequencyValue.getText().toString());
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
            mFrequencyValue.setEnabled(false);
        } else {
            //stopping service
            Log.i(LOG_TAG, "Button toggled off");
            mFrequencyValue.setEnabled(true);
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
