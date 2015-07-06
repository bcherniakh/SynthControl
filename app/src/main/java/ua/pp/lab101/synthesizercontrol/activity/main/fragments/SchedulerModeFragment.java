package ua.pp.lab101.synthesizercontrol.activity.main.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.ReasonPhraseCatalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.pp.lab101.synthesizercontrol.activity.accessory.AddItemToScheduleActivity;
import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.accessory.ReadDataFromCSVFileActivity;
import ua.pp.lab101.synthesizercontrol.activity.accessory.WriteDataToCSVFileActivity;
import ua.pp.lab101.synthesizercontrol.activity.main.IServiceDistributor;
import ua.pp.lab101.synthesizercontrol.service.BoardManagerService;
import ua.pp.lab101.synthesizercontrol.service.ServiceStatus;
import ua.pp.lab101.synthesizercontrol.service.task.Task;

import static android.widget.AdapterView.AdapterContextMenuInfo;

public class SchedulerModeFragment extends Fragment {

    private static final String LOG_TAG = "SchedulerFragment";
    private Button mAddBtn = null;
    private Button mReadBtn = null;
    private Button mWriteBtn = null;
    private ToggleButton mRunBtn = null;
    private CheckBox mCycleCheckBox = null;
    private static final int CM_DELETE_ID = 1;
    private static final int CM_EDIT_ID = 2;
    private static final int ADD_ITEM_REQUEST = 1;
    private static final int READ_FILE_REQUEST = 2;
    private static final int WRITE_FILE_REQUEST = 3;



    // atribute names for map
    public static final String ATTRIBUTE_FREQUENCY = "frequency";
    public static final String ATTRIBUTE_TIME = "time";

    public static final String ATTRIBUTE_FREQUENCY_ARRAY = "frequencyArray";
    public static final String ATTRIBUTE_TIME_ARRAY = "timeArray";

    private ListView mScheduleLv;
    private SimpleAdapter mScheduleListAdapter;
    private ArrayList<Map<String, Object>> mScheduleData = new ArrayList<Map<String, Object>>();

    private Map<String, Object> mScheduleEntry;
    //private View mListHeader;

    private int mChangeIndex = 0;

    private BoardManagerService mService;

