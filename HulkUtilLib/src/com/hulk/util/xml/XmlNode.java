package com.hulk.util.xml;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

@SuppressLint("NewApi")
public class XmlNode {
	
	public static final String TAG = "XmlNode";

	public String name = "";//当前节点名称
	public String value = "";//当前节点text值
	public int depth = 0;   //Xml节点层级
	public NodeType nodeType = NodeType.PAIRS;//节点类型: one of NodeType.*, 默认键值对map类型
	public XmlNode parent = null; //父节点名称,根节点的父节点为空
	
	//对于一个节点来说,要么是数组节点,要么是数据键值对,不会出现两个同时存在
	private JSONObject mChildPairs = null;//节点字段集合map
	private JSONArray mChildArray = null;//节点字段集合为数组

	public XmlNode(String name, int depth) {
		this.name = name;
		this.depth = depth;
	}
	
	public XmlNode(String name, int depth, NodeType nodeType) {
		this.name = name;
		this.depth = depth;
		this.nodeType = nodeType;
	}
	
	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public synchronized boolean putPairChild(String name, Object value) {
		if(mChildPairs == null) {
			this.mChildPairs = new JSONObject();
		}
		if (value instanceof JSONObject) {
			JSONObject o = (JSONObject)value;
			if (o.length() > 0) {
				putJsonPair(mChildPairs, name, o);
			}
		} else if (value instanceof JSONArray) {
			JSONArray a = (JSONArray)value;
			if (a.length() > 0) {
				putJsonPair(mChildPairs, name, a);
			}
		} else {
			putJsonPair(mChildPairs, name, value);
		}
		return true;
	}
	
	public synchronized boolean putArrayChild(Object value) {
		if(mChildArray == null) {
			this.mChildArray = new JSONArray();
		}
		if (value instanceof JSONObject) {
			JSONObject o = (JSONObject)value;
			if (o.length() > 0) {
				mChildArray.put(o);
			}
		} else if (value instanceof JSONArray) {
			JSONArray a = (JSONArray)value;
			if (a.length() > 0) {
				mChildArray.put(a);
			}
		} else {
			mChildArray.put(value);
		}
		return true;
	}
	
	/**
	 * 最终生成的json JSONObject
	 * @return
	 */
	public synchronized JSONObject toJSONObject() {
		JSONObject jsonObj = new JSONObject();
		if (isPairsNode()) {
			if (!isChildPairsEmpty()) {
				Log.i(TAG, "toJSONObject Pairs name: " + name + " : " + mChildPairs);
				putJsonPair(jsonObj, name, mChildPairs);
			} else if (isArrayNode()) {
				if(!isChildArrayEmpty()) {
					Log.i(TAG, "toJSONObject Array name: " + name + " : " + mChildPairs);
					putJsonPair(jsonObj, name, mChildArray);
				}
			} else {
				Log.e(TAG, "toJSONObject ERROR: Unknown nodeType: " + nodeType);
			}
		}
		
		return jsonObj;
	}
	
	public String toJsonText() {
		return toJSONObject().toString();
	}
	
	public Object getChildPair(String name) {
		try {
			return mChildPairs.get(name);
		} catch (JSONException e) {
			Log.e(TAG, "Get ChildPairs : + " + e + ", for name=" + name, e);
		}
		return null;
	}
	
	public JSONObject getChildPairs() {
		return mChildPairs;
	}
	
	public JSONArray getChildArray() {
		return mChildArray;
	}
	
	public Object optChildPair(String name) {
		return mChildPairs.opt(name);
	}
	
	public JSONObject optChildJson(String name) {
		if(mChildPairs == null) {
			return null;
		}
		return mChildPairs.optJSONObject(name);
	}
	
	public int getChildDepth() {
		return depth + 1;//子节点层级为当前节点+1
	}
	
	public Object removeChildPair(String name) {
		if(mChildPairs == null) {
			return null;
		}
		return mChildPairs.remove(name);
	}
	
	public boolean hasChildPair(String name) {
		if(mChildPairs == null) {
			return false;
		}
		return mChildPairs.opt(name) != null;
	}

	public void clearChildPairs() {
		if(mChildPairs == null) {
			return;
		}
		Iterator<String> keys = mChildPairs.keys();
		while (keys.hasNext()) {
			mChildPairs.remove(keys.next());
		}
	}
	
	public void clearChildArray() {
		if(mChildArray == null) {
			return;
		}
		for (int i = 0; i < mChildArray.length(); i++) {
			mChildArray.remove(i);
		}
	}
	
	public Object removeArrayChild(int index) {
		if(mChildArray == null) {
			return null;
		}
		return mChildArray.remove(index);
	}
	
	public boolean isChildPairsEmpty() {
		if(mChildPairs == null) {
			return true;
		}
		return mChildPairs.length() == 0;
	}
	
	public boolean isChildArrayEmpty() {
		if(mChildArray == null) {
			return true;
		}
		return mChildArray.length() == 0;
	}
	
	public boolean isArrayNode() {
		return nodeType == NodeType.ARRAY;
	}
	
	public boolean isPairsNode() {
		return nodeType == NodeType.PAIRS;
	}

	/**
	 * 只对比depth和name
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof XmlNode)) {
			return false;
		}
		XmlNode n = (XmlNode)obj;
		//TODO 层级和名称相同就认为是同一个节点(不包含数组，另外找解决办法)
		if (equalNode(n)) {
			return true;
		}
		return super.equals(obj);
	}
	
	public boolean equalNode(XmlNode node) {
		//TODO 层级和名称相同就认为是同一个节点(不包含数组，另外找解决办法)
		if (depth == node.depth && name.equals(node.name)) {
			return true;
		}
		return false;
	}
	
	public String getParentName() {
		return parent != null ? parent.name : null;
	}

	@Override
	public String toString() {
		return "XmlNode [name=" + name + ", depth=" + depth + ", nodeType=" + nodeType
				+ ", parent=" + getParentName() + ", childPairs=" + mChildPairs + ", childArray=" + mChildArray + "]";
	}

	private boolean putJsonPair(JSONObject jsonObject, String name, Object value) {
		if (TextUtils.isEmpty(name) || value == null) {
			Log.e(TAG, "putJson FAILED for invalid Pair>> \"" + name + "\":" + value);
			return false;
		}
		try {
			Object jsonObj = jsonObject.opt(name);
			if (jsonObj != null) {
				Log.w(TAG, "putJsonPair feild: " + name + " is existed, old value: " + jsonObj);
			}
			jsonObject.put(name, value);
			return true;
		} catch (JSONException e) {
			Log.e(TAG, "putJsonPair: " + e + " >> \"" + name + "\":" + value, e);
			return false;
		}
	}
}
