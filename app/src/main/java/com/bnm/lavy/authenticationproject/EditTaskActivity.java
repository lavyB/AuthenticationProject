package com.bnm.lavy.authenticationproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class EditTaskActivity extends AppCompatActivity implements View.OnClickListener {

    Button btDeleteTask, btCancel, btUpdate, btDeadLine;
    CheckBox chDeadLine, chHiPriority;
    EditText etTaskText;
    DatePickerDialog picker; // dead line date picker

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        btDeleteTask = findViewById(R.id.btDeleteTask);
        btCancel = findViewById(R.id.btCancel);
        btUpdate = findViewById(R.id.btUpdate);
        btDeadLine = findViewById(R.id.btDeadLine);
        etTaskText = findViewById(R.id.etTaskText);
        chHiPriority = findViewById(R.id.chHiPriority);
        chDeadLine = findViewById(R.id.chDeatLine);


        Intent intent = getIntent();
        if (intent.getExtras() != null)
        {
            etTaskText.setText(intent.getStringExtra("text"));
            String dl = intent.getStringExtra("dl");
            if ( !dl.equals("")) {
                chDeadLine.setChecked(true);
            }
            btDeadLine.setText(dl);
            chHiPriority.setChecked(intent.getBooleanExtra("hp", false));
        }
        setDeadLineVisibility();

        btDeleteTask.setOnClickListener(this);
        btDeadLine.setOnClickListener(this);
        btUpdate.setOnClickListener(this);
        chDeadLine.setOnClickListener(this);
        btCancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if( v == btDeleteTask)
        {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("delete", true);
            setResult(RESULT_OK, intent);
            finish();
        }
        else if ( v == btDeadLine)
        {
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(EditTaskActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            btDeadLine.setText(dayOfMonth + "." + (monthOfYear + 1) + "." + year);
                        }
                    }, year, month, day);
            picker.show();

        }
        else if ( v == btUpdate)
        {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("delete", false);
            intent.putExtra("text", etTaskText.getText().toString());
            if (chDeadLine.isChecked())
                intent.putExtra("dl", btDeadLine.getText().toString());
            else
                intent.putExtra("dl", "");
            intent.putExtra("hp", chHiPriority.isChecked());
            setResult(RESULT_OK, intent);
            finish();
        }
        else if (v == chDeadLine)
        {
            setDeadLineVisibility();
        }
        else if( v== btCancel)
        {
            setResult(RESULT_CANCELED,null);
            finish();
        }
    }
    // Set dead line button visibility according to status of check box
    private void setDeadLineVisibility()
    {
        if ( chDeadLine.isChecked()) {
            btDeadLine.setVisibility(View.VISIBLE);
            if (btDeadLine.getText().toString().equals(""))
                btDeadLine.setText("Set a Date");
        }
        else
            btDeadLine.setVisibility(View.INVISIBLE);
    }
}
