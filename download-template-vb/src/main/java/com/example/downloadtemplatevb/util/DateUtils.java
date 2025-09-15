package com.example.downloadtemplatevb.util;

import org.apache.commons.lang3.StringUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author qzz
 * @date 2025/8/28
 */
public class DateUtils {
    public static Date getDateToString(String dateStr, String patten) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(patten, Locale.CHINA);

            try {
                return formatter.parse(dateStr);
            } catch (ParseException var4) {
                var4.printStackTrace();
                return null;
            }
        }
    }
}
