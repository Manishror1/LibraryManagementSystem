package com.manish.librarysystemfinal;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "dcfqnh4vq");
        config.put("api_key", "146817948336889");
        config.put("api_secret", "8zIVxsMd_Gp0icp14V1jUtLKJ5w");


        MediaManager.init(this, config);
    }
}
