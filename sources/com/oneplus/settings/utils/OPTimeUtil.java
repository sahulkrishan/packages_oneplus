package com.oneplus.settings.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OPTimeUtil {
    public static String UnixTimeRead(long time) {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(1000 * time));
    }

    public static String millsTimeRead(long time) {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(time));
    }
}
