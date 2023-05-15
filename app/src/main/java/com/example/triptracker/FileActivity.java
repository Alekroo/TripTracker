package com.example.triptracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class FileActivity extends AppCompatActivity {

    private Button btnFile,btnDelete;
    private ListView lvFiles;
    private ArrayList<String> textFiles;
    private String selectedItem;
    private RadioButton rbSpeed;
    private RadioButton rbMovement;
    private RadioButton rbTdbscan;
    private String mode;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        //Connects xml components with java code (buttons, radio buttons, list)
        setUpLayout();

        //will be used to store the mode which the user chooses
        mode = "";

        String[] files = this.fileList();
        if(files.length<1)
        {

        }
        textFiles = new ArrayList<>();

        for(String file : files)
        {
            if (file.toLowerCase().endsWith(".csv")) {
                textFiles.add(file);
            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, textFiles);
        lvFiles.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvFiles.setAdapter(arrayAdapter);

        lvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //String s = lv_files.getItemAtPosition(i).toString();
                selectedItem = lvFiles.getItemAtPosition(i).toString();
                Toast.makeText(getBaseContext(), "Selected: " + selectedItem,
                        Toast.LENGTH_SHORT).show();
            }
        });


        //When the choose file button is pressed it will check if the user
        //has selected a file and mode, if he has it wall call the startResultActivity
        //if he has not it will display a message reminding him of picking them.
        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedItem != null && (rbSpeed.isChecked() || rbMovement.isChecked() ||
                        rbTdbscan.isChecked())){

                    if (rbSpeed.isChecked()) {
                        mode = "speed";
                    } else if (rbMovement.isChecked()) {
                        mode = "movement";
                    } else if (rbTdbscan.isChecked()) {
                        mode = "db";
                    }
                    startResultActivity();
                }
                else{
                    Toast.makeText(getBaseContext(), "Select file and mode to continue!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //If delete button is pressed when a file is selected, that files gets deleted -
        //from the internal storage
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File[] files = getFilesDir().listFiles();
                for (File file : files) {
                    if(file.getName().equals(selectedItem)) {
                        file.delete();
                    }
                }
                recreate();
            }
        });


    }

    //Starts the ResultActivity, passing to it a file name and mode that the user chose
    public void startResultActivity()
    {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("fileName", selectedItem);
        intent.putExtra("mode", mode);
        startActivity(intent);
    }


    //Connects xml components with java code (buttons, radio buttons, list)
    public void setUpLayout()
    {
        btnFile = findViewById(R.id.btnFile);
        lvFiles = (ListView)findViewById(R.id.lv_files);
        rbSpeed = findViewById(R.id.rbSpeed);
        rbMovement = findViewById(R.id.rbMovement);
        rbTdbscan = findViewById(R.id.rbTdbscan);
        btnDelete = findViewById(R.id.btnDelete);
    }




}