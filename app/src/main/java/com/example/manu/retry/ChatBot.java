package com.example.manu.retry;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alicebot.ab.*;

public class ChatBot extends AppCompatActivity {

    private ListView mListView;
    private Button mButtonSend,speak;
    private EditText mEditTextMessage;
    public Bot bot;
    public static Chat chat;
    private ChatMessageAdapter mAdapter;
    ArrayList<String> results;
    String omsg;

    PackageManager p;
    List<PackageInfo> ipkglist;
    static List<String> iipkglist;
    static List<String> iiapplist;
    List<PackageInfo> pkglist;
    static Intent LaunchIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        Intent intent = new Intent(this, HelloRetry.class);
        startService(intent);
        mListView = (ListView) findViewById(R.id.listview);
        mButtonSend = (Button) findViewById(R.id.send);
        mEditTextMessage = (EditText) findViewById(R.id.editmessage);
        mAdapter = new ChatMessageAdapter(this, new ArrayList<ChatMessage>());
        mListView.setAdapter(mAdapter);

        /*
        new AsyncTask<String,Void,String>(){
            @Override
            protected String doInBackground(String... params) {
                //some heavy processing resulting in a Data String

                return "whatever result you have";
            }
        }.execute("");
        */

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            Intent app = new Intent(Intent.ACTION_MAIN, null);
            app.addCategory(Intent.CATEGORY_LAUNCHER);

            p = this.getPackageManager();
            ipkglist = new ArrayList<PackageInfo>();
            iipkglist = new ArrayList<String>();
            iiapplist = new ArrayList<String>();
            pkglist = getPackageManager().getInstalledPackages(0);
            for (PackageInfo pi : pkglist) {
                //if (  (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                {
                    ipkglist.add(pi);
                    Log.e("oanCreate: ", pi.applicationInfo.loadLabel(p).toString() + " " + pi.packageName);
                    iipkglist.add(pi.packageName.toString());
                    iiapplist.add(pi.applicationInfo.loadLabel(p).toString());
                }
            }

//code for sending the message
            mButtonSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = omsg = mEditTextMessage.getText().toString();
                    //bot
                    String response = chat.multisentenceRespond(mEditTextMessage.getText().toString());
                    if (TextUtils.isEmpty(message)) {
                        return;
                    }
                    sendMessage(message);
                    mimicOtherMessage(response);
                    mEditTextMessage.setText("");
                    mListView.setSelection(mAdapter.getCount() - 1);
                }
            });

            speak = (Button) findViewById(R.id.stt);
            speak.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    // This are the intents needed to start the Voice recognizer
                    Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

                    startActivityForResult(i, 1010);
                }
            });
            //checking SD card availablility
            boolean a = isSDCARDAvailable();
            //receiving the assets from the app directory
            AssetManager assets = getResources().getAssets();
            File jayDir = new File(Environment.getExternalStorageDirectory().toString() + "/superaiml/bots/superAIML");
            File todel = new File(Environment.getExternalStorageDirectory().toString() + "/superaiml");
            deleteRecursive(todel);
        /*
        File afile = new File(Environment.getExternalStorageDirectory().toString());
        String[] directories = afile.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        System.out.println(Arrays.toString(directories));
        */
            boolean b = jayDir.mkdirs();
            if (jayDir.exists()) {
                //Reading the file
                try {
                    for (String dir : assets.list("superAIML")) {
                        File subdir = new File(jayDir.getPath() + "/" + dir);
                        boolean subdir_check = subdir.mkdirs();
                        for (String file : assets.list("superAIML/" + dir)) {
                            File f = new File(jayDir.getPath() + "/" + dir + "/" + file);
                            if (f.exists()) {
                                continue;
                            }
                            InputStream in = null;
                            OutputStream out = null;
                            in = assets.open("superAIML/" + dir + "/" + file);
                            out = new FileOutputStream(jayDir.getPath() + "/" + dir + "/" + file);
                            //copy file from assets to the mobile's SD card or any secondary memory
                            copyFile(in, out);
                            in.close();
                            in = null;
                            out.flush();
                            out.close();
                            out = null;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//get the working directory
            MagicStrings.root_path = Environment.getExternalStorageDirectory().toString() + "/superAIML";
            System.out.println("Working Directory = " + MagicStrings.root_path);
            AIMLProcessor.extension = new PCAIMLProcessorExtension();
//Assign the AIML files to bot for processing
            bot = new Bot("superAIML", MagicStrings.root_path, "chat");
            chat = new Chat(bot);
            String[] args = null;
            mainFunction(args);

            Intent i = getIntent();
            String s = i.getStringExtra(MainActivity.s);
            s = "";
            if (!s.equals("")) {
                s = "MY NAME IS " + s;
                mimicOtherMessage(chat.multisentenceRespond(s));
            } else {
                s = "HI";
                mimicOtherMessage(chat.multisentenceRespond(s));
            }
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            return;
        }
    }

    public List<ApplicationInfo> getApplicationList(Context con){
        PackageManager p = con.getPackageManager();
        List<ApplicationInfo> info = p.getInstalledApplications(0);
        return info;
    }
    public String applicationLabel(Context con,ApplicationInfo info){
        PackageManager p = con.getPackageManager();
        String label = p.getApplicationLabel(info).toString();
        return label;
    }
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        // retrieves data from the VoiceRecognizer
        if (requestCode == 1010 && resultCode == RESULT_OK) {
            results = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mEditTextMessage.setText(results.get(0));
            mButtonSend.callOnClick();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //check SD card availability
    public static boolean isSDCARDAvailable(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)? true :false;
    }
    //copying the file
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    //Request and response of user and the bot
    public static void mainFunction (String[] args) {
        MagicBooleans.trace_mode = false;
        System.out.println("trace mode = " + MagicBooleans.trace_mode);
        Graphmaster.enableShortCuts = true;
        Timer timer = new Timer();
        String request = "Hello.";
        String response = chat.multisentenceRespond(request);

        System.out.println("Human: "+request);
        System.out.println("Robot: " + response);
    }
    private void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, true, false);
        mAdapter.add(chatMessage);
        //respond as Helloworld
        //mimicOtherMessage("HelloWorld");
    }

    private void mimicOtherMessage(String message) {

        if (!message.contains("<oob>"))
        {
            ChatMessage chatMessage = new ChatMessage(message, false, false);
            mAdapter.add(chatMessage);
        }
        else
        {
            String msg=message.substring(0,message.indexOf("<oob>"));
            ChatMessage chatMessage = new ChatMessage(msg, false, false);
            mAdapter.add(chatMessage);
            OOBProcessor oob = new OOBProcessor(this);
            try {
                Log.e("mimicOtherMessage: ",message );
                oob.removeOobTags(message,omsg);
                if(!OOBProcessor.searchresult.equals(""))
                {
                    ChatMessage searchreply = new ChatMessage(OOBProcessor.searchresult, false, false);
                    mAdapter.add(searchreply);
                    OOBProcessor.searchresult="";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage() {
        ChatMessage chatMessage = new ChatMessage(null, true, true);
        mAdapter.add(chatMessage);

        mimicOtherMessage();
    }

    private void mimicOtherMessage() {
        ChatMessage chatMessage = new ChatMessage(null, false, true);
        mAdapter.add(chatMessage);
    }
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    finish();
                    startActivity(getIntent());

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    ChatMessage chatMessage = new ChatMessage("Oops! I need your storage access to talk to you!", false, false);
                    mAdapter.add(chatMessage);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
