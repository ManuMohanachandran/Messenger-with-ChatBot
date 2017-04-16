package com.example.manu.retry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * Created by Manu on 3/17/2017.
 */
public class OOBProcessor {

    String appname,pkgname,url;
    static String searchresult="";
    Context c;
    ChatBot activity;
    Exception exception = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    boolean flag=false;

    public OOBProcessor(ChatBot activity) {
        this.activity = activity;
        c=activity;
    }

    private class searchweb extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //some heavy processing resulting in a Data String
            try {
                Log.e("something", url);
                Document document=Jsoup.connect("http://www.bing.com/search?q="+url).get();

                Elements tit1 = document.getElementsByClass(" b_entityTitle");
                if (!tit1.text().equals(""))
                    searchresult=tit1.text()+".\n";
                Elements desc1 = document.select("div.b_lBottom.b_snippet > span");
                for (Element des : desc1) {
                    searchresult=searchresult+des.text();
                }

                Elements tit2 = document.getElementsByClass("b_focusTextMedium");
                if (!tit2.text().equals(""))
                    searchresult=searchresult+tit2.text()+".\n";
                Elements desc2 = document.select("div.rwrl.rwrl_sec.rwrl_padref");
                for (Element des : desc2) {
                    searchresult=searchresult+des.text();
                }

                /*
                Elements tit3 = document.getElementsByClass("b_focusTextMedium");
                if (!tit3.text().equals(""))
                    searchresult=searchresult+tit3.text()+".\n";
                    */
                Elements desc3 = document.select("div.dc_mn");
                for (Element des : desc3) {
                    searchresult=searchresult+"\n"+des.text()+"\n";
                }

                if (searchresult.equals(""))
                    searchresult="It's strange that I didn't find any results.";
                flag=true;
                cancel(true);
                /*
                Elements info = document.select("a[href]");
                Elements info2 = document.select("h3[class=r]");
                for (Element link : info) {
                    Elements titles = link.select("h3[class=r]");
                    String title = titles.text();

                    Elements bodies = link.select("span[class=st]");
                    String body = bodies.text();

                    System.out.println("Title: "+title);
                    System.out.println("Body: "+body+"\n");
                }
                */
                Log.e("doInBackground: ", document.text() );
                Log.e("info1 ",tit1.text() );
                //Log.e("info2 ",info2.text() );
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "whatever result you have";
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
    // remove the oob tags and send it along to the processor
    public void removeOobTags(String output, String msg) throws Exception {
        if (output != null) {
            Pattern pattern = Pattern.compile("<oob>(.*)</oob>");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                String oobContent = matcher.group(1);
                processInnerOobTags(oobContent,output,msg);
                //activity.showBotResponse(matcher.replaceAll(""));
            }
        }
    }

    // check inner oob command and take appropriate action
    public void processInnerOobTags(String oobContent,String output,String msg) throws Exception {
        Log.e("processInnerOobTags: ", oobContent);
        url=msg.toLowerCase().replace("search ","");
        if (oobContent.contains("<app>")) {
            appname=output.replace("Launching ","");
            Log.e("removeOobTags: ", appname);
            appname=appname.substring(0,appname.indexOf("...")).toLowerCase();
            Log.e("removeOobTags: ", appname);
            oobapp();
        }
        if (oobContent.contains("<search>")) {
            new searchweb().execute(url);
            while (!flag) {
                try { Thread.sleep(100); }
                catch (InterruptedException e) { e.printStackTrace(); }
            }
        }
        if (oobContent.contains("<camera>")) {
            oobCamera();
        }
        if (oobContent.contains("<calendar>")) {
            oobCalendar(oobContent);
        }

    }

