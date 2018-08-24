package com.feitai.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.feitai.utils.identity.Exceptions.convertReflectionExceptionToUnchecked;

/**
 * 对象操作类，针对擦除获取和反射获取
 */
@Slf4j
public abstract class ObjectUtils extends org.apache.commons.lang3.ObjectUtils {

    private static final String JAVAP = "java.";
    private static final String JAVADATESTR = "java.util.Date";

    private static final String SETTER_PREFIX = "set";
    private static final String GETTER_PREFIX = "get";
    private static final String CGLIB_CLASS_SEPARATOR = "$$";

    private static Logger logger = LoggerFactory.getLogger(ObjectUtils.class);

    /**
     * 调用Getter方法.
     */
    public static Object invokeGetter(Object obj, String propertyName) {
        String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(propertyName);
        return invokeMethod(obj, getterMethodName, new Class[] {}, new Object[] {});
    }

    /**
     * 调用Setter方法, 仅匹配方法名。
     */
    public static void invokeSetter(Object obj, String propertyName, Object value) {
        String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(propertyName);
        invokeMethodByName(obj, setterMethodName, new Object[] { value });
    }

    /**
     * 直接调用对象方法, 无视private/protected修饰符，
     * 用于一次性调用的情况，否则应使用getAccessibleMethodByName()函数获得Method后反复调用.
     * 只匹配函数名，如果有多个同名函数调用第一个。
     */
    public static Object invokeMethodByName(final Object obj, final String methodName, final Object[] args) {
        Method method = getAccessibleMethodByName(obj, methodName);
        if (method == null) {
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
        }

        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }
    }

    /**
     * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.
     * 如向上转型到Object仍无法找到, 返回null.
     * 只匹配函数名。
     *
     * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
     */
    public static Method getAccessibleMethodByName(final Object obj, final String methodName) {
        Validate.notNull(obj, "object can't be null");
        Validate.notBlank(methodName, "methodName can't be blank");

        for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
            Method[] methods = searchType.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    makeAccessible(method);
                    return method;
                }
            }
        }
        return null;
    }



    /**
     * 直接调用对象方法, 无视private/protected修饰符.
     * 用于一次性调用的情况，否则应使用getAccessibleMethod()函数获得Method后反复调用.
     * 同时匹配方法名+参数类型，
     */
    public static Object invokeMethod(final Object obj, final String methodName, final Class<?>[] parameterTypes,
                                      final Object[] args) {
        Method method = getAccessibleMethod(obj, methodName, parameterTypes);
        if (method == null) {
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
        }
        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }
    }

    /**
     * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.
     * 如向上转型到Object仍无法找到, 返回null.
     * 匹配函数名+参数类型。
     *
     * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
     */
    public static Method getAccessibleMethod(final Object obj, final String methodName,
                                             final Class<?>... parameterTypes) {
        Validate.notNull(obj, "object can't be null");
        Validate.notBlank(methodName, "methodName can't be blank");

        for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
            try {
                Method method = searchType.getDeclaredMethod(methodName, parameterTypes);
                makeAccessible(method);
                return method;
            } catch (NoSuchMethodException e) {
                // Method不在当前类定义,继续向上转型
            }
        }
        return null;
    }

    /**
     * 改变private/protected的方法为public，尽量不调用实际改动的语句，避免JDK的SecurityManager抱怨。
     */
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    /**
     * 循环向上转型, 获取对象的DeclaredField, 并强制设置为可访问.
     *
     * 如向上转型到Object仍无法找到, 返回null.
     */
    public static Field getAccessibleField(final Object obj, final String fieldName) {
        Validate.notNull(obj, "object can't be null");
        Validate.notBlank(fieldName, "fieldName can't be blank");
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                makeAccessible(field);
                return field;
            } catch (NoSuchFieldException e) {// NOSONAR
                // Field不在当前类定义,继续向上转型
            }
        }
        return null;
    }

    /**
     * 通过反射, 获得Class定义中声明的泛型参数的类型, 注意泛型必须定义在父类处
     * 如无法找到, 返回Object.class.
     * eg.
     * public UserDao extends HibernateDao<User>
     *
     * @param clazz The class to introspect
     * @return the first generic declaration, or Object.class if cannot be determined
     */
    public static <T> Class<T> getClassGenricType(final Class clazz) {
        return getClassGenricType(clazz, 0);
    }

    /**
     * 通过反射, 获得Class定义中声明的父类的泛型参数的类型.
     * 如无法找到, 返回Object.class.
     *
     * 如public UserDao extends HibernateDao<User,Long>
     *
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic ddeclaration,start from 0.
     * @return the index generic declaration, or Object.class if cannot be determined
     */
    public static Class getClassGenricType(final Class clazz, final int index) {

        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if ((index >= params.length) || (index < 0)) {
            logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
                    + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class) params[index];
    }

    public static Class<?> getUserClass(Object instance) {
        Validate.notNull(instance, "Instance must not be null");
        Class clazz = instance.getClass();
        if ((clazz != null) && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
            Class<?> superClass = clazz.getSuperclass();
            if ((superClass != null) && !Object.class.equals(superClass)) {
                return superClass;
            }
        }
        return clazz;

    }

    /**
     * 改变private/protected的成员变量为public，尽量不调用实际改动的语句，避免JDK的SecurityManager抱怨。
     */
    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) || Modifier
                .isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    /**
     * 擦除获取泛型类的实际类
     *
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T> Class<T> getGenericInterface(Class<?> classOfT) {
        return getGenericInterface(classOfT, Object.class, 0, 0);
    }


    /**
     * 擦除获取泛型类的实际类
     *
     * @param classOfT
     * @param degenerateClass 泛型退化类
     * @param <T>
     * @return
     */
    public static <T> Class<T> getGenericInterface(Class<?> classOfT, Class<? super T> degenerateClass) {
        return getGenericInterface(classOfT, degenerateClass, 0, 0);
    }


    /**
     * 擦除获取泛型接口
     *
     * @param classOfT
     * @param degenerateClass 泛型退化类
     * @param <T>
     * @return
     */
    public static <T> Class<T> getGenericInterface(Class<?> classOfT, Class<? super T> degenerateClass, int superIndex, int paramsIndex) {
        Class<?> clazz = classOfT;
        if (!clazz.isInterface()) {
            return null;
        }
        Class<?> clazzResult = classOfT;
        while (!(clazz.getGenericInterfaces()[superIndex] instanceof ParameterizedType) && clazz != degenerateClass) {
            // 子类循环父类，获取最终的接口
            clazz = clazz.getInterfaces()[superIndex];
        }
        try {
            if (clazz != Object.class) {
                Type type = clazz.getGenericInterfaces()[superIndex];
                Type trueType = ((ParameterizedType) type).getActualTypeArguments()[paramsIndex];
                if (trueType instanceof ParameterizedType) {
                    //泛型嵌套，获取具体泛型类
                    return (Class<T>) ((ParameterizedType) trueType).getRawType();
                } else {
                    // 普通类，直接提取对应类
                    return (Class<T>) trueType;
                }
            } else {
                // 泛型退化
                clazzResult = degenerateClass;
            }
        } catch (Exception e) {
            log.error(String.format("getGenericInterface classOfT<%s> degenerateClass<%s> superIndex<%s> paramsIndex", classOfT.getName(), degenerateClass.getName(), superIndex, paramsIndex), e);
            throw new RuntimeException(e);
        }
        return (Class<T>) clazzResult;
    }

    /**
     * 擦除获取泛型类的实际类
     *
     * @param type
     * @param <T>
     * @return
     */
    public static <T> Class<T> getGenericClass(Type type) {
        return getGenericClass(type, 0);
    }

    /**
     * 擦除获取泛型类的实际类
     *
     * @param type
     * @param paramsIndex
     * @param <T>
     * @return
     */
    public static <T> Class<T> getGenericClass(Type type, int paramsIndex) {
        Type trueType = ((ParameterizedType) type).getActualTypeArguments()[paramsIndex];
        if (trueType instanceof ParameterizedType) {
            //泛型嵌套，获取具体泛型类
            return (Class<T>) ((ParameterizedType) trueType).getRawType();
        } else {
            // 普通类，直接提取对应类
            return (Class<T>) trueType;
        }
    }

    /**
     * 擦除获取泛型类的实际类
     *
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T> Class<T> getGenericClass(Class<?> classOfT) {
        return ObjectUtils.getGenericClass(classOfT, Object.class, 0);
    }


    /**
     * 擦除获取泛型类的实际类
     *
     * @param classOfT
     * @param degenerateClass 泛型退化类
     * @param <T>
     * @return
     */
    public static <T> Class<T> getGenericClass(Class<?> classOfT, Class<? super T> degenerateClass) {
        return ObjectUtils.getGenericClass(classOfT, degenerateClass, 0);
    }

    /**
     * 擦除获取泛型类
     *
     * @param classOfT
     * @param degenerateClass 泛型退化类
     * @param <T>
     * @return
     */
    public static <T> Class<T> getGenericClass(Class<?> classOfT, Class<? super T> degenerateClass, int paramsIndex) {
        Class<?> clazz = classOfT;
        if (clazz.isInterface()) {
            return null;
        }
        Class<?> clazzResult = classOfT;
        // 判断当前类
        if (classOfT.getGenericInterfaces().length > 0 && classOfT.getGenericInterfaces().length <= (paramsIndex + 1)) {
            return (Class<T>) ((ParameterizedType) classOfT.getGenericInterfaces()[paramsIndex]).getActualTypeArguments()[paramsIndex];
        }
        // 父类泛型
        while (!(clazz.getGenericSuperclass() instanceof ParameterizedType) && clazz != degenerateClass) {
            // 子类循环父类，获取最终的接口
            clazz = clazz.getSuperclass();
        }
        try {
            if (clazz != Object.class) {
                Type type = clazz.getGenericSuperclass();
                clazzResult = ObjectUtils.<T>getGenericClass(type, paramsIndex);
            } else {
                // 泛型退化
                clazzResult = degenerateClass;
            }
        } catch (Exception e) {
            log.error(String.format("getGenericClass classOfT<%s> degenerateClass<%s> paramsIndex<%s>", classOfT.getName(), degenerateClass.getName(), paramsIndex));
            throw new RuntimeException(e);
        }
        return (Class<T>) clazzResult;
    }

    /**
     * 利用反射获取指定对象的指定属性
     *
     * @param object    目标对象
     * @param fieldName 目标属性
     * @return 目标属性的值
     */
    public static Object getFieldValue(Object object, String fieldName) {
        Object result = null;
        Field field = getField(object, fieldName);
        if (field != null) {
            field.setAccessible(true);
            try {
                result = field.get(object);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error(String.format("getFieldValue object<%s> fieldName<%s>", object.toString(), fieldName), e);
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 利用反射获取指定对象里面的指定属性
     *
     * @param object    目标对象
     * @param fieldName 目标属性
     * @return 目标字段
     */
    public static Field getField(Object object, String fieldName) {
        Field field = null;
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                log.error(String.format("getField object<%s> fieldName<%s>", object.toString(), fieldName), e);
                throw new RuntimeException(e);
            }
        }
        return field;
    }

    /**
     * 利用反射设置指定对象的指定属性为指定的值
     *
     * @param object     目标对象
     * @param fieldName  目标属性
     * @param fieldValue 目标值
     */
    public static void setFieldValue(Object object, String fieldName, Object fieldValue) {
        Field field = getField(object, fieldName);
        if (field != null) {
            try {
                field.setAccessible(true);
                field.set(object, fieldValue);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error(String.format("setFieldValue object<%s> fieldName<%s> fieldValue<%s>", object.toString(), fieldName, fieldValue.toString()), e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 获取利用反射获取类里面的值和名称
     *
     * @param obj
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, Object> objectToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<String, Object>();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(obj);
            map.put(fieldName, value);
        }
        return map;
    }

    /**
     * 利用递归调用将Object中的值全部进行获取
     *
     * @param timeFormatStr 格式化时间字符串默认<strong>2017-03-10 10:21</strong>
     * @param obj           对象
     * @param excludeFields 排除的属性
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, String> objectToMapString(String timeFormatStr, Object obj, String... excludeFields) throws IllegalAccessException {
        Map<String, String> map = new HashMap<String, String>();

        if (excludeFields.length!=0){
            List<String> list = Arrays.asList(excludeFields);
            objectTransfer(timeFormatStr, obj, map, list);
        }else{
            objectTransfer(timeFormatStr, obj, map,null);
        }
        return map;
    }

    /**
     * 递归调用函数
     *
     * @param obj           对象
     * @param map           map
     * @param excludeFields 对应参数
     * @return
     * @throws IllegalAccessException
     */
    private static Map<String, String> objectTransfer(String timeFormatStr, Object obj, Map<String, String> map, List<String> excludeFields) throws IllegalAccessException {
        boolean isExclude = false;
        //默认字符串
        String formatStr = "YYYY-MM-dd HH:mm:ss";
        //设置格式化字符串
        if (timeFormatStr != null && !timeFormatStr.isEmpty()) {
            formatStr = timeFormatStr;
        }
        if (excludeFields != null) {
            isExclude = true;
        }
        Class<?> clazz = obj.getClass();
        //获取值
        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = clazz.getSimpleName() + "." + field.getName();
            //判断是不是需要跳过某个属性
            if (isExclude && excludeFields.contains(fieldName)) {
                continue;
            }
            //设置属性可以被访问
            field.setAccessible(true);
            Object value = field.get(obj);
            Class<?> valueClass = value.getClass();
            if (valueClass.isPrimitive()) {
                map.put(fieldName, value.toString());

            } else if (valueClass.getName().contains(JAVAP)) {//判断是不是基本类型
                if (valueClass.getName().equals(JAVADATESTR)) {
                    //格式化Date类型
                    SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
                    Date date = (Date) value;
                    String dataStr = sdf.format(date);
                    map.put(fieldName, dataStr);
                } else {
                    map.put(fieldName, value.toString());
                }
            } else {
                objectTransfer(timeFormatStr, value, map, excludeFields);
            }
        }
        return map;

    }


}