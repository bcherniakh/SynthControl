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

    public static final int ADD_ITEM_RUN = 1;
    private static final int READ_FILE_RUN = 2;
    private static final int WRITE_FILE_RUN = 3;

    private static final int CM_DELETE_ID = 1;
    private static final int CM_EDIT_ID = 2;

    //UI components
    private ToggleButton mApplyBtn;
    private Button mReadBtn;
    private Button mAddItemBtn;

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
        mFreqScanLv = (ListView) getActivity().findViewById(R.id.freqScanDataLv);

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
            startActivityForResult(intent, ADD_ITEM_RUN);
        }
        return super.onContextItemSelected(item);
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
            if (runType == AddItemToFreqScanActivity.ADD_RUN) {
                addDataToList(fromFrequency, toFrequency, frequencyStep, timeStep);
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

    private void addDataToList(String fromFrequency, String toFrequency, String frequencyStep, String timeStep) {
        Map<String, Object> newFreqScanListItem = new HashMap<String, Object>();
        newFreqScanListItem.put(ATTRIBUTE_FROM_FREQUENCY, fromFrequency);
        newFreqScanListItem.put(ATTRIBUTE_TO_FREQUENCY, toFrequency);
        newFreqScanListItem.put(ATTRIBUTE_FREQUENCY_STEP, frequencyStep);
        newFreqScanListItem.put(ATTRIBUTE_TIME_STEP, timeStep);
        mFreqScanData.add(newFreqScanListItem);
        mFreqScanListAdapter.notifyDataSetChanged();
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
