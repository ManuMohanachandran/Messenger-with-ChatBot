package com.example.manu.retry;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
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
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class ChatBot extends AppCompatActivity {

    private ListView mListView;
    private ImageButton mButtonSend,speak;
    private EditText mEditTextMessage;
    private ChatMessageAdapter mAdapter;
    ArrayList<String> results;
    String omsg,answer;

    PackageManager p;
    List<PackageInfo> ipkglist;
    static List<String> iipkglist;
    static List<String> iiapplist;
    List<PackageInfo> pkglist;
    static Intent LaunchIntent;
    ArrayList<ChatMessage> chatarr = new ArrayList<ChatMessage>();
    TextToSpeech tts;
    String that="nocontext";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        mListView = (ListView) findViewById(R.id.listview);
        mButtonSend = (ImageButton) findViewById(R.id.send);
        mEditTextMessage = (EditText) findViewById(R.id.editmessage);
        mAdapter = new ChatMessageAdapter(this, chatarr);
        mListView.setAdapter(mAdapter);

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
                    String message = omsg = mEditTextMessage.getText().toString().trim();
                    message=TextProcessor.Process(message.toLowerCase());
                    Log.e("onClick: ", message);
                    if (TextUtils.isEmpty(message)) {
                        return;
                    }
                    sendMessage(message);
                    try {
                        aimlparser(message);
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                    //mimicOtherMessage(response);
                    mEditTextMessage.setText("");
                    mListView.setSelection(mAdapter.getCount() - 1);
                }
            });

            tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {

                @Override
                public void onInit(int status) {
                    // TODO Auto-generated method stub
                    if(status == TextToSpeech.SUCCESS){
                        int result=tts.setLanguage(new Locale("en","IN"));
                        if(result==TextToSpeech.LANG_MISSING_DATA ||
                                result==TextToSpeech.LANG_NOT_SUPPORTED){
                            Log.e("error", "This Language is not supported");
                        }
                        else{
                            //tts.speak(answer, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                    else
                        Log.e("error", "Initilization Failed!");
                }
            });
            speak = (ImageButton) findViewById(R.id.stt);
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
            File jayDir = new File(Environment.getExternalStorageDirectory().toString() + "/chatbot");
            File todel = new File(Environment.getExternalStorageDirectory().toString() + "/chatbot");
            deleteRecursive(todel);

            boolean b = jayDir.mkdirs();
            if (jayDir.exists()) {
                //Reading the file
                try {
                    for (String dir : assets.list("chatbot")) {
                        File subdir = new File(jayDir.getPath() + "/" + dir);
                        boolean subdir_check = subdir.mkdirs();
                        for (String file : assets.list("chatbot/" + dir)) {
                            File f = new File(jayDir.getPath() + "/" + dir + "/" + file);
                            if (f.exists()) {
                                continue;
                            }
                            InputStream in = null;
                            OutputStream out = null;
                            in = assets.open("chatbot/" + dir + "/" + file);
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

            Intent i = getIntent();
            String s = i.getStringExtra(MainActivity.s);
            s = "";
            if (!s.equals("")) {
                s = "MY NAME IS " + s;
                //mimicOtherMessage(chat.multisentenceRespond(s));
            } else {
                s = "HI";
                //mimicOtherMessage(chat.multisentenceRespond(s));
            }
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            return;
        }
        Intent intent = new Intent(this, HelloRetry.class);
        startService(intent);
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

    private void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, true, false);
        mAdapter.add(chatMessage);
        //respond as Helloworld
        //mimicOtherMessage("HelloWorld");
    }

    public void aimlparser(String message) throws ParserConfigurationException, IOException, SAXException {
        int delaybound=0;
int flag=0;
        try {
            StringBuilder sb=new StringBuilder("Searching:\n");
            File datadir = new File(Environment.getExternalStorageDirectory().toString() + "/chatbot/cbml");
            for (File dataFile:datadir.listFiles()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(dataFile);
                doc.getDocumentElement().normalize();
                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                NodeList nList = doc.getElementsByTagName("category");
                System.out.println("----------------------------");

                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    System.out.println("\nCurrent Element :" + nNode.getNodeName());
                    NodeList pList=nNode.getChildNodes();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        for (int ptemp = 1; ptemp < pList.getLength(); ptemp=ptemp+2) {
                            Node pNode = pList.item(ptemp);
                            System.out.println("\nCurrent Element yo :" + pNode.getNodeName());
                            if (pNode.getNodeType() == Node.ELEMENT_NODE) {
                                if (ptemp<pList.getLength()-3)
                                    sb.append("Pattern: "
                                            + pNode.getTextContent() + "\n");
                                else
                                    sb.append("Template: "
                                            + pNode.getTextContent() + "\n");
/*

                            sb.append("Template : "
                                    + eElement
                                    .getElementsByTagName("template")
                                    .item(0)
                                    .getTextContent().trim() + "\n");
 */
                                String ps = pNode.getTextContent().trim();
                                String ms = message;
                                ps = ps.replace("*", "(\\w|\\s)*");
                                Pattern p = Pattern.compile(ps,Pattern.CASE_INSENSITIVE);
                                Matcher m = p.matcher(ms);
                                NodeList nl = eElement.getElementsByTagName("that");
                                int maxthatlength=nl.getLength();

                                if (m.matches() && that.equalsIgnoreCase(eElement.getElementsByTagName("that").item(maxthatlength-1).getTextContent()) && !that.equalsIgnoreCase("nocontext"))
                                {
                                    flag=1;
                                    Log.e("aimlparser: ", "Heya "+eElement
                                            .getElementsByTagName("template")
                                            .item(0)
                                            .getTextContent().trim());
                                    ChatMessage chatMessage = new ChatMessage(sb.toString(), false, false);
                                    //mAdapter.add(chatMessage);

                                    chatMessage = new ChatMessage("Pattern: "
                                            + pNode
                                            .getTextContent() + "\nTemplate : "
                                            + eElement
                                            .getElementsByTagName("template")
                                            .item(0)
                                            .getTextContent().trim(), false, false);
                                    answer=eElement
                                            .getElementsByTagName("template")
                                            .item(0)
                                            .getTextContent();
                                    chatMessage = new ChatMessage(answer, false, false);
                                    final ChatMessage typechatmessage=new ChatMessage("typing...", false, false);
                                    mAdapter.add(typechatmessage);
                                    final ChatMessage finalChatMessage = chatMessage;
                                    final Handler handler = new Handler();
                                    delaybound=new Random().nextInt(3000)+1000+delaybound;
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapter.remove(typechatmessage);
                                            mAdapter.add(finalChatMessage);
                                            tts.speak(answer, TextToSpeech.QUEUE_ADD, null);
                                        }
                                    }, delaybound);
                                    if (!answer.equalsIgnoreCase("Sorry, I couldn't get you."))
                                    break;
                                }


                            }
                        }
                        for (int ptemp = 1; ptemp < pList.getLength(); ptemp=ptemp+2) {
                            Node pNode = pList.item(ptemp);
                            System.out.println("\nCurrent Element yo :" + pNode.getNodeName());
                            if (pNode.getNodeType() == Node.ELEMENT_NODE) {
                                if (ptemp<pList.getLength()-3)
                                    sb.append("Pattern: "
                                            + pNode.getTextContent() + "\n");
                                else
                                    sb.append("Template: "
                                            + pNode.getTextContent() + "\n");
/*

                            sb.append("Template : "
                                    + eElement
                                    .getElementsByTagName("template")
                                    .item(0)
                                    .getTextContent().trim() + "\n");
 */
                                String ps = pNode.getTextContent().trim();
                                String ms = message;
                                ps = ps.replace("*", "(\\w|\\s)*");
                                Pattern p = Pattern.compile(ps,Pattern.CASE_INSENSITIVE);
                                Matcher m = p.matcher(ms);
                                NodeList nl = eElement.getElementsByTagName("that");
                                int maxthatlength=nl.getLength();

                                if (m.matches() && eElement.getElementsByTagName("that").item(0).getTextContent().equalsIgnoreCase("nocontext")) {
                                    flag=1;
                                    that=eElement.getElementsByTagName("that").item(maxthatlength-1).getTextContent();
                                    sb.append("Template: "
                                            + eElement
                                            .getElementsByTagName("template")
                                            .item(0)
                                            .getTextContent().trim() + "\n");
                                    ChatMessage chatMessage = new ChatMessage(sb.toString(), false, false);
                                    //mAdapter.add(chatMessage);

                                    chatMessage = new ChatMessage("Pattern: "
                                            + pNode
                                            .getTextContent() + "\nTemplate : "
                                            + eElement
                                            .getElementsByTagName("template")
                                            .item(0)
                                            .getTextContent().trim(), false, false);
                                    answer=eElement
                                            .getElementsByTagName("template")
                                            .item(0)
                                            .getTextContent();
                                    chatMessage = new ChatMessage(answer, false, false);
                                    final ChatMessage typechatmessage=new ChatMessage("typing...", false, false);
                                    mAdapter.add(typechatmessage);
                                    final ChatMessage finalChatMessage = chatMessage;
                                    final Handler handler = new Handler();
                                    delaybound=new Random().nextInt(3000)+1000+delaybound;
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapter.remove(typechatmessage);
                                            mAdapter.add(finalChatMessage);
                                            tts.speak(answer, TextToSpeech.QUEUE_ADD, null);
                                        }
                                    }, delaybound);
                                    break;
                                }
                            }
                        }
                        System.out.println("Pattern: "
                                + eElement
                                .getElementsByTagName("pattern")
                                .item(0)
                                .getTextContent());
                        System.out.println("Template : "
                                + eElement
                                .getElementsByTagName("template")
                                .item(0)
                                .getTextContent());

                    }
                    if (flag==1)break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
