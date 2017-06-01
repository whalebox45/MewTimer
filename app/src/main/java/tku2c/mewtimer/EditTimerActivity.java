package tku2c.mewtimer;

import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class EditTimerActivity extends Activity {

    private NumberPicker npHour, npMinute, npSecond;
    private int hour, minute, second;
    private long value;
    private EditText etName;
    private String name;
    private Button btnCancel, btnDone;
    private Bundle bundleReceive;
    private int position;

    private Spinner spnTone;
    private int toneID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_timer);


        etName = (EditText) findViewById(R.id.etname);

        spnTone = (Spinner) findViewById(R.id.spnTone);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnDone = (Button) findViewById(R.id.btnDone);

        final Intent intent = getIntent();
        bundleReceive = intent.getExtras();

        name = bundleReceive.getString("timer_name");
        value = bundleReceive.getLong("timer_length");
        position = bundleReceive.getInt("timer_position");
        toneID = bundleReceive.getInt("tone_id");

        etName.setText(name);

        this.second = (int) (value / 1000 % 60);
        this.minute = (int) (value / 1000 / 60 % 60);
        this.hour = (int) (value / 1000 / 3600 % 24);

        initNpMinute();
        initNpHour();
        initNpSecond();

        npHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                hour = newVal;
            }
        });
        npMinute.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                minute = newVal;
            }
        });
        npSecond.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                second = newVal;
            }
        });

        initSpnTone();
        spnTone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  try {
                      Cursor cursor = MainActivity.dbTone.rawQuery("SELECT * FROM " +
                              MainActivity.tb_name + " WHERE name = \'" + spnTone.getSelectedItem().toString() + "\'", null);
                      cursor.moveToFirst();
                      toneID = cursor.getInt(cursor.getColumnIndex("resID"));
                  }
                  catch(Exception e){
                      Log.e("test",e.toString());
                  }
              }
              @Override
              public void onNothingSelected(AdapterView<?> parent) {

              }
          });

        btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });


        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                value = (long) (second + minute * 60 + hour * 3600) * 1000;
                name = etName.getEditableText().toString();
                Bundle bundleBack = new Bundle();
                if (value<1000) value = 1000;
                bundleBack.putInt("timer_position",position);
                bundleBack.putString("edit_name", name);
                bundleBack.putLong("edit_time", value);
                bundleBack.putInt("tone_id",toneID);
                intent.putExtras(bundleBack);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void initSpnTone(){

        ArrayList<String> ast = new ArrayList<>();

        Cursor cursor = MainActivity.dbTone.rawQuery("SELECT * FROM "+MainActivity.tb_name,null);

        if(cursor.moveToFirst()){
            do{
                ast.add(cursor.getString(cursor.getColumnIndex("name")));
            } while(cursor.moveToNext());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.spinner_style,ast);
        spnTone.setAdapter(adapter);
        spnTone.setVisibility(View.VISIBLE);
    }

    private void initNpHour() {
        npHour = (NumberPicker) findViewById(R.id.npHour);
        npHour.setMaxValue(23);
        npHour.setMinValue(0);
        npHour.setValue(this.hour);
        npHour.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                String data = value < 10 ? "0" + value : String.valueOf(value);
                return data;
            }
        });
        npHour.setWrapSelectorWheel(true);
    }

    private void initNpMinute() {
        npMinute = (NumberPicker) findViewById(R.id.npMinute);
        npMinute.setMaxValue(59);
        npMinute.setMinValue(0);
        npMinute.setValue(this.minute);
        npMinute.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                String data = value < 10 ? "0" + value : String.valueOf(value);
                return data;
            }
        });
    }

    private void initNpSecond() {
        npSecond = (NumberPicker) findViewById(R.id.npSecond);
        npSecond.setMaxValue(59);
        npSecond.setMinValue(0);
        npSecond.setValue(this.second);
        npSecond.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                String data = value < 10 ? "0" + value : String.valueOf(value);
                return data;
            }
        });
    }
}
