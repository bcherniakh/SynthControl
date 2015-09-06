package ua.pp.lab101.synthesizercontrol.activity.main.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.accessory.AddItemToFreqScanActivity;
import ua.pp.lab101.synthesizercontrol.activity.accessory.AddItemToScheduleActivity;
import ua.pp.lab101.synthesizercontrol.activity.accessory.ReadDataFromCSVFileActivity;
import ua.pp.lab101.synthesizercontrol.activity.accessory.WriteDataToCSVFileActivity;
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

    public static final String ATTRIBUTE_FROM_FREQUENCY_ARRAY = "fromFrequency";
    public static final String ATTRIBUTE_TO_FREQUENCY_ARRAY = "toFrequency";
    public static final String ATTRIBUTE_FREQUENCY_STEP_ARRAY = "frequencyStep";
    public static final String ATTRIBUTE_TIME_STEP_ARRAY = "timeStepArray";

    public static final int ADD_ITEM_REQUEST = 1488;
    private static final int READ_FILE_REQUEST = 2488;
    private static final int WRITE_FILE_REQUEST = 3488;

    private static final int CM_DELETE_ID = 1;
    private static final int CM_EDIT_ID = 2;

    //UI components
    private ToggleButton mApplyBtn;
    private Button mReadBtn;
    private Button mAddItemBtn;
    private Button mWriteBtn;
    private CheckBox mCycleCheckBox = null;

    private ListView mFreqScanLv;
    private SimpleAdapter mFreqScanListAdapter;
    private ArrayList<Map<String, Object>> mFreqScanData = new ArrayList<Map<String, Object>>();
    private int mChangeIndex = 0;

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
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_frequency_scan_mode, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mApplyBtn = (ToggleButton) getActivity().findViewById(R.id.freqScanApplyBtn);
        mApplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onApplyButtonClick();
            }
        });
        mAddItemBtn = (Button) getActivity().findViewById(R.id.freqScanAddItemBtn);
        mAddItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddItemButtonClick();
            }
        });
        mReadBtn = (Button) getActivity().findViewById(R.id.freqScanReadFileBtn);
        mReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadFileButtonClick();
            }
        });
        mWriteBtn = (Button) getActivity().findViewById(R.id.freqScanWriteFileBtn);
        mWriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onWriteFileButtonClick();
            }
        });
        mFreqScanLv = (ListView) getActivity().findViewById(R.id.freqScanDataLv);
        mCycleCheckBox = (CheckBox) getActivity().findViewById(R.id.freqScanCycleTaskCb);

        String[] from = {ATTRIBUTE_FROM_FREQUENCY, ATTRIBUTE_TO_FREQUENCY,
                ATTRIBUTE_FREQUENCY_STEP, ATTRIBUTE_TIME_STEP};
        int[] to = {R.id.freqScanListFromFrequencyText, R.id.freqScanListToFrequencyText,
        R.id.freqScanListFrequencyStepText, R.id.freqScanListTimeStepText};
        mFreqScanListAdapter = new SimpleAdapter(getActivity(), mFreqScanData, R.layout.list_item_frequency_scan_task,
                from, to);

        View v = getActivity().getLayoutInflater().inflate(R.layout.list_header_freq_scan_task, null);
        mFreqScanLv.addHeaderView(v, "", false);
        mFreqScanLv.setAdapter(mFreqScanListAdapter);
        registerForContextMenu(mFreqScanLv);

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
                mCycleCheckBox.setChecked(currentTask.getIsCycled());
                setControlsDisabled();
                mApplyBtn.setChecked(true);
            } else {
                mService.stopAnyWorkingTask();
                setControlsEnabled();
                mApplyBtn.setChecked(false);
            }
        }
        IntentFilter intentFilter = new IntentFilter(BoardManagerService.INTENT_TASK_DONE);
        getActivity().registerReceiver(mTaskFinishedReceiver, intentFilter);
    }

    private void fillUiFieldsFromTask(Task currentTask) {
        double[] fromFrequncy = currentTask.getStartFrequency();
        double[] toFrequency = currentTask.getFinishFrequency();
        double[] frequencyStep = currentTask.getFrequencyStep();
        int[] timeStep = currentTask.getTimeStep();

        if (fromFrequncy == null || toFrequency == null || frequencyStep == null || timeStep == null ) {
            Log.e(LOG_TAG, "Null pointer");
            return;
        }

        mFreqScanData.clear();

        for (int i = 0; i < fromFrequncy.length; i++) {
            Map<String, Object> newFreqScanListItem = new HashMap<String, Object>();
            newFreqScanListItem.put(ATTRIBUTE_FROM_FREQUENCY, fromFrequncy[i]);
            newFreqScanListItem.put(ATTRIBUTE_TO_FREQUENCY, toFrequency[i]);
            newFreqScanListItem.put(ATTRIBUTE_FREQUENCY_STEP, frequencyStep[i]);
            newFreqScanListItem.put(ATTRIBUTE_TIME_STEP,  ( (double) timeStep[i]) / 1000);
            mFreqScanData.add(newFreqScanListItem);
        }
        mFreqScanListAdapter.notifyDataSetChanged();
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, getString(R.string.freq_scan_menu_entry_delete_item));
        menu.add(0, CM_EDIT_ID, 0, getString(R.string.freq_scan_menu_entry_edit_item));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int index = acmi.position - 1;
            mFreqScanData.remove(index);
            mFreqScanListAdapter.notifyDataSetChanged();
            return true;
        }

        if (item.getItemId() == CM_EDIT_ID) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            mChangeIndex = acmi.position - 1;
            Intent intent = new Intent(getActivity(), AddItemToFreqScanActivity.class);
            intent.putExtra(AddItemToFreqScanActivity.RUN_TYPE_ID, AddItemToFreqScanActivity.EDIT_RUN);

            String fromFrequency = mFreqScanData.get(mChangeIndex).get(ATTRIBUTE_FROM_FREQUENCY).toString();
            String toFrequency = mFreqScanData.get(mChangeIndex).get(ATTRIBUTE_TO_FREQUENCY).toString();
            String frequencyStep = mFreqScanData.get(mChangeIndex).get(ATTRIBUTE_FREQUENCY_STEP).toString();
            String timeStep = mFreqScanData.get(mChangeIndex).get(ATTRIBUTE_TIME_STEP).toString();
            intent.putExtra(ATTRIBUTE_FROM_FREQUENCY, fromFrequency);
            intent.putExtra(ATTRIBUTE_TO_FREQUENCY, toFrequency);
            intent.putExtra(ATTRIBUTE_FREQUENCY_STEP, frequencyStep);
            intent.putExtra(ATTRIBUTE_TIME_STEP, timeStep);
            startActivityForResult(intent, ADD_ITEM_REQUEST);
        }
        return super.onContextItemSelected(item);
    }


    private void onApplyButtonClick() {
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

            if (mFreqScanData.isEmpty()) {
                showToast(getString(R.string.freq_scan_toast_err_schedule_empty));
                mApplyBtn.setChecked(false);
                return;
            }

            Log.d(LOG_TAG, "Task performed");
            boolean cycle = mCycleCheckBox.isChecked();
            double[] fromFrequency = getFromFrequencyValues();
            double[] toFrequency = getToFrequencyValues();
            double[] frequencyStep = getFrequencyStepValues();
            double[] timeStep = getTimeStepValues();
            Task task = new Task(fromFrequency, toFrequency, frequencyStep, timeStep, cycle);
            mService.performTask(task);
            setControlsDisabled();
        } else {
            //stopping service
            Log.d(LOG_TAG, "Button toggled off");
            setControlsEnabled();
            mService.stopAnyWorkingTask();
        }
    }

    private double[] getFromFrequencyValues() {
        double[] fromFrequency = new double[mFreqScanData.size()];
        for (int i = 0; i < mFreqScanData.size(); i++) {
            fromFrequency[i] = Double.parseDouble(mFreqScanData.get(i).get(ATTRIBUTE_FROM_FREQUENCY).toString());
        }
        return fromFrequency;
    }

    private double[] getToFrequencyValues() {
        double[] toFrequency = new double[mFreqScanData.size()];
        for (int i = 0; i < mFreqScanData.size(); i++) {
            toFrequency[i] = Double.parseDouble(mFreqScanData.get(i).get(ATTRIBUTE_TO_FREQUENCY).toString());
        }
        return toFrequency;
    }

    private double[] getFrequencyStepValues() {
        double[] frequencyStep = new double[mFreqScanData.size()];
        for (int i = 0; i < mFreqScanData.size(); i++) {
            frequencyStep[i] = Double.parseDouble(mFreqScanData.get(i).get(ATTRIBUTE_FREQUENCY_STEP).toString());
        }
        return frequencyStep;
    }

    private double[] getTimeStepValues() {
        double[] timeStep = new double[mFreqScanData.size()];
        for (int i = 0; i < mFreqScanData.size(); i++) {
            timeStep[i] = Double.parseDouble(mFreqScanData.get(i).get(ATTRIBUTE_TIME_STEP).toString());
        }
        return timeStep;
    }

    private void onAddItemButtonClick() {
        Intent intent = new Intent(getActivity(), AddItemToFreqScanActivity.class);
        startActivityForResult(intent, ADD_ITEM_REQUEST);
    }

    private void onReadFileButtonClick() {
        Intent intent = new Intent(getActivity(), ReadDataFromCSVFileActivity.class);
        intent.putExtra(ReadDataFromCSVFileActivity.READ_FILE_TYPE_ID, ReadDataFromCSVFileActivity.READ_FREQUENCY_SCAN_FILE);
        startActivityForResult(intent, READ_FILE_REQUEST);
    }

    private void onWriteFileButtonClick() {
        if (mFreqScanData.isEmpty()) {
            showToast(getString(R.string.freq_scan_toast_err_task_list_is_empty));
            return;
        }

        int arraySize = mFreqScanData.size();
        String[] fromFrequencyValues = new String[arraySize];
        String[] toFrequencyValues = new String[arraySize];
        String[] frequencyStepValues = new String[arraySize];
        String[] timeStepValues = new String[arraySize];
        for (int i = 0; i < arraySize; i++) {
            fromFrequencyValues[i] = mFreqScanData.get(i).get(ATTRIBUTE_FROM_FREQUENCY).toString();
            toFrequencyValues[i] = mFreqScanData.get(i).get(ATTRIBUTE_TO_FREQUENCY).toString();
            frequencyStepValues[i] = mFreqScanData.get(i).get(ATTRIBUTE_FREQUENCY_STEP).toString();
            timeStepValues[i] = mFreqScanData.get(i).get(ATTRIBUTE_TIME_STEP).toString();
        }

        Intent intent = new Intent(getActivity(), WriteDataToCSVFileActivity.class);
        intent.putExtra(WriteDataToCSVFileActivity.WRITE_FILE_TYPE_ID, WriteDataToCSVFileActivity.WRITE_FREQUENCY_SCAN_FILE);
        intent.putExtra(ATTRIBUTE_FROM_FREQUENCY_ARRAY, fromFrequencyValues);
        intent.putExtra(ATTRIBUTE_TO_FREQUENCY_ARRAY, toFrequencyValues);
        intent.putExtra(ATTRIBUTE_FREQUENCY_STEP_ARRAY, frequencyStepValues);
        intent.putExtra(ATTRIBUTE_TIME_STEP_ARRAY, timeStepValues);
        startActivityForResult(intent, WRITE_FILE_REQUEST);
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
        mFreqScanLv.setEnabled(false);
        mAddItemBtn.setEnabled(false);
        mReadBtn.setEnabled(false);
        mWriteBtn.setEnabled(false);
        mCycleCheckBox.setEnabled(false);
    }

    private void setControlsEnabled() {
        mFreqScanLv.setEnabled(true);
        mAddItemBtn.setEnabled(true);
        mReadBtn.setEnabled(true);
        mWriteBtn.setEnabled(true);
        mCycleCheckBox.setEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ITEM_REQUEST) {
            if (data == null) {
                Log.e(LOG_TAG, "No data from additem");
                return;
            }

            Log.d(LOG_TAG, "got the result from additem");
            int runType = data.getIntExtra(AddItemToFreqScanActivity.RUN_TYPE_ID, AddItemToFreqScanActivity.ADD_RUN);
            String fromFrequency = String.valueOf(data.getDoubleExtra(ATTRIBUTE_FROM_FREQUENCY, 0.0));
            String toFrequency = String.valueOf(data.getDoubleExtra(ATTRIBUTE_TO_FREQUENCY, 0.0));
            String frequencyStep = String.valueOf(data.getDoubleExtra(ATTRIBUTE_FREQUENCY_STEP, 0.0));
            String timeStep = String.valueOf(data.getDoubleExtra(ATTRIBUTE_TIME_STEP, 0.0));
            if (runType == AddItemToFreqScanActivity.ADD_RUN) {
                addDataToList(fromFrequency, toFrequency, frequencyStep, timeStep);
            } else if (runType == AddItemToScheduleActivity.EDIT_RUN) {
                editDataInList(fromFrequency, toFrequency, frequencyStep, timeStep);
            }
        } else if (requestCode == READ_FILE_REQUEST) {
            if (resultCode != getActivity().RESULT_OK || data == null) {
                showToast("No data added");
                Log.d(LOG_TAG, "read file failed");
                return;
            }
            double[] fromFrequency = data.getDoubleArrayExtra(ATTRIBUTE_FROM_FREQUENCY_ARRAY );
            double[] toFrequency = data.getDoubleArrayExtra(ATTRIBUTE_TO_FREQUENCY_ARRAY);
            double[] frequencyStep = data.getDoubleArrayExtra(ATTRIBUTE_FREQUENCY_STEP_ARRAY);
            double[] timeStep = data.getDoubleArrayExtra(ATTRIBUTE_TIME_STEP_ARRAY);
            fillTaskListFromArrays(fromFrequency, toFrequency, frequencyStep, timeStep);
        }
    }

    private void fillTaskListFromArrays(double[] fromFrequency , double[] toFrequency,
                                        double[] frequencyStep, double[] timeStep) {
        if (fromFrequency.length != toFrequency.length) {
            Log.d(LOG_TAG, "Arrays length mismatch");
            return;
        }

        mFreqScanData.clear();
        for (int i = 0; i < fromFrequency.length; i++) {
            Map<String, Object> newFreqScanListItem = new HashMap<String, Object>();
            newFreqScanListItem.put(ATTRIBUTE_FROM_FREQUENCY, String.valueOf(fromFrequency[i]));
            newFreqScanListItem.put(ATTRIBUTE_TO_FREQUENCY, String.valueOf(toFrequency[i]));
            newFreqScanListItem.put(ATTRIBUTE_FREQUENCY_STEP, String.valueOf(frequencyStep[i]));
            newFreqScanListItem.put(ATTRIBUTE_TIME_STEP, String.valueOf(timeStep[i]));
            mFreqScanData.add(newFreqScanListItem);
        }
        mFreqScanListAdapter.notifyDataSetChanged();
    }

    private void addDataToList(String fromFrequency, String toFrequency, String frequencyStep, String timeStep) {
        Map<String, Object> newFreqScanListItem = new HashMap<String, Object>();
        newFreqScanListItem.put(ATTRIBUTE_FROM_FREQUENCY, fromFrequency);
        newFreqScanListItem.put(ATTRIBUTE_TO_FREQUENCY, toFrequency);
        newFreqScanListItem.put(ATTRIBUTE_FREQUENCY_STEP, frequencyStep);
        newFreqScanListItem.put(ATTRIBUTE_TIME_STEP, timeStep);
        mFreqScanData.add(newFreqScanListItem);
        mFreqScanListAdapter.notifyDataSetChanged();
    }

    private void editDataInList(String fromFrequency, String toFrequency, String frequencyStep, String timeStep) {
        Map<String, Object> freqScanlListItem = mFreqScanData.get(mChangeIndex);
        freqScanlListItem.remove(ATTRIBUTE_FROM_FREQUENCY);
        freqScanlListItem.remove(ATTRIBUTE_TO_FREQUENCY);
        freqScanlListItem.remove(ATTRIBUTE_FREQUENCY_STEP);
        freqScanlListItem.remove(ATTRIBUTE_FROM_FREQUENCY);
        freqScanlListItem.put(ATTRIBUTE_FROM_FREQUENCY, fromFrequency);
        freqScanlListItem.put(ATTRIBUTE_TO_FREQUENCY, toFrequency);
        freqScanlListItem.put(ATTRIBUTE_FREQUENCY_STEP, frequencyStep);
        freqScanlListItem.put(ATTRIBUTE_TIME_STEP, timeStep);
        mFreqScanListAdapter.notifyDataSetChanged();
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