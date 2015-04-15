package ua.pp.lab101.synthesizercontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddItemToScheduleActivity extends ActionBarActivity implements View.OnClickListener{
    private Button applyBtn = null;
    private EditText frequencyValue = null;
    private EditText timeValue = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_to_schedule);
        applyBtn = (Button) findViewById(R.id.applyBtn);
        frequencyValue = (EditText) findViewById(R.id.frequencyValue);
        timeValue = (EditText) findViewById(R.id.timeValue);
        applyBtn.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_item_to_schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        String frequencyValueText = frequencyValue.getText().toString();
        String  timeValueText =  timeValue.getText().toString();

        if (Double.parseDouble(frequencyValueText) < 35 || Double.parseDouble(frequencyValueText) > 4400) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.frequency_incorrect),
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (Integer.parseInt(timeValueText) < 0 || Integer.parseInt(timeValueText) > 18000) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.time_incorrect),
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }
        intent.putExtra("frequency", frequencyValueText );
        intent.putExtra("time", timeValueText);
        setResult(RESULT_OK, intent);
        finish();
    }
}
