/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.hadoop.yarn.util;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience.Private;

@Private
public class Times {
  private static final Log LOG = LogFactory.getLog(Times.class);

  static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  // This format should match the one used in yarn.dt.plugins.js
  static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy").withZone(
          ZoneId.systemDefault());

  static final DateTimeFormatter ISO_OFFSET_DATE_TIME =
      DateTimeFormatter.ofPattern(ISO8601_DATE_FORMAT).withZone(
          ZoneId.systemDefault());

  public static long elapsed(long started, long finished) {
    return Times.elapsed(started, finished, true);
  }

  // A valid elapsed is supposed to be non-negative. If finished/current time
  // is ahead of the started time, return -1 to indicate invalid elapsed time,
  // and record a warning log.
  public static long elapsed(long started, long finished, boolean isRunning) {
    if (finished > 0 && started > 0) {
      long elapsed = finished - started;
      if (elapsed >= 0) {
        return elapsed;
      } else {
        LOG.warn("Finished time " + finished
            + " is ahead of started time " + started);
        return -1;
      }
    }
    if (isRunning) {
      long current = System.currentTimeMillis();
      long elapsed = started > 0 ? current - started : 0;
      if (elapsed >= 0) {
        return elapsed;
      } else {
        LOG.warn("Current time " + current
            + " is ahead of started time " + started);
        return -1;
      }
    } else {
      return -1;
    }
  }

  public static String format(long ts) {
    return ts > 0 ? DATE_FORMAT.format(Instant.ofEpochMilli(ts)) : "N/A";
  }

  /**
   * Given a time stamp returns ISO-8601 formated string in format
   * "yyyy-MM-dd'T'HH:mm:ss.SSSZ".
   * @param ts to be formatted in ISO format.
   * @return ISO 8601 formatted string.
   */
  public static String formatISO8601(long ts) {
    return ISO_OFFSET_DATE_TIME.format(Instant.ofEpochMilli(ts));
  }

  /**
   * Given ISO formatted string with format "yyyy-MM-dd'T'HH:mm:ss.SSSZ", return
   * epoch time for local Time zone.
   * @param isoString in format of "yyyy-MM-dd'T'HH:mm:ss.SSSZ".
   * @return epoch time for local time zone.
   * @throws ParseException if given ISO formatted string can not be parsed.
   */
  public static long parseISO8601ToLocalTimeInMillis(String isoString)
      throws ParseException {
    if (isoString == null) {
      throw new ParseException("Invalid input.", -1);
    }
    return Instant.from(ISO_OFFSET_DATE_TIME.parse(isoString)).toEpochMilli();
  }
}
