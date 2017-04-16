package com.example.manu.retry;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class WidgetDialog extends AppCompatActivity {

    String selectedFromList,url;
    ListView options;
    ArrayList<String> results;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_widget_dialog);
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
            startActivityForResult(i, 1000);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        // retrieves data from the VoiceRecognizer
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            results = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //options.setAdapter(new ArrayAdapter<String>(this,
              //      android.R.layout.simple_list_item_1, results));
            selectedFromList =results.get(0);
            if(selectedFromList.substring(0,4).matches("call"))
            {
                //Intent configIntent = new Intent(this, AppWidget.class);
                //configIntent.putExtra(selectedFromList.substring(0,4),true);
                selectcontact();
                finish();
                System.exit(0);
            }
        }
    }
    public void selectcontact()
    {
        url=null;
        if(selectedFromList!=null)
        {
            Intent i=new Intent(this,AppWidget.class);
            url = selectedFromList.substring(5);
            if(url.replace(" ","").matches("[0-9]+")) {
                Toast.makeText(this,"Attacking "+url,Toast.LENGTH_SHORT).show();
                i.putExtra("url",url);
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
