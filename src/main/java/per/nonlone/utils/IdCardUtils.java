package per.nonlone.utils;

import per.nonlone.utils.datetime.DateTimeStyle;
import per.nonlone.utils.datetime.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 身份证工具类
 *
 * @author June
 * @version 1.0, 2010-06-17
 */
@Slf4j
public abstract class IdCardUtils extends StringUtils {

    /**
     * 中国公民身份证号码最小长度。
     */
    private static final int CHINA_ID_MIN_LENGTH = 15;
    /**
     * 中国公民身份证号码最大长度。
     */
    private static final int CHINA_ID_MAX_LENGTH = 18;

    /**
     * 每位加权因子
     */
    private static final int[] POWER = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    /**
     * 第18位校检码
     */
    private static final String[] VERIFY_CODE = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
    /**
     * 最低年限
     */
    private static final int MIN = 1930;

    /**
     * 省份信息
     */
    private static final Map<Integer, String> PROVINCE_CODES = new HashMap<Integer, String>() {{
        this.put(11, "北京");
        this.put(12, "天津");
        this.put(13, "河北");
        this.put(14, "山西");
        this.put(15, "内蒙古");
        this.put(21, "辽宁");
        this.put(22, "吉林");
        this.put(23, "黑龙江");
        this.put(31, "上海");
        this.put(32, "江苏");
        this.put(33, "浙江");
        this.put(34, "安徽");
        this.put(35, "福建");
        this.put(36, "江西");
        this.put(37, "山东");
        this.put(41, "河南");
        this.put(42, "湖北");
        this.put(43, "湖南");
        this.put(44, "广东");
        this.put(45, "广西");
        this.put(46, "海南");
        this.put(50, "重庆");
        this.put(51, "四川");
        this.put(52, "贵州");
        this.put(53, "云南");
        this.put(54, "西藏");
        this.put(61, "陕西");
        this.put(62, "甘肃");
        this.put(63, "青海");
        this.put(64, "宁夏");
        this.put(65, "新疆");
        this.put(71, "台湾");
        this.put(81, "香港");
        this.put(82, "澳门");
        this.put(90, "国外");
    }};