    // open the camera
    public void oobapp() {
        List<String> iipkglist=ChatBot.iipkglist;
        List<String> iiapplist=ChatBot.iiapplist;
        ListIterator<String> iterator = iiapplist.listIterator();
        while (iterator.hasNext())
        {
            iterator.set(iterator.next().toLowerCase());
        }
        Log.e("oobapp: ",iiapplist.toString()+iipkglist );
        if (iiapplist.contains(appname))
        {
            pkgname=iipkglist.get(iiapplist.indexOf(appname));
            Intent LaunchIntent = c.getPackageManager().getLaunchIntentForPackage(pkgname);
            activity.startActivity( LaunchIntent );
        }
        else
            searchresult="It seems that no such app is installed.";
    }

    // open the camera
    public void oobCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // make the calendar event
    @SuppressLint("NewApi")
    public void oobCalendar(String oobContent) {
        //match and extract the content in the various fields within the calendar event
        Pattern bHTimePattern = Pattern.compile("<starthour>(.*)</starthour>");
        Matcher bHTimeMatcher = bHTimePattern.matcher(oobContent);
        Pattern bMTimePattern = Pattern.compile("<startminute>(.*)</startminute>");
        Matcher bMTimeMatcher = bMTimePattern.matcher(oobContent);
        Pattern eHTimePattern = Pattern.compile("<endhour>(.*)</endhour>");
        Matcher eHTimeMatcher = eHTimePattern.matcher(oobContent);
        Pattern eMTimePattern = Pattern.compile("<endminutes>(.*)</endminutes>");
        Matcher eMTimeMatcher = eMTimePattern.matcher(oobContent);
        Pattern dayPattern = Pattern.compile("<day>(.*)</day>");
        Matcher dayMatcher = dayPattern.matcher(oobContent);
        Pattern yearPattern = Pattern.compile("<year>(.*)</year>");
        Matcher yearMatcher = yearPattern.matcher(oobContent);
        Pattern monthPattern = Pattern.compile("<month>(.*)</month>");
        Matcher monthMatcher = monthPattern.matcher(oobContent);
        Pattern titlePattern = Pattern.compile("<title>(.*)</title>");
        Matcher titleMatcher = titlePattern.matcher(oobContent);
        Pattern locPattern = Pattern.compile("<location>(.*)</location>");
        Matcher locMatcher = locPattern.matcher(oobContent);
        if (bHTimeMatcher.find() && bMTimeMatcher.find() && eHTimeMatcher.find() && eMTimeMatcher.find() && dayMatcher.find() && yearMatcher.find() && monthMatcher.find() && titleMatcher.find() && locMatcher.find()) {
            Intent calendarIntent = new Intent(Intent.ACTION_EDIT);
            calendarIntent.setType("vnd.android.cursor.item/event");
            try {
                // create the calendar intent with the data as specified from the conversation
                int year = Integer.parseInt(yearMatcher.group(1).toString());
                int day = Integer.parseInt(dayMatcher.group(1).toString());
                int month = Integer.parseInt(monthMatcher.group(1).toString());
                int hourOfDay = Integer.parseInt(bHTimeMatcher.group(1).toString());
                int minute = Integer.parseInt(bMTimeMatcher.group(1).toString());
                Calendar beginTime = Calendar.getInstance();
                beginTime.set(year, month, day, hourOfDay, minute);
                hourOfDay = Integer.parseInt(eHTimeMatcher.group(1).toString());
                minute = Integer.parseInt(eMTimeMatcher.group(1).toString());
                Calendar endTime = Calendar.getInstance();
                endTime.set(year, month, day, hourOfDay, minute);
                calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis());
                calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());
                calendarIntent.putExtra(Events.TITLE, titleMatcher.group(1).toString());
                calendarIntent.putExtra(Events.EVENT_LOCATION, locMatcher.group(1).toString());
                activity.startActivity(calendarIntent);
            } catch (Exception ex) {
                //activity.processBotResponse("There was an error in scheduling your event.");
            }
        } //else activity.showBotResponse("There was an issue with one of the details you specificied. Please try and schedule your event again.");
    }
}
