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

import ua.pp.lab101.synthesizercontrol.activity.additem.AddItemToScheduleActivity;
import ua.pp.lab101.synthesizercontrol.R;

import static android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class SchedulerModeFragment extends Fragment {

    private static final String LOG_TAG = "Me schedulero";
    private Button mAddBtn = null;
    private static final int CM_DELETE_ID = 1;
    private static final int CM_EDIT_ID = 2;

    // atribute names for map
    public static final String ATTRIBUTE_FREQUENCY = "frequency";
    public static final String ATTRIBUTE_TIME = "time";

    private ListView mScheduleLv;
    private SimpleAdapter mScheduleListAdapter;
    private ArrayList<Map<String, Object>> mScheduleData = new ArrayList<Map<String, Object>>();;
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
        return inflater.inflate(R.layout.schedule_mode, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAddBtn =  (Button) getActivity().findViewById(R.id.addItemBtn);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddItemClick();
            }
        });
        String[] from = { ATTRIBUTE_FREQUENCY, ATTRIBUTE_TIME };
        int[] to = { R.id.scheduleFrequencyText, R.id.scheduleTimeText };
        mScheduleListAdapter = new SimpleAdapter(getActivity(), mScheduleData, R.layout.schedule_mode_list_item, from, to);
        mScheduleLv = (ListView) getActivity().findViewById(R.id.scheduleList);
        View v = getActivity().getLayoutInflater().inflate(R.layout.schedule_mode_list_header, null);
        mScheduleLv.addHeaderView(v, "", false);
        mScheduleLv.setAdapter(mScheduleListAdapter);
        registerForContextMenu(mScheduleLv);
    }

    public void onAddItemClick() {
        Intent intent = new Intent(getActivity(), AddItemToScheduleActivity.class);
        startActivityForResult(intent, 1);
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
        } if (item.getItemId() == CM_EDIT_ID) {
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
            mChamgeIndex = acmi.position - 1;
            Intent intent = new Intent(getActivity(), AddItemToScheduleActivity.class);
            intent.putExtra(AddItemToScheduleActivity.RUN_TYPE_ID, AddItemToScheduleActivity.EDIT_RUN);
            double frequency = Double.valueOf(mScheduleData.get(mChamgeIndex).get(ATTRIBUTE_FREQUENCY).toString());
            int time = Integer.valueOf(mScheduleData.get(mChamgeIndex).get(ATTRIBUTE_TIME).toString());
            intent.putExtra(ATTRIBUTE_FREQUENCY,frequency);
            intent.putExtra(ATTRIBUTE_TIME, time);
            startActivityForResult(intent, 1);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        Log.d(LOG_TAG, "got the result");
        int runType = data.getIntExtra(AddItemToScheduleActivity.RUN_TYPE_ID, AddItemToScheduleActivity.ADD_RUN);
        String frequency = String.valueOf(data.getDoubleExtra(ATTRIBUTE_FREQUENCY, 0.0));
        String time = String.valueOf(data.getIntExtra(ATTRIBUTE_TIME, 0));
        if (runType == AddItemToScheduleActivity.ADD_RUN) {
            addDataToList(frequency, time);
        } else if (runType == AddItemToScheduleActivity.EDIT_RUN) {
            editDataInList(frequency, time);
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

    private void addDataToList(String frequency, String time){
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