    /**
     * 城市编码
     */
    private static final Map<Integer, String> CITY_CODES = new HashMap<Integer, String>() {{
        this.put(1100, "北京市");
        this.put(1200, "天津市");
        this.put(1300, "河北省");
        this.put(1301, "石家庄市");
        this.put(1302, "唐山市");
        this.put(1303, "秦皇岛市");
        this.put(1304, "邯郸市");
        this.put(1305, "邢台市");
        this.put(1306, "保定市");
        this.put(1307, "张家口市");
        this.put(1308, "承德市");
        this.put(1309, "沧州市");
        this.put(1310, "廊坊市");
        this.put(1311, "衡水市");
        this.put(1400, "山西省");
        this.put(1401, "太原市");
        this.put(1402, "大同市");
        this.put(1403, "阳泉市");
        this.put(1404, "长治市");
        this.put(1405, "晋城市");
        this.put(1406, "朔州市");
        this.put(1407, "晋中市");
        this.put(1408, "运城市");
        this.put(1409, "忻州市");
        this.put(1410, "临汾市");
        this.put(1411, "吕梁市");
        this.put(1500, "内蒙古自治区");
        this.put(1501, "呼和浩特市");
        this.put(1502, "包头市");
        this.put(1503, "乌海市");
        this.put(1504, "赤峰市");
        this.put(1505, "通辽市");
        this.put(1506, "鄂尔多斯市");
        this.put(1507, "呼伦贝尔市");
        this.put(1508, "巴彦淖尔市");
        this.put(1509, "乌兰察布市");
        this.put(1522, "兴安盟");
        this.put(1525, "锡林郭勒盟");
        this.put(1529, "阿拉善盟");
        this.put(2100, "辽宁省");
        this.put(2101, "沈阳市");
        this.put(2102, "大连市");
        this.put(2103, "鞍山市");
        this.put(2104, "抚顺市");
        this.put(2105, "本溪市");
        this.put(2106, "丹东市");
        this.put(2107, "锦州市");
        this.put(2108, "营口市");
        this.put(2109, "阜新市");
        this.put(2110, "辽阳市");
        this.put(2111, "盘锦市");
        this.put(2112, "铁岭市");
        this.put(2113, "朝阳市");
        this.put(2114, "葫芦岛市");
        this.put(2200, "吉林省");
        this.put(2201, "长春市");
        this.put(2202, "吉林市");
        this.put(2203, "四平市");
        this.put(2204, "辽源市");
        this.put(2205, "通化市");
        this.put(2206, "白山市");
        this.put(2207, "松原市");
        this.put(2208, "白城市");
        this.put(2224, "延边朝鲜族自治州");
        this.put(2300, "黑龙江省");
        this.put(2301, "哈尔滨市");
        this.put(2302, "齐齐哈尔市");
        this.put(2303, "鸡西市");
        this.put(2304, "鹤岗市");
        this.put(2305, "双鸭山市");
        this.put(2306, "大庆市");
        this.put(2307, "伊春市");
        this.put(2308, "佳木斯市");
        this.put(2309, "七台河市");
        this.put(2310, "牡丹江市");
        this.put(2311, "黑河市");
        this.put(2312, "绥化市");
        this.put(2327, "大兴安岭地区");
        this.put(3100, "上海市");
        this.put(3200, "江苏省");
        this.put(3201, "南京市");
        this.put(3202, "无锡市");
        this.put(3203, "徐州市");
        this.put(3204, "常州市");
        this.put(3205, "苏州市");
        this.put(3206, "南通市");
        this.put(3207, "连云港市");
        this.put(3208, "淮安市");
        this.put(3209, "盐城市");
        this.put(3210, "扬州市");
        this.put(3211, "镇江市");
        this.put(3212, "泰州市");
        this.put(3213, "宿迁市");
        this.put(3300, "浙江省");
        this.put(3301, "杭州市");
        this.put(3302, "宁波市");
        this.put(3303, "温州市");
        this.put(3304, "嘉兴市");
        this.put(3305, "湖州市");
        this.put(3306, "绍兴市");
        this.put(3307, "金华市");
        this.put(3308, "衢州市");
        this.put(3309, "舟山市");
        this.put(3310, "台州市");
        this.put(3311, "丽水市");
        this.put(3400, "安徽省");
        this.put(3401, "合肥市");
        this.put(3402, "芜湖市");
        this.put(3403, "蚌埠市");
        this.put(3404, "淮南市");
        this.put(3405, "马鞍山市");
        this.put(3406, "淮北市");
        this.put(3407, "铜陵市");
        this.put(3408, "安庆市");
        this.put(3410, "黄山市");
        this.put(3411, "滁州市");
        this.put(3412, "阜阳市");
        this.put(3413, "宿州市");
        this.put(3415, "六安市");
        this.put(3416, "亳州市");
        this.put(3417, "池州市");
        this.put(3418, "宣城市");
        this.put(3500, "福建省");
        this.put(3501, "福州市");
        this.put(3502, "厦门市");
        this.put(3503, "莆田市");
        this.put(3504, "三明市");
        this.put(3505, "泉州市");
        this.put(3506, "漳州市");
        this.put(3507, "南平市");
        this.put(3508, "龙岩市");
        this.put(3509, "宁德市");
        this.put(3600, "江西省");
        this.put(3601, "南昌市");
        this.put(3602, "景德镇市");
        this.put(3603, "萍乡市");
        this.put(3604, "九江市");
        this.put(3605, "新余市");
        this.put(3606, "鹰潭市");
        this.put(3607, "赣州市");
        this.put(3608, "吉安市");
        this.put(3609, "宜春市");
        this.put(3610, "抚州市");
        this.put(3611, "上饶市");
        this.put(3700, "山东省");
        this.put(3701, "济南市");
        this.put(3702, "青岛市");
        this.put(3703, "淄博市");
        this.put(3704, "枣庄市");
        this.put(3705, "东营市");
        this.put(3706, "烟台市");
        this.put(3707, "潍坊市");
        this.put(3708, "济宁市");
        this.put(3709, "泰安市");
        this.put(3710, "威海市");
        this.put(3711, "日照市");
        this.put(3712, "莱芜市");
        this.put(3713, "临沂市");
        this.put(3714, "德州市");
        this.put(3715, "聊城市");
        this.put(3716, "滨州市");
        this.put(3717, "菏泽市");
        this.put(4100, "河南省");
        this.put(4101, "郑州市");
        this.put(4102, "开封市");
        this.put(4103, "洛阳市");
        this.put(4104, "平顶山市");
        this.put(4105, "安阳市");
        this.put(4106, "鹤壁市");
        this.put(4107, "新乡市");
        this.put(4108, "焦作市");
        this.put(4109, "濮阳市");
        this.put(4110, "许昌市");
        this.put(4111, "漯河市");
        this.put(4112, "三门峡市");
        this.put(4113, "南阳市");
        this.put(4114, "商丘市");
        this.put(4115, "信阳市");
        this.put(4116, "周口市");
        this.put(4117, "驻马店市");
        this.put(4200, "湖北省");
        this.put(4201, "武汉市");
        this.put(4202, "黄石市");
        this.put(4203, "十堰市");
        this.put(4205, "宜昌市");
        this.put(4206, "襄阳市");
        this.put(4207, "鄂州市");
        this.put(4208, "荆门市");
        this.put(4209, "孝感市");
        this.put(4210, "荆州市");
        this.put(4211, "黄冈市");
        this.put(4212, "咸宁市");
        this.put(4213, "随州市");
        this.put(4228, "恩施土家族苗族自治州");
        this.put(4300, "湖南省");
        this.put(4301, "长沙市");
        this.put(4302, "株洲市");
        this.put(4303, "湘潭市");
        this.put(4304, "衡阳市");
        this.put(4305, "邵阳市");
        this.put(4306, "岳阳市");
        this.put(4307, "常德市");
        this.put(4308, "张家界市");
        this.put(4309, "益阳市");
        this.put(4310, "郴州市");
        this.put(4311, "永州市");
        this.put(4312, "怀化市");
        this.put(4313, "娄底市");
        this.put(4331, "湘西土家族苗族自治州");
        this.put(4400, "广东省");
        this.put(4401, "广州市");
        this.put(4402, "韶关市");
        this.put(4403, "深圳市");
        this.put(4404, "珠海市");
        this.put(4405, "汕头市");
        this.put(4406, "佛山市");
        this.put(4407, "江门市");
        this.put(4408, "湛江市");
        this.put(4409, "茂名市");
        this.put(4412, "肇庆市");
        this.put(4413, "惠州市");
        this.put(4414, "梅州市");
        this.put(4415, "汕尾市");
        this.put(4416, "河源市");
        this.put(4417, "阳江市");
        this.put(4418, "清远市");
        this.put(4419, "东莞市");
        this.put(4420, "中山市");
        this.put(4451, "潮州市");
        this.put(4452, "揭阳市");
        this.put(4453, "云浮市");
        this.put(4500, "广西壮族自治区");
        this.put(4501, "南宁市");
        this.put(4502, "柳州市");
        this.put(4503, "桂林市");
        this.put(4504, "梧州市");
        this.put(4505, "北海市");
        this.put(4506, "防城港市");
        this.put(4507, "钦州市");
        this.put(4508, "贵港市");
        this.put(4509, "玉林市");
        this.put(4510, "百色市");
        this.put(4511, "贺州市");
        this.put(4512, "河池市");
        this.put(4513, "来宾市");
        this.put(4514, "崇左市");
        this.put(4600, "海南省");
        this.put(4601, "海口市");
        this.put(4602, "三亚市");
        this.put(4603, "三沙市");
        this.put(4604, "儋州市");
        this.put(5000, "重庆市");
        this.put(5002, "重庆市郊县");
        this.put(5100, "四川省");
        this.put(5101, "成都市");
        this.put(5103, "自贡市");
        this.put(5104, "攀枝花市");
        this.put(5105, "泸州市");
        this.put(5106, "德阳市");
        this.put(5107, "绵阳市");
        this.put(5108, "广元市");
        this.put(5109, "遂宁市");
        this.put(5110, "内江市");
        this.put(5111, "乐山市");
        this.put(5113, "南充市");
        this.put(5114, "眉山市");
        this.put(5115, "宜宾市");
        this.put(5116, "广安市");
        this.put(5117, "达州市");
        this.put(5118, "雅安市");
        this.put(5119, "巴中市");
        this.put(5120, "资阳市");
        this.put(5132, "阿坝藏族羌族自治州");
        this.put(5133, "甘孜藏族自治州");
        this.put(5134, "凉山彝族自治州");
        this.put(5200, "贵州省");
        this.put(5201, "贵阳市");
        this.put(5202, "六盘水市");
        this.put(5203, "遵义市");
        this.put(5204, "安顺市");
        this.put(5205, "毕节市");
        this.put(5206, "铜仁市");
        this.put(5223, "黔西南布依族苗族自治州");
        this.put(5226, "黔东南苗族侗族自治州");
        this.put(5227, "黔南布依族苗族自治州");
        this.put(5300, "云南省");
        this.put(5301, "昆明市");
        this.put(5303, "曲靖市");
        this.put(5304, "玉溪市");
        this.put(5305, "保山市");
        this.put(5306, "昭通市");
        this.put(5307, "丽江市");
        this.put(5308, "普洱市");
        this.put(5309, "临沧市");
        this.put(5323, "楚雄彝族自治州");
        this.put(5325, "红河哈尼族彝族自治州");
        this.put(5326, "文山壮族苗族自治州");
        this.put(5328, "西双版纳傣族自治州");
        this.put(5329, "大理白族自治州");
        this.put(5331, "德宏傣族景颇族自治州");
        this.put(5333, "怒江傈僳族自治州");
        this.put(5334, "迪庆藏族自治州");
        this.put(5400, "西藏自治区");
        this.put(5401, "拉萨市");
        this.put(5402, "日喀则市");
        this.put(5403, "昌都市");
        this.put(5404, "林芝市");
        this.put(5405, "山南市");
        this.put(5406, "那曲市");
        this.put(5425, "阿里地区");
        this.put(6100, "陕西省");
        this.put(6101, "西安市");
        this.put(6102, "铜川市");
        this.put(6103, "宝鸡市");
        this.put(6104, "咸阳市");
        this.put(6105, "渭南市");
        this.put(6106, "延安市");
        this.put(6107, "汉中市");
        this.put(6108, "榆林市");
        this.put(6109, "安康市");
        this.put(6110, "商洛市");
        this.put(6200, "甘肃省");
        this.put(6201, "兰州市");
        this.put(6202, "嘉峪关市");
        this.put(6203, "金昌市");
        this.put(6204, "白银市");
        this.put(6205, "天水市");
        this.put(6206, "武威市");
        this.put(6207, "张掖市");
        this.put(6208, "平凉市");
        this.put(6209, "酒泉市");
        this.put(6210, "庆阳市");
        this.put(6211, "定西市");
        this.put(6212, "陇南市");
        this.put(6229, "临夏回族自治州");
        this.put(6230, "甘南藏族自治州");
        this.put(6300, "青海省");
        this.put(6301, "西宁市");
        this.put(6302, "海东市");
        this.put(6322, "海北藏族自治州");
        this.put(6323, "黄南藏族自治州");
        this.put(6325, "海南藏族自治州");
        this.put(6326, "果洛藏族自治州");
        this.put(6327, "玉树藏族自治州");
        this.put(6328, "海西蒙古族藏族自治州");
        this.put(6400, "宁夏回族自治区");
        this.put(6401, "银川市");
        this.put(6402, "石嘴山市");
        this.put(6403, "吴忠市");
        this.put(6404, "固原市");
        this.put(6405, "中卫市");
        this.put(6500, "新疆维吾尔自治区");
        this.put(6501, "乌鲁木齐市");
        this.put(6502, "克拉玛依市");
        this.put(6504, "吐鲁番市");
        this.put(6505, "哈密市");
        this.put(6523, "昌吉回族自治州");
        this.put(6527, "博尔塔拉蒙古自治州");
        this.put(6528, "巴音郭楞蒙古自治州");
        this.put(6529, "阿克苏地区");
        this.put(6530, "克孜勒苏柯尔克孜自治州");
        this.put(6531, "喀什地区");
        this.put(6532, "和田地区");
        this.put(6540, "伊犁哈萨克自治州");
        this.put(6542, "塔城地区");
        this.put(6543, "阿勒泰地区");
        this.put(7100, "台湾省");
        this.put(8100, "香港特别行政区");
        this.put(8200, "澳门特别行政区");
        this.put(9000, "外国");
    }};


