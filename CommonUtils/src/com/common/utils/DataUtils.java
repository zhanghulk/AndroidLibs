package com.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DataUtils {

    public static final String DIVIDER_VERTICAL_LINE = "|";

    public static void main(String[] args) {
    	List<String> headImgList = DataUtils.splitAsListWithVerLine("dfg|fdsfds");
    	System.out.println(headImgList.get(0));
	}

    public static <T> int getIndex(T[] array, T dest) {
        if(array == null || array.length == 0) return -1;
        int index = 0;
        if(array != null) {
            for (int i = 0; i < array.length; i++) {
                if(array[i].equals(dest)) {
                	index = i;
                    break;
                }
            }
        }
        return index;
    }

    public static int getIndex(int[] array, int dest) {
        if(array == null || array.length == 0) return -1;
        int index = 0;
        if(array != null) {
            for (int i = 0; i < array.length; i++) {
                if(array[i] == dest) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public static int getInt(String str) {
        try {
            return Integer.valueOf(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String formatText(String text, String spliter, String divider) {
        List<String> list = splitTextAsList(text, spliter);
        return formatListAsText(list, divider);
    }

    public static String formatListAsText(List<String> list, String divider) {
        if(list == null || list.isEmpty()) return "";
        StringBuffer buf = new StringBuffer();
        for (String string : list) {
            if(string != null ) {
                String it = string.trim();
                if(it.length() > 0) {
                    buf.append(it).append(divider);
                }
            }
        }
        if(buf.length() > 1) {
            buf.deleteCharAt(buf.length() - 1);//remove last "|"
        }
        return buf.toString();
    }
    
    public static String formatSetAsText(Set<String> set, String divider) {
        if(set == null || set.isEmpty()) return "";
        StringBuffer buf = new StringBuffer();
        for (String string : set) {
            if(string != null ) {
                String it = string.trim();
                if(it.length() > 0) {
                    buf.append(it).append(divider);
                }
            }
        }
        if(buf.length() > 1) {
            buf.deleteCharAt(buf.length() - 1);//remove last "|"
        }
        return buf.toString();
    }

    public static List<String> splitTextAsList(String text, String spliter) {
        List<String> list = new ArrayList<String>();
        if(text == null || text.length() == 0) {
            return list;
        }
        String[] arr = text.split(spliter);
        for (String string : arr) {
            if(string != null) {
                String it = string.trim();
                if(it.length() != 0) {
                    list.add(it);
                }
            }
        } 
        return list;
    }

    public static List<String> splitAsListWithVerLine(String text) {
		String[] arr = splitWithVerLine(text);
		ArrayList<String> urlList = new ArrayList<String>();
        if(text != null) {
            if(arr != null && arr.length > 0) {
            	for (String url : arr) {
                    if (!isEmpty(url)) {
                        urlList.add(url);
                    }
                }
            }
        }
        return urlList;
    }

    public static boolean isEmpty(String str) {
    	return str == null || str.length()  == 0;
	}

	/**
	 * split as String array.
	 * @param text the formated text with vertical line.
	 * @return
	 */
	public static String[] splitWithVerLine(String text) {
        if(text == null) return null;
        return text.split("\\|");
    }

    public static String formatTextWithVerLine(List<String> list) {
    	if(list == null || list.isEmpty()) return "";
    	if(list.size() == 1) {
    		return list.get(0);
    	}
		return formatListAsText(list, "|");
	}
    
    public static String[] removeNullValue(String[] array) {
		List<String> realList = new ArrayList<String>();
		for (int i = 0; i < array.length; i++) {
			if (!isEmpty(array[i])) {
				realList.add(array[i]);
			}
		}
		return realList.toArray(new String[realList.size()]);
	}
}