    public SchedulerModeFragment() {
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
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_schedule_mode, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAddBtn = (Button) getActivity().findViewById(R.id.scheduleAddItemBtn);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddItemClick();
            }
        });
        mReadBtn = (Button) getActivity().findViewById(R.id.scheduleReadFileBtn);
        mReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadBtnClick();
            }
        });
        mWriteBtn = (Button) getActivity().findViewById(R.id.scheduleWriteFileBtn);
        mWriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onWriteBtnClick();
            }
        });
        mRunBtn = (ToggleButton) getActivity().findViewById(R.id.scheduleApplyBtn);
        mRunBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRunButtonClicked();
            }
        });
        mCycleCheckBox = (CheckBox) getActivity().findViewById(R.id.scheduleCycleTaskCb);
        String[] from = {ATTRIBUTE_FREQUENCY, ATTRIBUTE_TIME};
        int[] to = {R.id.scheduleFrequencyText, R.id.scheduleTimeText};
        mScheduleListAdapter = new SimpleAdapter(getActivity(), mScheduleData, R.layout.list_item_schedule_mode_task, from, to);
        mScheduleLv = (ListView) getActivity().findViewById(R.id.scheduleTaskLv);
        View v = getActivity().getLayoutInflater().inflate(R.layout.list_header_schedule_mode_task, null);
        mScheduleLv.addHeaderView(v, "", false);
        mScheduleLv.setAdapter(mScheduleListAdapter);
        registerForContextMenu(mScheduleLv);
    }

    @Override
    public void onResume() {
        super.onResume();
        mService = getService();
        if (mService != null) {
            ServiceStatus currentStatus = mService.getCurrentStatus();
            if (currentStatus.equals(ServiceStatus.SCHEDULE_MODE)) {
                Task currentTask = mService.getCurrentTask();
                fillScheduleFromArrays(currentTask.getFrequencyArray(), currentTask.getTimeArray());
                setControlsDisabled();
                mRunBtn.setChecked(true);
            } else {
                mService.stopAnyWorkingTask();
                setControlsEnabled();
                mRunBtn.setChecked(false);
            }
        }
        IntentFilter intFilt = new IntentFilter(BoardManagerService.INTENT_TASK_DONE);
        getActivity().registerReceiver(mTaskFinishedReceiver, intFilt);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mTaskFinishedReceiver);
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

    private void onRunButtonClicked() {
        if (mService == null) {
            mService = getService();
        }

        if (mRunBtn.isChecked()) {

            if (mService == null) {
                showToast("Service is dead!");
                mRunBtn.setChecked(false);
                return;
            }

            if (!mService.isDeviceConnected()) {
                showToast(getString(R.string.const_msg_no_device));
                mRunBtn.setChecked(false);
                return;
            }

            if (mScheduleData.isEmpty()) {
                showToast(getString(R.string.scheduler_toast_err_schedule_empty));
                mRunBtn.setChecked(false);
                return;
            }

            Log.d(LOG_TAG, "Task performed");
            boolean cycle = mCycleCheckBox.isChecked();
            double[] frequency = getFrequencyValues();
            int[] time = getTimeValues();
            Task task = new Task(frequency, time, cycle);
            mService.performTask(task);
            setControlsDisabled();
        } else {
            //stopping service
            Log.d(LOG_TAG, "Button toggled off");
            setControlsEnabled();
            mService.stopAnyWorkingTask();
        }
    }

    private double[] getFrequencyValues() {
        double[] frequency = new double[mScheduleData.size()];
        for (int i = 0; i < mScheduleData.size(); i++) {
            frequency[i] = Double.parseDouble(mScheduleData.get(i).get(ATTRIBUTE_FREQUENCY).toString());
        }
        return frequency;
    }

    private int[] getTimeValues() {
        int[] time = new int[mScheduleData.size()];
        for (int i = 0; i < mScheduleData.size(); i++) {
            time[i] = Integer.parseInt(mScheduleData.get(i).get(ATTRIBUTE_TIME).toString());
        }
        return time;
    }

    private void setControlsDisabled() {
        mScheduleLv.setEnabled(false);
        mAddBtn.setEnabled(false);
        mReadBtn.setEnabled(false);
        mWriteBtn.setEnabled(false);
        mCycleCheckBox.setEnabled(false);
    }

    private void setControlsEnabled() {
        mScheduleLv.setEnabled(true);
        mAddBtn.setEnabled(true);
        mReadBtn.setEnabled(true);
        mWriteBtn.setEnabled(true);
        mCycleCheckBox.setEnabled(true);
    }

    private void onWriteBtnClick() {
        if (mScheduleData.isEmpty()) {
            showToast(getString(R.string.scheduler_toast_err_schedule_empty));
            return;
        }

        int arraySize = mScheduleData.size();
        String[] frequencyValues = new String[arraySize];
        String[] timeValues = new String[arraySize];
        for (int i = 0; i < arraySize; i++) {
            frequencyValues[i] = mScheduleData.get(i).get(ATTRIBUTE_FREQUENCY).toString();
            timeValues[i] = mScheduleData.get(i).get(ATTRIBUTE_TIME).toString();
        }

        Intent intent = new Intent(getActivity(), WriteDataToCSVFileActivity.class);
        intent.putExtra(WriteDataToCSVFileActivity.WRITE_FILE_TYPE_ID, WriteDataToCSVFileActivity.WRITE_SCHEDULE_FILE);
        intent.putExtra(ATTRIBUTE_FREQUENCY_ARRAY, frequencyValues);
        intent.putExtra(ATTRIBUTE_TIME_ARRAY, timeValues);
        startActivityForResult(intent, WRITE_FILE_REQUEST);
    }

    private void onReadBtnClick() {
        Intent intent = new Intent(getActivity(), ReadDataFromCSVFileActivity.class);
        intent.putExtra(ReadDataFromCSVFileActivity.READ_FILE_TYPE_ID, ReadDataFromCSVFileActivity.READ_SCHEDULE_FILE);
        startActivityForResult(intent, READ_FILE_REQUEST);
    }

    private void onAddItemClick() {
        Intent intent = new Intent(getActivity(), AddItemToScheduleActivity.class);
        startActivityForResult(intent, ADD_ITEM_REQUEST);
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, getString(R.string.scheduler_menu_entry_delete_item));
        menu.add(0, CM_EDIT_ID, 0, getString(R.string.scheduler_menu_entry_edit_item));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
            int index = acmi.position - 1;
            mScheduleData.remove(index);
            mScheduleListAdapter.notifyDataSetChanged();
            return true;
        }

        if (item.getItemId() == CM_EDIT_ID) {
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
            mChangeIndex = acmi.position - 1;
            Intent intent = new Intent(getActivity(), AddItemToScheduleActivity.class);
            intent.putExtra(AddItemToScheduleActivity.RUN_TYPE_ID, AddItemToScheduleActivity.EDIT_RUN);
            double frequency = Double.valueOf(mScheduleData.get(mChangeIndex).get(ATTRIBUTE_FREQUENCY).toString());
            int time = Integer.valueOf(mScheduleData.get(mChangeIndex).get(ATTRIBUTE_TIME).toString());
            intent.putExtra(ATTRIBUTE_FREQUENCY, frequency);
            intent.putExtra(ATTRIBUTE_TIME, time);
            startActivityForResult(intent, ADD_ITEM_REQUEST);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ITEM_REQUEST) {
            if (data == null) {
                return;
            }
            Log.d(LOG_TAG, "got the result from additem");
            int runType = data.getIntExtra(AddItemToScheduleActivity.RUN_TYPE_ID, AddItemToScheduleActivity.ADD_RUN);
            String frequency = String.valueOf(data.getDoubleExtra(ATTRIBUTE_FREQUENCY, 0.0));
            String time = String.valueOf(data.getIntExtra(ATTRIBUTE_TIME, 0));
            if (runType == AddItemToScheduleActivity.ADD_RUN) {
                addDataToList(frequency, time);
            } else if (runType == AddItemToScheduleActivity.EDIT_RUN) {
                editDataInList(frequency, time);
            }
        } else if (requestCode == READ_FILE_REQUEST) {
            if (resultCode != getActivity().RESULT_OK || data == null) {
                showToast("No data added");
                Log.d(LOG_TAG, "read file failed");
                return;
            }

            double[] frequency = data.getDoubleArrayExtra(ATTRIBUTE_FREQUENCY_ARRAY);
            int[] time = data.getIntArrayExtra(ATTRIBUTE_TIME_ARRAY);
            fillScheduleFromArrays(frequency, time);

        }
    }

    private void fillScheduleFromArrays(double[] frequency, int[] time) {
        if (frequency.length != time.length) {
            Log.d(LOG_TAG, "Arrays length mismatch");
            return;
        }

        mScheduleData.clear();
        for (int i = 0; i < frequency.length; i++) {
            mScheduleEntry = new HashMap<String, Object>();
            mScheduleEntry.put(ATTRIBUTE_FREQUENCY, String.valueOf(frequency[i]));
            mScheduleEntry.put(ATTRIBUTE_TIME, String.valueOf(time[i]));
            mScheduleData.add(mScheduleEntry);
        }
        mScheduleListAdapter.notifyDataSetChanged();
    }

    private void editDataInList(String frequency, String time) {
        mScheduleEntry = mScheduleData.get(mChangeIndex);
        mScheduleEntry.remove(ATTRIBUTE_FREQUENCY);
        mScheduleEntry.remove(ATTRIBUTE_TIME);
        mScheduleEntry.put(ATTRIBUTE_FREQUENCY, frequency);
        mScheduleEntry.put(ATTRIBUTE_TIME, time);
        mScheduleListAdapter.notifyDataSetChanged();
    }

    private void addDataToList(String frequency, String time) {
        mScheduleEntry = new HashMap<String, Object>();
        mScheduleEntry.put(ATTRIBUTE_FREQUENCY, frequency);
        mScheduleEntry.put(ATTRIBUTE_TIME, time);
        mScheduleData.add(mScheduleEntry);
        mScheduleListAdapter.notifyDataSetChanged();
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private final BroadcastReceiver mTaskFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mRunBtn.setChecked(false);
            setControlsEnabled();
            showToast(getString(R.string.schedule_toast_task_done));
        }
    };
}