    /**
     * 台湾身份首字母对应数字
     */
    private static final Map<String, Integer> TW_FIRST_CODE = new HashMap<String, Integer>() {{
        this.put("A", 10);
        this.put("B", 11);
        this.put("C", 12);
        this.put("D", 13);
        this.put("E", 14);
        this.put("F", 15);
        this.put("G", 16);
        this.put("H", 17);
        this.put("J", 18);
        this.put("K", 19);
        this.put("L", 20);
        this.put("M", 21);
        this.put("N", 22);
        this.put("P", 23);
        this.put("Q", 24);
        this.put("R", 25);
        this.put("S", 26);
        this.put("T", 27);
        this.put("U", 28);
        this.put("V", 29);
        this.put("X", 30);
        this.put("Y", 31);
        this.put("W", 32);
        this.put("Z", 33);
        this.put("I", 34);
        this.put("O", 35);
    }};
    /**
     * 香港身份首字母对应数字
     */
    private static final Map<String, Integer> HK_FIRST_CODE = new HashMap<String, Integer>() {{
        this.put("A", 1);
        this.put("B", 2);
        this.put("C", 3);
        this.put("R", 18);
        this.put("U", 21);
        this.put("Z", 26);
        this.put("X", 24);
        this.put("W", 23);
        this.put("O", 15);
        this.put("N", 14);
    }};


