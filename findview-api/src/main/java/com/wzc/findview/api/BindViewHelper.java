package com.wzc.findview.api;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Constructor;

public class BindViewHelper {

    public static void bind(Activity activity){
        try {
            Class<?> clazz = activity.getClass().getClassLoader().loadClass(activity.getClass().getCanonicalName() + "_ViewBinding");
            Constructor constructor = clazz.getConstructor(activity.getClass());
            constructor.newInstance(activity);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("wzc===", "Exception="+ e.getMessage());
        }
    }
}
