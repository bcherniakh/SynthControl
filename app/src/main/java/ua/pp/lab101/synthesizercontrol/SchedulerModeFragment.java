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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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

    // имена атрибутов для Map
    final String ATTRIBUTE_FREQUENCY = "frequency";
    final String ATTRIBUTE_TIME = "time";

    ListView lvSimple;
    SimpleAdapter sAdapter;
    ArrayList<Map<String, Object>> data;
    Map<String, Object> m;

    public SchedulerModeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.sceduler_mode, container, false);
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
        data = new ArrayList<Map<String, Object>>();
            m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_FREQUENCY, "Frequency in MHz");
            m.put(ATTRIBUTE_TIME, "Time in seconds");
            data.add(m);

        String[] from = { ATTRIBUTE_FREQUENCY, ATTRIBUTE_TIME };
        int[] to = { R.id.scheduleFrequencyText, R.id.scheduleTimeText };

        sAdapter = new SimpleAdapter(getActivity(), data, R.layout.scheduler_item, from, to);
        lvSimple = (ListView) getActivity().findViewById(R.id.scheduleList);
        lvSimple.setAdapter(sAdapter);
        registerForContextMenu(lvSimple);
    }

    public void onButtonClick() {
        Intent intent = new Intent(getActivity(), AddItemToScheduleActivity.class);
        startActivityForResult(intent, 1);
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0,
                getActivity().getResources().getString(R.string.delete_item_menu_entry));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
            data.remove(acmi.position);
            sAdapter.notifyDataSetChanged();
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
        m = new HashMap<String, Object>();
        m.put(ATTRIBUTE_FREQUENCY, frequency);
        m.put(ATTRIBUTE_TIME, time);
        // добавляем его в коллекцию
        data.add(m);
        // уведомляем, что данные изменились
        sAdapter.notifyDataSetChanged();
    }



}
