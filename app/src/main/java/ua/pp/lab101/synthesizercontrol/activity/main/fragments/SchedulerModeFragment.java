package ua.pp.lab101.synthesizercontrol.activity.main.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.pp.lab101.synthesizercontrol.activity.accessory.AddItemToScheduleActivity;
import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.accessory.ReadScheduleActivity;
import ua.pp.lab101.synthesizercontrol.activity.accessory.WriteScheduleActivity;

import static android.widget.AdapterView.AdapterContextMenuInfo;

public class SchedulerModeFragment extends Fragment {

    private static final String LOG_TAG = "SchedulerFragment";
    private Button mAddBtn = null;
    private Button mReadBtn = null;
    private Button mWriteBtn = null;
    private static final int CM_DELETE_ID = 1;
    private static final int CM_EDIT_ID = 2;
    private static final int ADD_ITEM_RUN = 1;
    private static final int READ_FILE_RUN = 2;
    private static final int WRITE_FILE_RUN = 3;

    // atribute names for map
    public static final String ATTRIBUTE_FREQUENCY = "frequency";
    public static final String ATTRIBUTE_TIME = "time";

    public static final String ATTRIBUTE_FREQUENCY_ARRAY = "frequencyArray";
    public static final String ATTRIBUTE_TIME_ARRAY = "timeArray";

    private ListView mScheduleLv;
    private SimpleAdapter mScheduleListAdapter;
    private ArrayList<Map<String, Object>> mScheduleData = new ArrayList<Map<String, Object>>();
    ;
    private Map<String, Object> mScheduleEntry;
    //private View mListHeader;

    private int mChamgeIndex = 0;

    public SchedulerModeFragment() {
        // Required empty public constructor
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
        return inflater.inflate(R.layout.activity_schedule_mode, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAddBtn = (Button) getActivity().findViewById(R.id.addItemBtn);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddItemClick();
            }
        });
        mReadBtn = (Button) getActivity().findViewById(R.id.readFileBtn);
        mReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadBtnClick();
            }
        });
        mWriteBtn = (Button) getActivity().findViewById(R.id.writeFileBtn);
        mWriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onWriteBtnClick();
            }
        });
        String[] from = {ATTRIBUTE_FREQUENCY, ATTRIBUTE_TIME};
        int[] to = {R.id.scheduleFrequencyText, R.id.scheduleTimeText};
        mScheduleListAdapter = new SimpleAdapter(getActivity(), mScheduleData, R.layout.list_item_schedule_mode_task, from, to);
        mScheduleLv = (ListView) getActivity().findViewById(R.id.scheduleList);
        View v = getActivity().getLayoutInflater().inflate(R.layout.list_header_schedule_mode_task, null);
        mScheduleLv.addHeaderView(v, "", false);
        mScheduleLv.setAdapter(mScheduleListAdapter);
        registerForContextMenu(mScheduleLv);
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

        Intent intent = new Intent(getActivity(), WriteScheduleActivity.class);
        intent.putExtra(ATTRIBUTE_FREQUENCY_ARRAY, frequencyValues);
        intent.putExtra(ATTRIBUTE_TIME_ARRAY, timeValues);
        startActivityForResult(intent, WRITE_FILE_RUN);
    }

    private void onReadBtnClick() {
        Intent intent = new Intent(getActivity(), ReadScheduleActivity.class);
        startActivityForResult(intent, READ_FILE_RUN);
    }

    public void onAddItemClick() {
        Intent intent = new Intent(getActivity(), AddItemToScheduleActivity.class);
        startActivityForResult(intent, ADD_ITEM_RUN);
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
            mChamgeIndex = acmi.position - 1;
            Intent intent = new Intent(getActivity(), AddItemToScheduleActivity.class);
            intent.putExtra(AddItemToScheduleActivity.RUN_TYPE_ID, AddItemToScheduleActivity.EDIT_RUN);
            double frequency = Double.valueOf(mScheduleData.get(mChamgeIndex).get(ATTRIBUTE_FREQUENCY).toString());
            int time = Integer.valueOf(mScheduleData.get(mChamgeIndex).get(ATTRIBUTE_TIME).toString());
            intent.putExtra(ATTRIBUTE_FREQUENCY, frequency);
            intent.putExtra(ATTRIBUTE_TIME, time);
            startActivityForResult(intent, ADD_ITEM_RUN);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ITEM_RUN) {
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
        } else if (requestCode == READ_FILE_RUN) {
            if (resultCode != getActivity().RESULT_OK || data == null) {
                showToast("No data added");
                Log.d(LOG_TAG, "read file failed");
                return;
            }
            mScheduleData.clear();
            double[] frequency = data.getDoubleArrayExtra(ATTRIBUTE_FREQUENCY_ARRAY);
            int[] time = data.getIntArrayExtra(ATTRIBUTE_TIME_ARRAY);
            if (frequency.length != time.length) {
                Log.d(LOG_TAG, "Arrays length mismatch");
                return;
            }

            for (int i = 0; i < frequency.length; i++) {
                mScheduleEntry = new HashMap<String, Object>();
                mScheduleEntry.put(ATTRIBUTE_FREQUENCY, String.valueOf(frequency[i]));
                mScheduleEntry.put(ATTRIBUTE_TIME, String.valueOf(time[i]));
                mScheduleData.add(mScheduleEntry);
            }
            mScheduleListAdapter.notifyDataSetChanged();

        }
    }

    private void editDataInList(String frequency, String time) {
        mScheduleEntry = mScheduleData.get(mChamgeIndex);
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

}