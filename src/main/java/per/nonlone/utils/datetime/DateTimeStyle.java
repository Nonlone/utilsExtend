package per.nonlone.utils.datetime;

/**
 * 时间格式类
 */
public abstract class DateTimeStyle {

    public final static String DEFAULT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public final static String DEFAULT_YYYY_MM_DD = "yyyy-MM-dd";
    public final static String DEFAULT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static String[] parsePatterns = {
            DEFAULT_YYYYMMDDHHMMSS,
            "yyyyMMdd",
            "yyyy年MM月dd日",
            "yyyy.MM.ddHH:mm:ss",
            "yyyyMM",
            "yyyy年MM月",
            "yyyy",
            "yyyy年M月",
            "yyyy年MM月dd"
    };

    public static String[] parsePatternsHyphen = {
            DEFAULT_YYYY_MM_DD_HH_MM_SS,
            "yyyy-MM-dd HH:mm",
            DEFAULT_YYYY_MM_DD,
            "yyyy-MM",
            "yyyy-M-dd",
            "yyyy-MM-d",
            "yyyy-M-d HH:mm:ss",
            "yyyy-MM-d HH:mm:ss",
            "yyyy-M-d",
            "yyyy-M-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-ddHH:mm:ss"
    };

    public static String[] parsePatternsSlash = {
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm",
            "yyyy/MM/dd"
    };

}
