package com.example.saurabh.chat.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Utility {
    /*
     * Modified by Saurabh Joshi
     * Copyright 2012 Google Inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(Date date) {
        long time = date.getTime();

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now + MINUTE_MILLIS || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public static Date parseDateAsUTC(String date_str) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = null;

        try {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            d = df.parse(date_str);
        } catch(ParseException e) {
            e.printStackTrace();
        }

        return d;
    }

    public static String getAbbreviatedDateTime(Date date) {
        long time = date.getTime();

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now + MINUTE_MILLIS || time <= 0) {
            return null;
        }

        // https://stackoverflow.com/questions/2517709/comparing-two-dates-to-see-if-they-are-in-the-same-day
        Calendar dateCalendar = Calendar.getInstance();
        Calendar nowCalender = Calendar.getInstance();

        dateCalendar.setTime(date);
        nowCalender.setTimeInMillis(now);

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "Now";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " mins";
        } else if (diff < DAY_MILLIS) {
            return new SimpleDateFormat("h:mm a").format(date).replace("AM", "am").replace("PM", "pm");
        } else if (diff < 4 * DAY_MILLIS) {
            return new SimpleDateFormat("EEE h:mm a").format(date).replace("AM", "am").replace("PM", "pm");
        } else if(dateCalendar.get(Calendar.YEAR) == nowCalender.get(Calendar.YEAR)) {
            return new SimpleDateFormat("EEE d MMM h:mm a").format(date).replace("AM", "am").replace("PM", "pm");
        }

        return new SimpleDateFormat("EEE d MMM yyyy h:mm a").format(date).replace("AM", "am").replace("PM", "pm");
    }
}
