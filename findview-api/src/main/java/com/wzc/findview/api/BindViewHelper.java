package com.wzc.findview.api;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Constructor;

public class BindViewHelper {

    private static boolean mShowLog = false;

    public static void bind(Activity activity){
        try {
            loadSwitch();
            if (mShowLog){
                Log.d("wzc","gradle plugin modified this variable");
            }
            Class<?> clazz = activity.getClass().getClassLoader().loadClass(activity.getClass().getCanonicalName() + "_ViewBinding");
            Constructor constructor = clazz.getConstructor(activity.getClass());
            constructor.newInstance(activity);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("wzc===", "Exception="+ e.getMessage());
        }
    }

    private static void loadSwitch() {
        mShowLog = false;
    }
}
