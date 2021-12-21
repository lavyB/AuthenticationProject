package com.bnm.lavy.authenticationproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView lvTasks;
    TaskAdapter taskAdapter;
    ArrayList<Task> tasks;
    Task lastSelection; // last selected item
    Button btAdd;
    SoundPool sp;
    private int sms; // receive sms sound
    DatabaseReference myRef = null;
    ValueEventListener valueEventListener = null;



    IncomingSms_receiver smsReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvTasks = findViewById(R.id.lvTasks);
        btAdd = findViewById(R.id.btNewTask);
        tasks = new ArrayList<Task>();
        taskAdapter = new TaskAdapter(this, tasks);
        lvTasks.setAdapter(taskAdapter);
        myRef = FirebaseDatabase.getInstance().getReference();
        load_tasks();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Ask for permision
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        }
        smsReceiver = new IncomingSms_receiver();


//        LoadItemsToList load = new LoadItemsToList();
//        load.execute();

        lvTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastSelection = taskAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
                intent.putExtra("text", lastSelection.getContent());
                intent.putExtra("dl", lastSelection.getDeadLine());
                intent.putExtra("hp", lastSelection.isHiPriority());

                startActivityForResult(intent, 0); // 0 - for update task

            }
        });
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
                startActivityForResult(intent, 1); // 1 -  for add new task

            }
        });
        lvTasks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //hlp.deleteTask(tasks.get(position).getId());
                String str_id = tasks.get(position).getId();

                myRef.child(str_id).removeValue();
                //tasks.remove(position);
                taskAdapter.notifyDataSetChanged();
                //saveTasks();
                return true;
            }
        });
        createSoundEffects();

    }

    public void createSoundEffects()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            sp = new SoundPool.Builder().setMaxStreams(20).build();
        }
        else sp = new SoundPool(20, AudioManager.STREAM_MUSIC,1);
        sms = sp.load(this,R.raw.bounce,1);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if ( id == R.id.mnuDelete )
        {
            //hlp.deleteAllTasks();
            tasks.clear();
            taskAdapter.notifyDataSetChanged();
        }
        else if ( id == R.id.mnuExit)
        {
            finishAffinity();
        }
        return true;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) // update or delete
            {
                if (data.getBooleanExtra("delete", false)) {
                    //hlp.deleteTask(lastSelection.getId());
                    String id = lastSelection.getId();
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                    myRef.child(id).removeValue();
                    //tasks.remove(lastSelection);

                    taskAdapter.notifyDataSetChanged();
                } else // update
                {
                    String id = lastSelection.getId();
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

                    lastSelection.setContent(data.getStringExtra("text"));
                    lastSelection.setDeadLine(data.getStringExtra("dl"));
                    lastSelection.setHiPriority(data.getBooleanExtra("hp", false));

                    myRef.child(id).setValue(lastSelection);

                    //hlp.updateTask(lastSelection);
                    taskAdapter.notifyDataSetChanged();

                }
            } else if (requestCode == 1) // for add
            {
                String taskText = data.getStringExtra("text");
                boolean taskHi = data.getBooleanExtra("hp", false);
                String taskDL = data.getStringExtra("dl");
                Task t = new Task(taskText, taskDL, taskHi);
                tasks.add(0, new Task(taskText, taskDL, taskHi));
                //hlp.insertTask(t);

                // write task to database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference().push();
                t.setId(myRef.getKey());
                myRef.setValue(t);
                taskAdapter.notifyDataSetChanged();
            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(smsReceiver);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity( new Intent(this, LoginActivity.class));
    }

    @Override
    protected void onDestroy() {
        myRef.removeEventListener(valueEventListener);
        super.onDestroy();
    }

    /*
        Loads all items to list view
         */
    private void load_tasks()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        valueEventListener = new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                tasks.clear();
                for(DataSnapshot taskSnapshot: dataSnapshot.getChildren())
                {
                    Task task = taskSnapshot.getValue(Task.class);
                    tasks.add(task);
                }

                taskAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Data change fail!", Toast.LENGTH_LONG).show();
            }
        };
        // Read from the database
        myRef.addValueEventListener(valueEventListener);
    }

    /**
     * Loads all item and fill the list view
     */
//    private class LoadItemsToList extends AsyncTask<Void, Void, ArrayList<com.bnm.lavy.todolistproj.Task>> {
//
//        @Override
//        protected ArrayList<Task> doInBackground(Void... voids) {
//
//            return hlp.loadAllItems();
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPostExecute(tasks);
//            // database helper init
//            hlp = new HelperDB(MainActivity.this);
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<com.bnm.lavy.todolistproj.Task> lst) {
//            tasks = lst;
//            // creates adapter
//            taskAdapter = new com.bnm.lavy.todolistproj.TaskAdapter(MainActivity.this, tasks);
//
//            // links adapter to the list
//            lvTasks.setAdapter(taskAdapter);
//        }
//
//    }
    public class IncomingSms_receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle myBundle = intent.getExtras();
            SmsMessage[] messages = null;
            String strMessage = "";

            if (myBundle != null) {
                Object[] pdus = (Object[]) myBundle.get("pdus");

                messages = new SmsMessage[pdus.length];

                for (int i = 0; i < messages.length; i++) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = myBundle.getString("format");
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    } else {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    strMessage += messages[i].getMessageBody();
                }
                if (strMessage.toUpperCase().startsWith("T00:")) {
                    sp.play(sms, 1, 1, 0, 0, 1);
                    strMessage = strMessage.substring(4);
                    Task t = new Task(strMessage, "", false);
                    tasks.add(0, new Task(strMessage, "", false));
                    //hlp.insertTask(t);
                    taskAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}




