package ua.pp.lab101.synthesizercontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

import ua.pp.lab101.synthesizercontrol.adregisters.adparameters.DoubleBuffer;

public class AddItemToScheduleActivity extends ActionBarActivity implements View.OnClickListener{
    private static final String LOG_TAG = "AddItem";
    private Button mApplyBtn = null;
    private EditText mFrequencyValueET = null;
    private EditText mTimeValueET = null;
    private double mFerquencyValue = 0;
    private int mTimeValue = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_to_schedule);
        mApplyBtn = (Button) findViewById(R.id.applyBtn);
        mFrequencyValueET = (EditText) findViewById(R.id.frequencyValue);
        mTimeValueET = (EditText) findViewById(R.id.timeValue);
        mApplyBtn.setOnClickListener(this);
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
        if (id == R.id.action_stop_service) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        String frequencyValueText = mFrequencyValueET.getText().toString();
        String  timeValueText =  mTimeValueET.getText().toString();
        try {
            mFerquencyValue = Double.parseDouble(frequencyValueText);
            mTimeValue = Integer.parseInt(timeValueText);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Parse exception occurred");
        }

        if (mFerquencyValue < 35 || mFerquencyValue > 4400) {
            showToast(getString(R.string.frequency_incorrect));
            return;
        }

        if (mTimeValue <= 0 || mTimeValue > 18000) {
            showToast(getString(R.string.time_incorrect));
            return;
        }

        double newDouble = new BigDecimal(mFerquencyValue).setScale(3, RoundingMode.UP).doubleValue();
        frequencyValueText = String.valueOf(newDouble);
        intent.putExtra("frequency", frequencyValueText );
        intent.putExtra("time", timeValueText);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
