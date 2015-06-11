package ua.pp.lab101.synthesizercontrol.activity.main.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.main.IServiceDistributor;
import ua.pp.lab101.synthesizercontrol.service.BoardManagerService;
import ua.pp.lab101.synthesizercontrol.service.ServiceStatus;
import ua.pp.lab101.synthesizercontrol.service.task.Task;

public class FrequencyScanModeFragment extends Fragment {

    private static final String LOG_TAG = "Frequrncy scan";

    private EditText mFrequencyFromET;
    private EditText mFrequencyToTE;
    private EditText mFrequencyStepET;
    private EditText mTimeStepET;
    private CheckBox mCycledCheckBox;
    private ToggleButton mApplyBtn;

    private BoardManagerService mService;

    public FrequencyScanModeFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_frequency_scan_mode, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFrequencyFromET = (EditText) getActivity().findViewById(R.id.frequencyFromEditText);
        mFrequencyToTE = (EditText) getActivity().findViewById(R.id.frequencyToEditText);
        mFrequencyStepET = (EditText) getActivity().findViewById(R.id.frequencySrepEditText);
        mTimeStepET = (EditText) getActivity().findViewById(R.id.timeStepEditText);
        mCycledCheckBox = (CheckBox) getActivity().findViewById(R.id.freqCycleCb);
        mApplyBtn = (ToggleButton) getActivity().findViewById(R.id.freqApplyTask);

        if (mApplyBtn != null) {
            mApplyBtn.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                buttonApplyPressed();
                                             }
                                         }

            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mService = getService();
        if (mService != null) {
            ServiceStatus currentStatus = mService.getCurrentStatus();
            if (currentStatus.equals(ServiceStatus.FREQUENCY_SCAN_MODE)) {
                Task currentTask = mService.getCurrentTask();
                fillUiFieldsFromTask(currentTask);
                setControlsDisabled();
                mApplyBtn.setChecked(true);
            } else {
                mService.stopAnyWorkingTask();
                setControlsEnabled();
                mApplyBtn.setChecked(false);
            }
        }
        IntentFilter intFilt = new IntentFilter(BoardManagerService.INTENT_TASK_DONE);
        getActivity().registerReceiver(mTaskFinishedReceiver, intFilt);
    }

    private void fillUiFieldsFromTask(Task currentTask) {
        String frequencyFrom = String.valueOf(currentTask.getStartFrequency());
        String frequencyTo = String.valueOf(currentTask.getFinishFrequency());
        String frequencyStep = String.valueOf(currentTask.getFrequencyStep());
        double timeStepValue= (double) currentTask.getTimeStep();
        timeStepValue = timeStepValue / 1000;
        String timeStep = String.valueOf(timeStepValue);

        mFrequencyFromET.setText(frequencyFrom);
        mFrequencyToTE.setText(frequencyTo);
        mFrequencyStepET.setText(frequencyStep);
        mTimeStepET.setText(timeStep);
        mCycledCheckBox.setChecked(currentTask.getIsCycled());
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mTaskFinishedReceiver);
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void buttonApplyPressed() {
        if (mService == null) {
            mService = getService();
        }

        if (mApplyBtn.isChecked()) {

            if (mService == null) {
                showToast("Service is dead!");
                mApplyBtn.setChecked(false);
                return;
            }

            if (!mService.isDeviceConnected()) {
                showToast(getString(R.string.const_msg_no_device));
                mApplyBtn.setChecked(false);
                return;
            }

            double fromFrequency = Double.parseDouble(mFrequencyFromET.getText().toString());
            double toFrequency = Double.parseDouble(mFrequencyToTE.getText().toString());
            double frequencyStep = Double.parseDouble(mFrequencyStepET.getText().toString());
            double timeStep = Double.parseDouble(mTimeStepET.getText().toString());

            if (!checkAllValues(fromFrequency, toFrequency, frequencyStep, timeStep)){
                mApplyBtn.setChecked(false);
                return;
            }

            Log.d(LOG_TAG, "Task performed");
            fromFrequency = roundValue(fromFrequency);
            toFrequency = roundValue(toFrequency);
            frequencyStep = roundValue(frequencyStep);
            timeStep = roundValue(timeStep);
            int  roundedTime = (int) (timeStep * 1000);
            boolean cycle = mCycledCheckBox.isChecked();

            Task task = new Task(fromFrequency, toFrequency, frequencyStep, roundedTime, cycle);
            mService.performTask(task);
            setControlsDisabled();
        } else {
            //stopping service
            Log.d(LOG_TAG, "Button toggled off");
            setControlsEnabled();
            mService.stopAnyWorkingTask();
        }
    }

    private double roundValue(double value) {
        return new BigDecimal(value).setScale(3, RoundingMode.UP).doubleValue();
    }
    private boolean checkAllValues(double fromFrequency, double toFrequency, double frequencyStep,
                                   double timeStep) {

        if (!checkFrequencyBoundaries(fromFrequency)) {
            showToast(getString(R.string.freq_scan_toast_frequency_range));
            return false;
        }

        if (!checkFrequencyBoundaries(toFrequency)) {
            showToast(getString(R.string.freq_scan_toast_frequency_range));
            return false;
        }

        if (!checkStepValue(frequencyStep)) {
            showToast(getString(R.string.freq_scan_toast_step_incorrect));
            return false;
        }

        if (!checkTimeStepValue(timeStep)) {
            showToast(getString(R.string.freq_scan_toast_step_incorrect));
            return false;
        }

        if (!checkFromTo(fromFrequency, toFrequency)){
            showToast(getString(R.string.freq_scan_toast_from_to));
            return false;
        }
        return true;
    }

    private boolean checkFromTo(double fromFrequency, double toFrequency) {
        if (fromFrequency >= toFrequency) return false;
        return true;
    }

    private boolean checkTimeStepValue(double timeStep) {
        if ((timeStep >= 0.1) && (timeStep <= 3600)) return true;
        return false;
    }

    private boolean checkStepValue(double frequencyStep) {
        if ((frequencyStep >= 0.001) && (frequencyStep <= 1000)) return true;
        return false;
    }

    private boolean checkFrequencyBoundaries(double frequency) {
        if ((frequency < 35) || (frequency > 4400)) return false;
        return true;
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

    private void showToast(String text) {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void setControlsDisabled() {
        mFrequencyFromET.setEnabled(false);
        mFrequencyToTE.setEnabled(false);
        mFrequencyStepET.setEnabled(false);
        mTimeStepET.setEnabled(false);
        mCycledCheckBox.setEnabled(false);
    }

    private void setControlsEnabled() {
        mFrequencyFromET.setEnabled(true);
        mFrequencyToTE.setEnabled(true);
        mFrequencyStepET.setEnabled(true);
        mTimeStepET.setEnabled(true);
        mCycledCheckBox.setEnabled(true);
    }

    private final BroadcastReceiver mTaskFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mApplyBtn.setChecked(false);
            setControlsEnabled();
            showToast(getString(R.string.schedule_toast_task_done));
        }
    };
}
