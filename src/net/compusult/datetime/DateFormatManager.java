/*
 * DateFormatManager.java
 * 
 * Copyright 2013, Compusult Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
   
package net.compusult.datetime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DateFormatManager {
	
	private static final String BASE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	
	/*
	 * YYYY-MM-DD
	 * T
	 * HH:MM:SS
	 * optionally .SSS for fractional seconds (could be e.g. .3 for 3/10 second, not always three digits)
	 * an optional timezone spec, either:
	 * Z for UTC, or
	 * + or -  HH:MM  where the colon is optional
	 */
	private static final Pattern DATE_TIME_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})(\\.(\\d{1,3}))?((([-+])(\\d{2}):?(\\d{2}))|Z)?$");

	private static final DateFormat ISO_DATE_FORMAT;
	private static final DateFormat ISO_DATE_FORMAT_WITH_TZ;
	private static final DateFormatManager INSTANCE;
	
	static {
		ISO_DATE_FORMAT = new SimpleDateFormat(BASE_DATE_FORMAT + "'Z'");
		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		ISO_DATE_FORMAT_WITH_TZ = new SimpleDateFormat(BASE_DATE_FORMAT + "Z");		// proper ISO format timezone (no colon)
		INSTANCE = new DateFormatManager();
	}
	
	
	public static DateFormatManager getInstance() {
		return INSTANCE;
	}

	public DateFormat getIsoDateTimeFormatter() {
		return (DateFormat) ISO_DATE_FORMAT.clone();
	}
	
	public String formatLocalAsUTC(Date in) {
		return getIsoDateTimeFormatter().format(in);
	}
	
	public Date parseUTCAsLocal(String in) throws ParseException {
		return getIsoDateTimeFormatter().parse(in);
	}
	
	/**
	 * Parse a string for the form YYYY-MM-DDTHH:MM:SS followed by a
	 * timezone specification, either 'Z' for UTC, or '-HH:MM' or '+HH:MM'
	 * to specify a timezone offset.
	 * 
	 * @param in
	 * @return
	 * @throws ParseException
	 */
	public Calendar parseDateTime(String in) throws ParseException {
		
		Date date;
		
		/*
		 * Massage the string to have a timezone spec in the form -HHMM (rather than -HH:MM),
		 * and to append a Z for UTC if no timezone was present at all.  Also remove fractional
		 * seconds. 
		 */
		Matcher m = DATE_TIME_PATTERN.matcher(in);
		if (! m.matches()) {
			throw new ParseException("Bad date/time \"" + in + "\"", 0);
		}
		
		String fracSeconds = m.group(3);
		int millis = 0;
		if (fracSeconds != null) {
			millis = Integer.parseInt(fracSeconds);
			if (fracSeconds.length() == 1) {
				millis *= 100;
			} else if (fracSeconds.length() == 2) {
				millis *= 10;
			}
		}
		
		/*
		 * Reconstruct a string suitable for SimpleDateFormat (no colon in the timezone, for example)
		 */
		in = m.group(1);
		if (m.group(5) != null) {		// matched +-HH:?MM
			in += m.group(6) + m.group(7) + m.group(8);
		} else {
			in += "Z";
		}
		
		if (in.endsWith("Z")) {
			synchronized (ISO_DATE_FORMAT) {
				date = ISO_DATE_FORMAT.parse(in);
			}
		} else {
			synchronized (ISO_DATE_FORMAT_WITH_TZ) {
				date = ISO_DATE_FORMAT_WITH_TZ.parse(in);
			}
		}
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTime(date);
		cal.roll(Calendar.MILLISECOND, millis);
		
		return cal;
	}
	
}
