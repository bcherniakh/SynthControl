package ua.pp.lab101.synthesizercontrol;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class SchedulerModeFragment extends Fragment {

    private Button mAddBtn = null;
    private static final int CM_DELETE_ID = 1;

    // atribute names for map
    final String ATTRIBUTE_FREQUENCY = "frequency";
    final String ATTRIBUTE_TIME = "time";

    ListView mScheduleLv;
    SimpleAdapter mScheduleListAdapter;
    ArrayList<Map<String, Object>> mScheduleData;
    Map<String, Object> mScheduleEntry;
    View mListHeader;

    public SchedulerModeFragment() {
        // Required empty public constructor
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
                onButtonClick();
            }
        });

        mScheduleData = new ArrayList<Map<String, Object>>();
            //mScheduleEntry = new HashMap<String, Object>();
            //mScheduleEntry.put(ATTRIBUTE_FREQUENCY, "Frequency in MHz");
            //mScheduleEntry.put(ATTRIBUTE_TIME, "Time in seconds");
            //mScheduleData.add(mScheduleEntry);


        String[] from = { ATTRIBUTE_FREQUENCY, ATTRIBUTE_TIME };
        int[] to = { R.id.scheduleFrequencyText, R.id.scheduleTimeText };
        mScheduleListAdapter = new SimpleAdapter(getActivity(), mScheduleData, R.layout.schedule_mode_list_item, from, to);
        mScheduleLv = (ListView) getActivity().findViewById(R.id.scheduleList);

        View v = getActivity().getLayoutInflater().inflate(R.layout.schedule_mode_list_header, null);
        mScheduleLv.addHeaderView(v);
        mScheduleLv.setAdapter(mScheduleListAdapter);
        registerForContextMenu(mScheduleLv);
    }

    public void onButtonClick() {
        Intent intent = new Intent(getActivity(), AddItemToScheduleActivity.class);
        startActivityForResult(intent, 1);
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0,
                getActivity().getResources().getString(R.string.scheduler_delete_item_menu_entry));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
            mScheduleData.remove(acmi.position - 1);
            mScheduleListAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        Log.i("TEST", "IT Heppends");
        String frequency = data.getStringExtra("frequency");
        String time = data.getStringExtra("time");
        addDataToList(frequency, time);
    }

    private void addDataToList(String frequency, String time){
        // создаем новый Map
        mScheduleEntry = new HashMap<String, Object>();
        mScheduleEntry.put(ATTRIBUTE_FREQUENCY, frequency);
        mScheduleEntry.put(ATTRIBUTE_TIME, time);
        // добавляем его в коллекцию
        mScheduleData.add(mScheduleEntry);
        // уведомляем, что данные изменились
        mScheduleListAdapter.notifyDataSetChanged();
    }



}
