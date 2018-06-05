package com.example.manu.retry;

import android.util.Log;

/**
 * Created by manum on 20-Mar-18.
 */

public class TextProcessor {
    static String matcher;
    public static String Process(String message){
        matcher=message;
        removespecialchars();
        wingscorrection();
        return matcher;
    }
    public static void removespecialchars(){
        matcher = matcher.replaceAll("[\\.\\-\\?\\!:,]"," ");
        matcher = matcher.replaceAll("(\\s{2,})"," ");
    }
    public static void wingscorrection(){
        matcher = matcher.replaceAll("(aaron|our wing|air wing|irving)","R wing");
        matcher = matcher.replaceAll("(a swing|swing)","S wing");
        matcher = matcher.replaceAll("(oven)","O wing");
        matcher = matcher.replaceAll("(feeling|paving|b wing)","P wing");
        matcher = matcher.replaceAll("(leaving|d wing|tawang)","T wing");
        matcher = matcher.replaceAll("(giving|jeevan)","G wing");
        matcher = matcher.replaceAll("(living|elving)","L wing");
    }
}
