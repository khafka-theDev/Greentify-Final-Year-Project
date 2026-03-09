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
            config.put("cloud_name", "diirc1eey");
            config.put("api_key", "843851974229233");
            config.put("api_secret", "VCniMssyCUXFiqi4-0OSxfZ9Z6s");
            MediaManager.init(context, config);
            isInitialized = true;
        }
    }
}
