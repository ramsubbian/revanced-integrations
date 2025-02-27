package app.revanced.integrations.videoplayer.videosettings;

import android.content.Context;


import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

/* loaded from: classes6.dex */
public class VideoQuality {
    public static final int[] videoResolutions = {0, 144, 240, 360, 480, 720, 1080, 1440, 2160};
    private static Boolean userChangedQuality = false;
    private static Boolean newVideo = false;


    public static void userChangedQuality() {
        userChangedQuality = true;
        newVideo = false;
    }

    public static int setVideoQuality(Object[] qualities, int quality, Object qInterface) {
        int preferredQuality;
        Field[] fields;
        if (!newVideo || userChangedQuality || qInterface == null) {
            if (SettingsEnum.DEBUG_BOOLEAN.getBoolean() && userChangedQuality) {
                LogHelper.debug(VideoQuality.class, "Skipping quality change because user changed it: " + quality);
            }
            userChangedQuality = false;
            return quality;
        }
        newVideo = false;
        LogHelper.debug(VideoQuality.class, "Quality: " + quality);
        Context context = ReVancedUtils.getContext();
        if (context == null) {
            LogHelper.printException(VideoQuality.class, "Context is null or settings not initialized, returning quality: " + quality);
            return quality;
        }
        if (Connectivity.isConnectedWifi(context)) {
            preferredQuality = SettingsEnum.PREFERRED_RESOLUTION_WIFI_INTEGER.getInt();
            LogHelper.debug(VideoQuality.class, "Wi-Fi connection detected, preferred quality: " + preferredQuality);
        } else if (Connectivity.isConnectedMobile(context)) {
            preferredQuality = SettingsEnum.PREFERRED_RESOLUTION_MOBILE_INTEGER.getInt();
            LogHelper.debug(VideoQuality.class, "Mobile data connection detected, preferred quality: " + preferredQuality);
        } else {
            LogHelper.debug(VideoQuality.class, "No Internet connection!");
            return quality;
        }
        if (preferredQuality == -2) {
            return quality;
        }
        Class<?> intType = Integer.TYPE;
        ArrayList<Integer> iStreamQualities = new ArrayList<>();
        try {
            for (Object streamQuality : qualities) {
                for (Field field : streamQuality.getClass().getFields()) {
                    if (field.getType().isAssignableFrom(intType)) {
                        int value = field.getInt(streamQuality);
                        if (field.getName().length() <= 2) {
                            iStreamQualities.add(value);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        Collections.sort(iStreamQualities);
        int index = 0;
        for (int streamQuality2 : iStreamQualities) {
            LogHelper.debug(VideoQuality.class, "Quality at index " + index + ": " + streamQuality2);
            index++;
        }
        for (Integer iStreamQuality : iStreamQualities) {
            int streamQuality3 = iStreamQuality;
            if (streamQuality3 <= preferredQuality) {
                quality = streamQuality3;
            }
        }
        if (quality == -2) {
            return quality;
        }
        int qualityIndex = iStreamQualities.indexOf(quality);
        LogHelper.debug(VideoQuality.class, "Index of quality " + quality + " is " + qualityIndex);
        try {
            Class<?> cl = qInterface.getClass();
            Method m = cl.getMethod("x", Integer.TYPE);
            m.invoke(qInterface, iStreamQualities.get(qualityIndex));
            LogHelper.debug(VideoQuality.class, "Quality changed to: " + qualityIndex);
            return qualityIndex;
        } catch (Exception ex) {
            LogHelper.printException(VideoQuality.class, "Failed to set quality", ex);
            return qualityIndex;
        }
    }
}
