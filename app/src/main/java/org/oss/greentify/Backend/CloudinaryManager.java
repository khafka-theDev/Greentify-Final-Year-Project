// CloudinaryManager.java
package org.oss.greentify.Backend;

import android.content.Context;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "");
            config.put("api_key", "");
            config.put("api_secret", "");
            MediaManager.init(context, config);
            isInitialized = true;
        }
    }
}
