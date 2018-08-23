package com.feitai.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 对象操作类，针对擦除获取和反射获取
 */
@Slf4j
public abstract class ObjectUtils extends org.apache.commons.lang3.ObjectUtils {

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
}