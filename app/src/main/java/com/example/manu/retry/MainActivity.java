package com.example.manu.retry;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    String selectedFromList,url;
    Button speak;
    ListView options;
    ArrayList<String> results;
    int req=1010;
    private int SPEECH_REQUEST_CODE= 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                    PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                            PackageManager.PERMISSION_GRANTED) {
                //do the things} else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            else
            {
                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myAppSettings);
            }
        }

        boolean isFromWidget = false;
        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.get("widget") != null)
        {
            isFromWidget = Boolean.valueOf(extras.get("widget").toString());
        }
        if(isFromWidget) {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
            startActivityForResult(i, SPEECH_REQUEST_CODE);
        }

        speak = (Button) findViewById(R.id.stt);
        options = (ListView) findViewById(R.id.outlist);
        speak.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (results!=null) {
                    results.clear();
                    options.setAdapter(null);
                    selectedFromList=null;
                    ((TextView)findViewById(R.id.out)).setText(null);
                }
                // TODO Auto-generated method stub
                // This are the intents needed to start the Voice recognizer
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

                startActivityForResult(i, 1010);
            }
        });

        // retrieves data from the previous state. This is incase the phones
        // orientation changes
        if (savedInstanceState != null) {
            results = savedInstanceState.getStringArrayList("results");

            if (results != null)
                options.setAdapter(new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, results));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        // retrieves data from the VoiceRecognizer
        req=requestCode;
        if (requestCode == 1010 && resultCode == RESULT_OK) {
            results = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            options.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, results));
        }
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            results = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            options.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, results));
            selectedFromList =results.get(0);
            if(selectedFromList.substring(0,4).matches("call"))
            {
                Intent configIntent = new Intent(this, AppWidget.class);
                configIntent.putExtra(selectedFromList.substring(0,4),true);
                selectcontact();
                Intent i=new Intent(this,AppWidget.class);
                i.putExtra("c",url);
            }
            finish();
            System.exit(0);
        }

        final ListView lv = (ListView) findViewById(R.id.outlist);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                selectedFromList =(String) (lv.getItemAtPosition(myItemInt));
                if(selectedFromList.substring(0,4).matches("call"))
                ((TextView)findViewById(R.id.out)).setText("Attack "+selectedFromList.substring(5)+" ?");
            }
        });

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // This should save all the data so that when the phone changes
        // orientation the data is saved
        super.onSaveInstanceState(outState);

        outState.putStringArrayList("results", results);
    }

    public static String s = "";

    public void proclick(View view) {

        EditText et=(EditText) findViewById(R.id.txtbx);
        s=et.getText().toString();

        Intent i=new Intent(this,Display.class);
        i.putExtra(s,s);
        startActivity(i);
    }
    public void chatbotclick(View view) {
        EditText et=(EditText) findViewById(R.id.txtbx);
        s=et.getText().toString();

        Intent i=new Intent(this,ChatBot.class);
        i.putExtra(s,s);
        startActivity(i);
    }

    public void attackclick(View view) {
        selectcontact();
    }

    public void selectcontact()
    {
        url=null;
        if(selectedFromList!=null)
        {
            url = selectedFromList.substring(5);
            if(url.replace(" ","").matches("[0-9]+")) {
                Toast.makeText(this,"Attacking "+url,Toast.LENGTH_SHORT).show();
                call(url);
            }
            else
            {
                String name=null,phoneNumber=null;
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
                while (phones.moveToNext())
                {
                    name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (url.equalsIgnoreCase(name)) {
                        phoneNumber=phoneNumber.replace(" ","").replace("-","");
                        break;
                    }
                    name=null;
                    phoneNumber=null;
                }
                phones.close();
                if (phoneNumber!=null && phoneNumber.length()!=10) {
                    Toast.makeText(this,"Attacking "+name,Toast.LENGTH_SHORT).show();
                    phoneNumber=phoneNumber.substring(phoneNumber.length()-10);
                    call(phoneNumber);
                }
                else if (phoneNumber!=null && phoneNumber.length()==10) {
                    Toast.makeText(this,"Attacking "+name,Toast.LENGTH_SHORT).show();
                    //phoneNumber=phoneNumber.substring(phoneNumber.length()-10);
                    call(phoneNumber);
                }
                else
                    Toast.makeText(this,"No such contact",Toast.LENGTH_SHORT).show();
            }
        }
        else
            Toast.makeText(this,"Please select a contact",Toast.LENGTH_SHORT).show();

    }
    public void call(String url)
    {
        url="tel:"+url;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }
}