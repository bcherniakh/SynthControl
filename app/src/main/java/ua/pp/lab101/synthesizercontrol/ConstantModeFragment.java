package ua.pp.lab101.synthesizercontrol;

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

import ua.pp.lab101.synthesizercontrol.service.BoardManagerService;
import ua.pp.lab101.synthesizercontrol.service.task.Task;

/**
 * Fragment that presents Constant frequency operation mode.
 * User just sets frequency and starts the synthesizer.
 * The fragment does not shuts down the synthesizer when loses focus.
 */
public class ConstantModeFragment extends Fragment {

    /*Service members*/
    private IServiceDistributor mServiceDistributor;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.constant_mode, container, false);
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
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mServiceDistributor = (IServiceDistributor) activity;
            mService = mServiceDistributor.getService();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /*Main logic methods*/
    public void buttonSendPressed() {

        if (mPowerBtn.isChecked()) {
            double frequencyValue = 0;
            try {
                frequencyValue = Double.parseDouble(mFrequencyValue.getText().toString());
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
                showToast(getString(R.string.const_msg_frequency_range_err));
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
            mService.shutdownDevice();
        }
    }

    private void showToast(String textToShow) {
        Toast toast = Toast.makeText(getActivity(),
                textToShow,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
