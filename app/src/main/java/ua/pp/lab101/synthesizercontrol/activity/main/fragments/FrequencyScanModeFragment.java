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
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.accessory.AddItemToFreqScanActivity;
import ua.pp.lab101.synthesizercontrol.activity.accessory.AddItemToScheduleActivity;
import ua.pp.lab101.synthesizercontrol.activity.main.IServiceDistributor;
import ua.pp.lab101.synthesizercontrol.service.BoardManagerService;
import ua.pp.lab101.synthesizercontrol.service.ServiceStatus;
import ua.pp.lab101.synthesizercontrol.service.task.Task;

public class FrequencyScanModeFragment extends Fragment {

    private static final String LOG_TAG = "Frequency scan";

    // atribute names for map
    public static final String ATTRIBUTE_FROM_FREQUENCY = "from frequency";
    public static final String ATTRIBUTE_TO_FREQUENCY = "to frequency";
    public static final String ATTRIBUTE_FREQUENCY_STEP = "frequency step";
    public static final String ATTRIBUTE_TIME_STEP = "time step";

    public static final String ATTRIBUTE_FREQUENCY_ARRAY = "frequencyArray";
    public static final String ATTRIBUTE_TIME_ARRAY = "timeArray";

    private static final int ADD_ITEM_RUN = 1;
    private static final int READ_FILE_RUN = 2;
    private static final int WRITE_FILE_RUN = 3;
    //UI components
    ToggleButton mApplyBtn;
    Button mReadBtn;
    Button mAddItemBtn;

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
        return inflater.inflate(R.layout.fragment_frequency_scan_mode, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mApplyBtn = (ToggleButton) getActivity().findViewById(R.id.freqScanApplyBtn);
        mApplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyButtonClicked();
            }
        });
        mAddItemBtn = (Button) getActivity().findViewById(R.id.freqScanAdditemBtn);
        mAddItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemButtonClicked();
            }
        });
        mReadBtn = (Button) getActivity().findViewById(R.id.freqScanReadBtn);
        mReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readButtonClicked();
            }
        });

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
                //mApplyBtn.setChecked(true);
            } else {
                mService.stopAnyWorkingTask();
                setControlsEnabled();
                //mApplyBtn.setChecked(false);
            }
        }
        IntentFilter intFilt = new IntentFilter(BoardManagerService.INTENT_TASK_DONE);
        getActivity().registerReceiver(mTaskFinishedReceiver, intFilt);
    }

    private void fillUiFieldsFromTask(Task currentTask) {

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

    private void applyButtonClicked() {
        showToast("Apply me");
    }

    private void addItemButtonClicked() {
        Intent intent = new Intent(getActivity(), AddItemToFreqScanActivity.class);
        startActivityForResult(intent, ADD_ITEM_RUN);
    }

    private void readButtonClicked() {
        showToast("Read me bitch!1111111");
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

    }

    private void setControlsEnabled() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ITEM_RUN) {
            if (data == null) {
                Log.e(LOG_TAG, "No data from additem");
                return;
            }

            Log.d(LOG_TAG, "got the result from additem");
            int runType = data.getIntExtra(AddItemToFreqScanActivity.RUN_TYPE_ID, AddItemToFreqScanActivity.ADD_RUN);
            String fromFrequency = String.valueOf(data.getDoubleExtra(ATTRIBUTE_FROM_FREQUENCY, 0.0));
            String toFrequency = String.valueOf(data.getDoubleExtra(ATTRIBUTE_TO_FREQUENCY, 0.0));
            String frequencyStep = String.valueOf(data.getDoubleExtra(ATTRIBUTE_FREQUENCY_STEP, 0.0));
            String timeStep = String.valueOf(data.getIntExtra(ATTRIBUTE_TIME_STEP, 0));
            showToast("From frequency: " + fromFrequency + System.getProperty("line.separator") +
            "To frequency: " + toFrequency + System.getProperty("line.separator") +
            "Frequency step: " + frequencyStep + System.getProperty("line.separator") +
            "Time step: " + timeStep);

            if (runType == AddItemToScheduleActivity.ADD_RUN) {
                //addDataToList(frequency, time);
            } else if (runType == AddItemToScheduleActivity.EDIT_RUN) {
                //editDataInList(frequency, time);
            }
        } else if (requestCode == READ_FILE_RUN) {
            if (resultCode != getActivity().RESULT_OK || data == null) {
                showToast("No data added");
                Log.d(LOG_TAG, "read file failed");
                return;
            }
//            double[] frequency = data.getDoubleArrayExtra(ATTRIBUTE_FREQUENCY_ARRAY);
//            int[] time = data.getIntArrayExtra(ATTRIBUTE_TIME_ARRAY);
//            fillScheduleFromArrays(frequency, time);

        }
    }

    private final BroadcastReceiver mTaskFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //mApplyBtn.setChecked(false);
            setControlsEnabled();
            showToast(getString(R.string.schedule_toast_task_done));
        }
    };
}
