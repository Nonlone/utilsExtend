package per.nonlone.utils.datetime;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;


/**
 * 日期工具类, 继承org.apache.commons.lang.time.DateUtils类
 */
@Slf4j
public abstract class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    public static Date getStartTime(Date date) {
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }


    public static Date getEndTime(Date date) {
        return new DateTime(date).withTimeAtStartOfDay().plusDays(1).minus(1).toDate();
    }

    public static Date getTodayStartTime() {
        return DateTime.now().withTimeAtStartOfDay().plusDays(1).minus(1).toDate();
    }

    public static Date getTodayEndTime() {
        return DateTime.now().withTimeAtStartOfDay().plusDays(1).minus(1).toDate();
    }

    public static Date getPervMonth() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.add(Calendar.MONTH, -1);
        return todayStart.getTime();
    }

    public static String currentDateFormat(String format) {
        return FastDateFormat.getInstance(format).format(new Date());
    }

    /**
     * 格式化时间
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String format(Date date, String pattern) {
        return FastDateFormat.getInstance(pattern).format(date);
    }

    /**
     * 字符串转时间
     *
     * @param source
     * @param pattern
     * @return
     * @throws ParseException
     */
    public static Date pares(String source, String pattern) throws ParseException {
        return FastDateFormat.getInstance(pattern).parse(source);
    }
}