    /**
     * 将15位身份证号码转换为18位
     *
     * @param idCard 15位身份编码
     * @return 18位身份编码
     */
    public static String conver15CardTo18(String idCard) {
        String idCard18 = "";
        if (idCard.length() != CHINA_ID_MIN_LENGTH) {
            return null;
        }
        if (isNum(idCard)) {
            // 获取出生年月日
            String birthday = idCard.substring(6, 12);
            Date birthDate = null;
            try {
                birthDate = new SimpleDateFormat("yyMMdd").parse(birthday);
            } catch (ParseException e) {
                log.error("", e);
            }
            Calendar cal = Calendar.getInstance();
            if (birthDate != null) {
                cal.setTime(birthDate);
            }
            // 获取出生年(完全表现形式,如：2010)
            String sYear = String.valueOf(cal.get(Calendar.YEAR));
            idCard18 = idCard.substring(0, 6) + sYear + idCard.substring(8);
            // 转换字符数组
            char[] cArr = idCard18.toCharArray();
            if (cArr != null) {
                int[] iCard = converCharToInt(cArr);
                int iSum17 = getPowerSum(iCard);
                // 获取校验位
                String sVal = getCheckCode18(iSum17);
                if (sVal.length() > 0) {
                    idCard18 += sVal;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
        return idCard18;
    }

    /**
     * 验证身份证是否合法
     */
    public static boolean validateCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return false;
        }
        String card = idCard.trim();
        if (validateIdCard18(card)) {
            return true;
        }
        if (validateIdCard15(card)) {
            return true;
        }
        String[] cardval = validateIdCard10(card);
        if (cardval != null) {
            if ("true".equals(cardval[2])) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证18位身份编码是否合法
     *
     * @param idCard 身份编码
     * @return 是否合法
     */
    public static boolean validateIdCard18(String idCard) {
        boolean bTrue = false;
        if (idCard.length() == CHINA_ID_MAX_LENGTH) {
            // 前17位
            String code17 = idCard.substring(0, 17);
            // 第18位
            String code18 = idCard.substring(17, CHINA_ID_MAX_LENGTH);
            if (isNum(code17)) {
                char[] cArr = code17.toCharArray();
                if (cArr != null) {
                    int[] iCard = converCharToInt(cArr);
                    int iSum17 = getPowerSum(iCard);
                    // 获取校验位
                    String val = getCheckCode18(iSum17);
                    if (val.length() > 0) {
                        if (val.equalsIgnoreCase(code18)) {
                            bTrue = true;
                        }
                    }
                }
            }
        }
        return bTrue;
    }

    /**
     * 验证15位身份编码是否合法
     *
     * @param idCard 身份编码
     * @return 是否合法
     */
    public static boolean validateIdCard15(String idCard) {
        if (idCard.length() != CHINA_ID_MIN_LENGTH) {
            return false;
        }
        if (isNum(idCard)) {
            String proCode = idCard.substring(0, 2);
            if (PROVINCE_CODES.get(proCode) == null) {
                return false;
            }
            String birthCode = idCard.substring(6, 12);
            Date birthDate = null;
            try {
                birthDate = new SimpleDateFormat("yy").parse(birthCode.substring(0, 2));
            } catch (ParseException e) {
                log.error("", e);
            }
            Calendar cal = Calendar.getInstance();
            if (birthDate != null) {
                cal.setTime(birthDate);
            }
            if (!valiDate(cal.get(Calendar.YEAR), Integer.valueOf(birthCode.substring(2, 4)),
                    Integer.valueOf(birthCode.substring(4, 6)))) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * 验证10位身份编码是否合法
     *
     * @param idCard 身份编码
     * @return 身份证信息数组
     * <p>
     * [0] - 台湾、澳门、香港 [1] - 性别(男M,女F,未知N) [2] - 是否合法(合法true,不合法false)
     * 若不是身份证件号码则返回null
     * </p>
     */
    public static String[] validateIdCard10(String idCard) {
        String[] info = new String[3];
        String card = idCard.replaceAll("[\\(|\\)]", "");
        if (card.length() != 8 && card.length() != 9 && idCard.length() != 10) {
            return null;
        }
        // 台湾
        if (idCard.matches("^[a-zA-Z][0-9]{9}$")) {
            info[0] = "台湾";
            String char2 = idCard.substring(1, 2);
            if ("1".equals(char2)) {
                info[1] = "M";
            } else if ("2".equals(char2)) {
                info[1] = "F";
            } else {
                info[1] = "N";
                info[2] = "false";
                return info;
            }
            info[2] = validateTWCard(idCard) ? "true" : "false";
        } else if (idCard.matches("^[1|5|7][0-9]{6}\\(?[0-9A-Z]\\)?$")) {
            // 澳门
            info[0] = "澳门";
            info[1] = "N";
        } else if (idCard.matches("^[A-Z]{1,2}[0-9]{6}\\(?[0-9A]\\)?$")) {
            // 香港
            info[0] = "香港";
            info[1] = "N";
            info[2] = validateHKCard(idCard) ? "true" : "false";
        } else {
            return null;
        }
        return info;
    }

    /**
     * 验证台湾身份证号码
     *
     * @param idCard 身份证号码
     * @return 验证码是否符合
     */
    public static boolean validateTWCard(String idCard) {
        String start = idCard.substring(0, 1);
        String mid = idCard.substring(1, 9);
        String end = idCard.substring(9, 10);
        Integer iStart = TW_FIRST_CODE.get(start);
        Integer sum = iStart / 10 + (iStart % 10) * 9;
        char[] chars = mid.toCharArray();
        Integer iflag = 8;
        for (char c : chars) {
            sum = sum + Integer.valueOf(c + "") * iflag;
            iflag--;
        }
        return (sum % 10 == 0 ? 0 : (10 - sum % 10)) == Integer.valueOf(end) ? true : false;
    }

    /**
     * 验证香港身份证号码(存在Bug，部份特殊身份证无法检查)
     * <p>
     * 身份证前2位为英文字符，如果只出现一个英文字符则表示第一位是空格，对应数字58 前2位英文字符A-Z分别对应数字10-35
     * 最后一位校验码为0-9的数字加上字符"A"，"A"代表10
     * </p>
     * <p>
     * 将身份证号码全部转换为数字，分别对应乘9-1相加的总和，整除11则证件号码有效
     * </p>
     *
     * @param idCard 身份证号码
     * @return 验证码是否符合
     */
    public static boolean validateHKCard(String idCard) {
        String card = idCard.replaceAll("[\\(|\\)]", "");
        Integer sum = 0;
        if (card.length() == 9) {
            sum = (Integer.valueOf(card.substring(0, 1).toUpperCase().toCharArray()[0]) - 55) * 9
                    + (Integer.valueOf(card.substring(1, 2).toUpperCase().toCharArray()[0]) - 55) * 8;
            card = card.substring(1, 9);
        } else {
            sum = 522 + (Integer.valueOf(card.substring(0, 1).toUpperCase().toCharArray()[0]) - 55) * 8;
        }
        String mid = card.substring(1, 7);
        String end = card.substring(7, 8);
        char[] chars = mid.toCharArray();
        Integer iflag = 7;
        for (char c : chars) {
            sum = sum + Integer.valueOf(c + "") * iflag;
            iflag--;
        }
        if ("A".equals(end.toUpperCase())) {
            sum = sum + 10;
        } else {
            sum = sum + Integer.valueOf(end);
        }
        return (sum % 11 == 0) ? true : false;
    }

    /**
     * 将字符数组转换成数字数组
     *
     * @param ca 字符数组
     * @return 数字数组
     */
    public static int[] converCharToInt(char[] ca) {
        int len = ca.length;
        int[] iArr = new int[len];
        try {
            for (int i = 0; i < len; i++) {
                iArr[i] = Integer.parseInt(String.valueOf(ca[i]));
            }
        } catch (NumberFormatException e) {
            log.error("converCharToInt", e);
        }
        return iArr;
    }

    /**
     * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
     *
     * @param iArr
     * @return 身份证编码。
     */
    public static int getPowerSum(int[] iArr) {
        int iSum = 0;
        if (POWER.length == iArr.length) {
            for (int i = 0; i < iArr.length; i++) {
                for (int j = 0; j < POWER.length; j++) {
                    if (i == j) {
                        iSum = iSum + iArr[i] * POWER[j];
                    }
                }
            }
        }
        return iSum;
    }

    /**
     * 将power和值与11取模获得余数进行校验码判断
     *
     * @param iSum
     * @return 校验位
     */
    public static String getCheckCode18(int iSum) {
        String sCode = "";
        switch (iSum % 11) {
            case 10:
                sCode = "2";
                break;
            case 9:
                sCode = "3";
                break;
            case 8:
                sCode = "4";
                break;
            case 7:
                sCode = "5";
                break;
            case 6:
                sCode = "6";
                break;
            case 5:
                sCode = "7";
                break;
            case 4:
                sCode = "8";
                break;
            case 3:
                sCode = "9";
                break;
            case 2:
                sCode = "x";
                break;
            case 1:
                sCode = "0";
                break;
            case 0:
                sCode = "1";
                break;
        }
        return sCode;
    }

    /**
     * 根据身份编号获取年龄，按周岁算
     *
     * @param idCard 身份编号
     * @return 年龄
     */
    public static int getAgeByIdCard(String idCard) {
        String birthByIdCard = IdCardUtils.getBirthByIdCard(idCard);
        Calendar birthDay = null;
        try {
            Date date = FastDateFormat.getInstance("yyyyMMdd").parse(birthByIdCard);
            birthDay = Calendar.getInstance();
            birthDay.setTime(date);
        } catch (ParseException e) {
            log.error("Fail to parse birthDay by idCard {}", idCard);
            return -1;
        }

        Calendar now = Calendar.getInstance();
        int age = 0;
        //如果传入的时间，在当前时间的后面，返回0岁
        if (now.before(birthDay)) {
            age = 0;
        } else {

            int yearNow = now.get(Calendar.YEAR);
            int monthNow = now.get(Calendar.MONTH);
            int dayOfMonthNow = now.get(Calendar.DAY_OF_MONTH);

            int yearBirth = birthDay.get(Calendar.YEAR);
            int monthBirth = birthDay.get(Calendar.MONTH);
            int dayOfMonthBirth = birthDay.get(Calendar.DAY_OF_MONTH);

            age = yearNow - yearBirth;

            //如果今年的生日没有过，则表示未满周岁
            if (monthNow <= monthBirth) {
                if (monthNow == monthBirth) {
                    if (dayOfMonthNow < dayOfMonthBirth) {
                        age--;
                    }
                } else {
                    age--;
                }
            }
        }

        return age;
    }

    /**
     * 根据身份编号获取生日
     *
     * @param idCard 身份编号
     * @return 生日(yyyyMMdd)
     */
    public static Date getBirthdayByIdCard(String idCard) {
        Integer len = idCard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        try {
            return DateUtils.pares(idCard.substring(6, 14), DateTimeStyle.parsePatterns[1]);
        } catch (ParseException pe) {
            log.error("getBirthdayByIdCard", pe);
        }
        return null;
    }


    /**
     * 根据身份编号获取生日
     *
     * @param idCard 身份编号
     * @return 生日(yyyyMMdd)
     */
    public static String getBirthByIdCard(String idCard) {
        Integer len = idCard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        return idCard.substring(6, 14);
    }

    /**
     * 根据身份编号获取生日年
     *
     * @param idCard 身份编号
     * @return 生日(yyyy)
     */
    public static Short getYearByIdCard(String idCard) {
        Integer len = idCard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        return Short.valueOf(idCard.substring(6, 10));
    }

    /**
     * 根据身份编号获取生日月
     *
     * @param idCard 身份编号
     * @return 生日(MM)
     */
    public static Short getMonthByIdCard(String idCard) {
        Integer len = idCard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        return Short.valueOf(idCard.substring(10, 12));
    }

    /**
     * 根据身份编号获取生日天
     *
     * @param idCard 身份编号
     * @return 生日(dd)
     */
    public static Short getDateByIdCard(String idCard) {
        Integer len = idCard.length();
        if (len < CHINA_ID_MIN_LENGTH) {
            return null;
        } else if (len == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        return Short.valueOf(idCard.substring(12, 14));
    }

    /**
     * 根据身份编号获取性别
     *
     * @param idCard 身份编号
     * @return 性别(M - 男 ， F - 女 ， N - 未知)
     */
    public static String getGenderByIdCard(String idCard) {
        String sGender = "N";
        if (idCard.length() == CHINA_ID_MIN_LENGTH) {
            idCard = conver15CardTo18(idCard);
        }
        String sCardNum = idCard.substring(16, 17);
        if (Integer.parseInt(sCardNum) % 2 != 0) {
            sGender = "M";
        } else {
            sGender = "F";
        }
        return sGender;
    }

    /**
     * 根据身份编号获取户籍省份
     *
     * @param idCard 身份编码
     * @return 省级编码。
     */
    public static String getProvinceByIdCard(String idCard) {
        int len = idCard.length();
        String sProvince = null;
        String sProvinNum = "";
        if (len == CHINA_ID_MIN_LENGTH || len == CHINA_ID_MAX_LENGTH) {
            sProvinNum = idCard.substring(0, 2);
        }
        sProvince = PROVINCE_CODES.get(sProvinNum);
        return sProvince;
    }

    /**
     * 根据身份编号获取户籍省份
     *
     * @param idCard 身份编码
     * @return 省级编码。
     */
    public static String geCityByIdCard(String idCard) {
        int len = idCard.length();
        String sCity = null;
        String sCityNum = "";
        if (len == CHINA_ID_MIN_LENGTH || len == CHINA_ID_MAX_LENGTH) {
            sCityNum = idCard.substring(0, 4);
        }
        sCity = CITY_CODES.get(sCityNum);
        return sCity;
    }

    /**
     * 数字验证
     *
     * @param val
     * @return 提取的数字。
     */
    public static boolean isNum(String val) {
        return val == null || "".equals(val) ? false : val.matches("^[0-9]*$");
    }

    /**
     * 验证小于当前日期 是否有效
     *
     * @param iYear  待验证日期(年)
     * @param iMonth 待验证日期(月 1-12)
     * @param iDate  待验证日期(日)
     * @return 是否有效
     */
    public static boolean valiDate(int iYear, int iMonth, int iDate) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int datePerMonth;
        if (iYear < MIN || iYear >= year) {
            return false;
        }
        if (iMonth < 1 || iMonth > 12) {
            return false;
        }
        switch (iMonth) {
            case 4:
            case 6:
            case 9:
            case 11:
                datePerMonth = 30;
                break;
            case 2:
                boolean dm = ((iYear % 4 == 0 && iYear % 100 != 0) || (iYear % 400 == 0)) && (iYear > MIN && iYear < year);
                datePerMonth = dm ? 29 : 28;
                break;
            default:
                datePerMonth = 31;
        }
        return (iDate >= 1) && (iDate <= datePerMonth);
    }

}