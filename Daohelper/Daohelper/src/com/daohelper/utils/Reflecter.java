package com.daohelper.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 利用反射复制对象,根据类中声明的所有变量，遍历出所有对应的getXXX()函数和setXXX()函数,进行赋值.
 * <p>
 * 对象中的变量（包括成员变量和静态变量）必须具有对应的getXXX()和setXXX()函数，否则会抛出NoSuchMethodException异常.
 * 
 * <p> 如果要避免以上情况请尝试调用copySmartlyThrowException()和copySmartly()
 * 
 * <p> NOTE 改反射器赋值对象不能复制父类对象中的值.
 * @author hao
 * 
 */
public class Reflecter {
	public Object copy(Object object) throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		Class classType = object.getClass();// 获得对象的类型
		System.out.println("Copy Class:" + classType.getName());

		// 通过默认的构造函数创建一个新的对象
		Object objectCopy = classType.getConstructor(new Class[] {})
				.newInstance(new Object[] {});

		// 获得对象的所有属性
		Field fields[] = classType.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String fieldName = field.getName();
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			// 获得和属性对应的getXXX（）方法的名字
			String getMethodName = "get" + firstLetter + fieldName.substring(1);
			// 获得和属性对应的setXXX()方法的名字
			String setMethodName = "set" + firstLetter + fieldName.substring(1);

			// 获得和属性对应的getXXX()方法
			Method getMethod = classType.getMethod(getMethodName,
					new Class[] {});
			// 获得和属性对应的setXXX()方法
			Method setMethod = classType.getMethod(setMethodName,
					new Class[] { field.getType() });

			// 调用原对象的getXXX()方法
			Object value = getMethod.invoke(object, new Object[] {});
			// System.out.println(fieldName + ":" + value);
			// 调用复制对象的setXXX()方法
			setMethod.invoke(objectCopy, new Object[] { value });
		}
		return objectCopy;
	}

	public Object copyThrowException(Object object) throws Exception {
		try {
			return copy(object);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public Object copySmartlyThrowException(Object object) throws Exception {
		try {
			return copySmartly(object);
		} catch (Exception e) {
			throw e;
		}
	}

	public Object copySmartly(Object object) throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Class classType = object.getClass();// 获得对象的类型
		System.out.println("Copy Class:" + classType.getName());

		// 通过默认的构造函数创建一个新的对象
		Object objectCopy = null;
		try {
			objectCopy = classType.getConstructor(new Class[] {}).newInstance(
					new Object[] {});
		} catch (NoSuchMethodException e) {
			System.err.println("copySmartly getConstructor: " + e);
			e.printStackTrace();
		}

		if (objectCopy == null) {
			System.err.println(" ## ERROR copySmartly execute Constructor failed, objectCopy is NULL ");
			return null;
		}
		// 获得对象的所有属性
		Field fields[] = classType.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String fieldName = field.getName();
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			// 获得和属性对应的getXXX（）方法的名字
			String getMethodName = "get" + firstLetter + fieldName.substring(1);
			// 获得和属性对应的setXXX()方法的名字
			String setMethodName = "set" + firstLetter + fieldName.substring(1);

			Method getMethod;
			Object value = null;
			try {
				// 获得和属性对应的getXXX()方法
				getMethod = classType.getMethod(getMethodName, new Class[] {});
				// 调用原对象的getXXX()方法
				value = getMethod.invoke(object, new Object[] {});
			} catch (NoSuchMethodException e) {
				System.err.println("copySmartly getMethodName: " + e);
				e.printStackTrace();
			}
			if(value == null) {
				System.out.println("##copySmartly get NULL from method: " + getMethodName);
				continue;
			}
			Method setMethod;
			try {
				// 获得和属性对应的setXXX()方法
				setMethod = classType.getMethod(setMethodName,
						new Class[] { field.getType() });
				// 调用复制对象的setXXX()方法
				setMethod.invoke(objectCopy, new Object[] { value });
			} catch (NoSuchMethodException e) {
				System.err.println("copySmartly setMethodName: " + e + ", value=" + value);
				e.printStackTrace();
			}
		}
		return objectCopy;
	}
}
