package per.nonlone.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * 62进制转换工具，提供加入字母大小写进制转换工具，顺序根据ASCII码表顺序
 */
public class Base62Utils {

    /**
     * 进制长度
     */
    public static final int BASE_62 = 62;

    /**
     * 进制字集
     */
    private static final String charSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * 十进制数字转62进制字符串
     *
     * @param value
     * @return
     */
    public static String numberTo62Char(long value) {
        Stack<Character> characterStack = new Stack<>();
        if (value < 0) {
            return null;
        }
        long temp = value;
        while (temp > 61) {
            long residue = temp % BASE_62;
            characterStack.push(charSet.charAt(Long.valueOf(residue).intValue()));
            temp = temp / BASE_62;
        }
        characterStack.push(charSet.charAt(Long.valueOf(temp).intValue()));
        // 倒序输出
        List<Character> resultString = new LinkedList<>(characterStack);
        Collections.reverse(resultString);
        return String.valueOf(ArrayUtils.toPrimitive(resultString.toArray(new Character[resultString.size()])));
    }


    /**
     * 62 进制转换 10 进制数字
     *
     * @param value
     * @return
     */
    public static Long char62ToNumber(String value) {
        Long result = 0L;
        if (StringUtils.isBlank(value)) {
            return result;
        }
        for (int i = 0; i < value.length(); i++) {
            int index = charSet.indexOf(String.valueOf(value.charAt(i)));
            // 数制加权
            result += new Double(Math.pow(BASE_62, (value.length() - 1 - i)) * index).longValue();
        }
        return result;
    }

}
