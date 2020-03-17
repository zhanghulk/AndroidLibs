package com.hulk.util.xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;

/**
 * xml文件解析为JSON
 * res/xml/persons.xml:
 * 
<?xml version="1.0" encoding="UTF-8"?>
<persons>
		<user>
			<user_info>
				<user_name>Hulk</user_name>
				<user_num>0666601</user_num>
				<user_age>22</user_age>
			</user_info>
			<user_school>
				<school_name>beijingdaxue</school_name>
				<school_code>010000</school_code>
			</user_school>
		</user>
		<user>
			<user_info>
				<user_name>dongxiaojun</user_name>
				<user_num>0856024</user_num>
				<user_age>2109</user_age>
			</user_info>
			<user_school>
				<school_name>qinghuadaxue</school_name>
				<school_code>020000</school_code>
			</user_school>
		</user>
</persons>

 use demo:
	private void convert() {
		XmlJsonParser parser = new XmlJsonParser(getResources(), R.xml.persons);
		XmlNode root = parser.parse(new XmlJsonParser.XmlNodeCallback() {
			
			@Override
			public NodeType getNodeType(String nodeName, int depth) {
				if ("persons".equals(nodeName)) {
					return NodeType.ARRAY;
				}
				return NodeType.PAIRS;
			}
		});
		mJsonTv.setText(root == null ? "" : root.toJsonText());
	}
 * Created by Hulk on 2017/10/19.
 */

public class XmlJsonParser {
	
	private static final String TAG = "XmlJsonParser";
	private static final String UTF_8 = "utf-8";
	
	//解析过程所有元素组成的堆栈: 
	// 根节点位于底部,最上面的元素为当前正在解析的节点的父节点
	//一个完整的父节点解析完毕后, 该节点出栈,赋值给其上层的父节点的一个元素... 如此重复,层层解析一直到根节点,解析完毕
	Stack<XmlNode> mXmlNodeStack = null;
	//如何判断解析完毕: 
	// Xml只有一个根节点,一开始解析, 根节点入栈,并记录为根节点,用于判断跟节点是否存在或者已经初始化,
	//解析完成时,发现堆栈里面之后根节点,说明Xml解析完毕.
	//最后的解析解析出来的根节点
	XmlNode mRootNode = null;
	
	//XmlPullParser解析器
	XmlPullParser mParser;
	
	//节点毁掉接口: 获取节点类型
	XmlCallback mCallback;

	public XmlJsonParser(Resources res, int xmlResId) {
		XmlResourceParser parser = res.getXml(xmlResId);
		mParser = parser;
	}
	
	public XmlJsonParser(InputStream inputStream) {
		mParser = createPullParser(inputStream);
	}

	public XmlJsonParser(Reader reader) {
		mParser = createPullParser(reader);
	}

	/**
	 * @param parser eg. XmlResourceParser parser = getResources().getXml(R.xml.test.xml);
	 */
	public XmlJsonParser(XmlPullParser parser) {
		mParser = parser;
	}
	
	public void setCallback(XmlCallback callback) {
		this.mCallback = callback;
	}
	
    public XmlPullParser createPullParser(InputStream inputStream) {
    	try {
			XmlPullParser parser = createPullParser();
            parser.setInput(new BufferedInputStream(inputStream), UTF_8);
            return parser;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    	return null;
	}

	/**
	 *
	 * @param reader eg. StringReader reader = new StringReader(mXmlText);
	 * @return
	 */
	public XmlPullParser createPullParser(Reader reader) {
		try {
			XmlPullParser parser = createPullParser();
			if (reader instanceof BufferedReader) {
				parser.setInput(reader);
			} else {
				parser.setInput(new BufferedReader(reader));
			}

			return parser;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		return null;
	}

	private XmlPullParser createPullParser() {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			return parser;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 开始解析xml文件内容
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public XmlNode parse() throws XmlPullParserException, IOException  {
        return parse(mParser);
    }
	
    /**
     * 开始解析xml文件内容
     * @param callback  节点属性回调： 目前需要caller根据name和depth,返回那些节点是否为数组节点,
     * <p>如果为null，一律按照数据字典的方式解析
     * @return
     */
    public XmlNode parse(XmlCallback callback) throws XmlPullParserException, IOException  {
    	if (callback != null) {
    		mCallback = callback;
		}
        return parse(mParser);
    }
    
    /**
     * 解析xml,  利用XmlPullParser时间逐个解析文件TAG,栈底为根节点,栈顶为当前节点的父节点
     * <p> 事件为START_TAG: 对应进栈(没有TEXT事件)，START_TAG的下一个事件为TEXT的节点直接放入父节点的字段集合(childPairs)
     * <p> 事件为END_TAG: 栈顶节点解析完毕，出栈，保存为上一节点的一个字段
     * eg.
     * <p><persons>
	 * <p> <user>
     * <p>   <user_info>
	 * <p>		<user_name>Hulk</user_name>
	 * <p>		<user_num>0666601</user_num>
	 * <p>		<user_age>22</user_age>
	 * <p>   </user_info>
	 * <p>  ....
	 * <p> <user>
	 * <p><persons>
     * @param parser
     * @return  根节点
     */
    private XmlNode parse(XmlPullParser parser) throws XmlPullParserException, IOException  {
    	int depth = parser.getDepth();
        int eventType = parser.getEventType();
        while ((eventType) != XmlPullParser.END_DOCUMENT) {
        	String name = parser.getName();
        	depth = parser.getDepth();
        	Log.i(TAG, "\n\n--------------------------------------------------------");
        	switch (eventType) {
        	case XmlPullParser.START_DOCUMENT:
        		initStack();
        		Log.w(TAG, "START_DOCUMENT name: " + name + ", depth= " + depth + " mXmlNodeStack is cleared ! ");
        		break;
			case XmlPullParser.START_TAG:
				//事件为START_TAG: 对应进栈(没有TEXT事件)，
				//START_TAG的下一个事件为TEXT的节点,直接放入父节点的字段集合(childPairs)
				Log.i(TAG, "START_TAG name: " + name  + ", depth= " + depth);
				if (pushRootNode(name, depth)) {
					//栈为空，说明是第一个节点元素(根节点),直接入栈(通常没有), 进入下一个循环
					eventType = parser.next();//移动到下一个TAG事件
	            	Log.w(TAG, "###########Pushed RootNode parser.next().enventType= " + decodeType(eventType));
					continue;
				}
				eventType = parseStartTag(parser);
				if (eventType != -1) {
					Log.i(TAG, "The node \"" + name + "\" has been parsed completely, continue next loop,"
							+ " >>>> parser next EventType= " + decodeType(parser.getEventType()) + ", name= " + parser.getName());
					continue;//马上重新循环.开始下一个节点
				}
				break;
			case XmlPullParser.TEXT:
				//一般不会出现这个case, XmlPullParser.TEXT已经在parseStartTag中解析完毕
				String text = parser.getText();
				Log.i(TAG, "TEXT text: " + text  + ", depth= " + depth);
				break;
			case XmlPullParser.END_TAG:
				//循环中发现是父节点的结束TAG,会自动出栈,如发现</user_info>结束,自动出栈
				//事件为END_TAG: 栈顶节点解析完毕，出栈，保存为上一节点的一个字段
				Log.i(TAG, "END_TAG name: " + name + ", depth= " + depth);
				parocessEndTag(parser);
				break;
				
			case XmlPullParser.END_DOCUMENT:
        		Log.w(TAG, "Parser END_DOCUMENT: " + name + ", depth= " + depth);
        		break;

			default:
				Log.w(TAG, "Parser Unknown: " + name + ", enventType= " + decodeType(eventType));
				break;
			}
        	
        	//TODO 移动到下一个TAG事件
        	eventType = parser.next();
        	Log.i(TAG, "--------------------------------Go to next loop: parser.EventType= " + decodeType(parser.getEventType()));
		}
        return mRootNode;
    }
    
    private void initStack() {
    	if(mXmlNodeStack == null) {
			mXmlNodeStack = new Stack<XmlNode>();
		} else {
			mXmlNodeStack.clear();
		}
    }

    /**
     * 解析一个完整的节点元素:
     * <p>解析START_TAG后面的TAG，根据下一个EventType类型,作以下处理:
     * <p>1. 下一个为START_TAG，当前节点为父节点, 里面必然存在子节点或者键值对，直接入栈.
     * <p>2. 下一个为TEXT，保存value至上一个节点(栈顶结点)的一个json字段,再次移动事件
     * <p>3. 下一个为END_TAG，此节点为空节点: value为空， 或者没有子节点，保存为上一个节点的的一个字段，value为"", 空字符串
     * <p>解析结束, 都要移动到下一个节点,进行下一个循环,如user_info中的user_name,user_num或者user_age:
     * <p><persons>
	 * <p> <user>
     * <p>   <user_info>
	 * <p>		<user_name>Hulk</user_name>
	 * <p>		<user_num>0666601</user_num>
	 * <p>		<user_age>22</user_age>
	 * <p>   </user_info>
	 * <p>  ....
	 * <p> <user>
	 * <p><persons>
     * @param parser
     * @return  返回处理之后的当前eventType事件, 下移事件后，没有其他处理，返回-1
     * @throws XmlPullParserException
     * @throws IOException
     */
    private int parseStartTag(XmlPullParser parser) throws XmlPullParserException, IOException {
    	if(mXmlNodeStack == null || mXmlNodeStack.isEmpty()) {
			Log.w(TAG, "parseStartTag failed for mXmlNodeStack is empty !! ");
			return -1;
		}
    	//引用栈顶元素,如user_info节点为父节点
		XmlNode parentNode = mXmlNodeStack.peek();
		//当前的节点信息:<user_name>Hulk</user_name>
		int eventType = parser.getEventType();//START_TAG
		String name = parser.getName();//user_name
		int depth = parser.getDepth();//相对于根节点的深度
		if (depth <= parentNode.depth) {
			//不存在当前节点depth比栈顶元素(父节点)小的可能性,这个函数中会把一个节点解析完毕,
			//parse的循环中发现是父节点的结束TAG,会自动出栈,如发现</user_info>结束,自动出栈
			String err = "parseStartTag FATAL ERROR: EnventType(" + decodeType(eventType) + ") " + name
					+ ".depth(" + depth + ") must > parent node " + parentNode.name + ".depth(" + parentNode.depth + ")";
			Log.e(TAG, err);
			throw new IllegalArgumentException(err);
		}
		// user_info为START_TAG,其下一个TAG为TAXT,直接解析text，并把事件后移至END_TAG,否则为父节点，直接入栈
		//eg. <user_name>Hulk</user_name>
		parser.require(XmlPullParser.START_TAG, null, name);
		eventType = parser.next();
		Log.i(TAG, "parseStartTag: " + name + ", >> next enventType: " + decodeType(eventType));
		if(eventType == XmlPullParser.START_TAG) {
			Log.i(TAG, "parseStartTag: >>> push New XmlNode: " + name);
			//user_info为START_TAG,其下一个TAG的加一个tag，下一个节点TAG事件仍然是START_TAG
			//TODO 此处特殊，因为事件之前已经后移，不需要往下走执行，continue直接开始循环
			pushNewXmlNode(new XmlNode(name, depth));
			parser.require(XmlPullParser.START_TAG, null, parser.getName());
		} else if (eventType == XmlPullParser.TEXT) {
			//解析出键值对的value, key为name
			String value = parser.getText();
			Log.i(TAG, "parseStartTag: node name= " + name + ", value= " + value);
			//TODO <user_name>Hulk</user_name>遍历完毕，需要进入下一循环时间
			//带有value的START_TAG，保存上一个节点的子元素字段,eg. user_name有value
			putPairForParent(parentNode, name, value);
			
			eventType = parser.getEventType();
			if (eventType != XmlPullParser.END_TAG) {
				//确保下一个节点是END_TAG
				eventType = parser.next();
			}
			parser.require(XmlPullParser.END_TAG, null, name);
    		//TODO 包含value的节点END_TAG不需要入栈, 移动到下一个TAG事件循环
        	eventType = parser.next();
        	Log.i(TAG,  "parseStartTag: Goto parser.next Name= " + parser.getName() + ", enventType= " + decodeType(eventType));
		} else if (eventType == XmlPullParser.END_TAG) {
			//如果一个节点的value为空, 其START_TAG的下一个为ENG_TAG,保存为""
			Log.w(TAG, "parseStartTag: " + name + ", next is END_TAG, the tag has no value !! ");
			putPairForParent(parentNode, name, "");
			//TODO 移动到下一个TAG事件
			eventType = parser.next();
		} else {
			// TODO 判处异常
			eventType = -1;
			String errMsg = "parseStartTag FATAL ERROR: Invalid next event type= " + decodeType(eventType) + " and neme= " + name
					+ ", please input standard xnl document !! ";
			Log.e(TAG, errMsg);
			throw new XmlPullParserException(errMsg);
		}
		return eventType;
	}
    
    private String decodeType(int eventType) {
		switch (eventType) {
		case XmlPullParser.START_DOCUMENT:
			return "START_DOCUMENT";
		case XmlPullParser.START_TAG:
			return "START_TAG";
		case XmlPullParser.TEXT:
			return "TEXT";
		case XmlPullParser.END_TAG:
			return "END_TAG";
		case XmlPullParser.END_DOCUMENT:
			return "END_DOCUMENT";
		case XmlPullParser.COMMENT:
			return "COMMENT";

		default:
			break;
		}
		return "Unknown EventType= " + eventType;
	}

    /**
     * 给父节点压入一个键值对
     * <p>如果父节点是一个数组就放入数组字段中, 否则当作一个普通键值父节点的一个元素
     * @param parent
     * @param name
     * @param value
     */
    private void putPairForParent(XmlNode parent, String name, Object value) {
    	Log.i(TAG, "Put childPair:( \"" + name + "\": \"" + value + "\")" 
    			+ " >> into parent node: " + parent.name + "(" + parent.nodeType + ")");
		if (parent.isArrayNode()) {
			parent.putArrayChild(value);
		} else {
			parent.putPairChild(name, value);
		}
	}

	public void clearStack() {
		mXmlNodeStack.clear();
	}

	/**
	 *栈为空，说明是第一个节点元素(根节点),直接入栈(通常没有), 进入下一个循环
	 * @param rootNode
	 * @return
	 */
	public boolean pushRootNode(XmlNode rootNode) {
		if(mXmlNodeStack == null) {
			initStack();
		}
		if (mXmlNodeStack.isEmpty()) {
			//root node has no parentName
			//确认节点类型
			rootNode.setNodeType(getNodeType(rootNode));
			mXmlNodeStack.push(rootNode);
			mRootNode = rootNode;
			Log.i(TAG, "Pushed stack rootNode: " + rootNode);
			return true;
		}
		return false;
	}

	/**
	 * 获取节点类型: 通过接口实现,用户自己接口实现节点的类型
	 * @param xmlNode
	 */
	private NodeType getNodeType(XmlNode xmlNode) {
		//用户自己实现接口，确定节点是否为数组
		if (mCallback != null) {
			//如果用户实现了接口就回调
			return mCallback.getNodeType(xmlNode.name, xmlNode.depth);
		}
		return NodeType.PAIRS;//默认为PAIRS
	}
	
	/**
	 * 栈为空，说明是第一个节点元素(根节点),直接入栈(通常没有), 进入下一个循环
	 * @param name
	 * @param depth
	 * @return
	 */
	public boolean pushRootNode(String name, int depth) {
		if (mXmlNodeStack != null && mXmlNodeStack.isEmpty()) {
			XmlNode rootNode = new XmlNode(name, depth);
			return pushRootNode(rootNode);
		}
		return false;
	}

    /**
     * 新节点入栈，如果当前栈顶节点的depth和name与新节点相同，不入栈
     * @param newNode
     * @return
     */
    private boolean pushNewXmlNode(XmlNode newNode) {
    	if (newNode == null) {
			return false;
		}
    	if(mXmlNodeStack == null) {
			initStack();
		}
		if (pushRootNode(newNode)) {
			Log.w(TAG,  "");
			return true;
		}
		//确认节点类型
    	newNode.setNodeType(getNodeType(newNode));
    	XmlNode topNode = mXmlNodeStack.peek();
    	if (newNode.equals(topNode)) {
			Log.w(TAG, "push stack FAILED, can not push the both same XmlNode1: " + newNode + " and " + newNode);
			return false;
		}
    	newNode.parent = topNode;
		mXmlNodeStack.push(newNode);
		Log.i(TAG, ">>>>>Pushed stack newNode(No TEXT TAG): " + newNode);
		return true;
	}

	private void finishXml() {
		XmlNode popNode = mXmlNodeStack.pop();
		String popName = popNode.name;
		Log.i(TAG, "finishXml: popName= " + popName + ", popNode.depth= " + popNode.depth);
		//root节点的END_TAG, 此时pop元素之后栈搞为0，解析完毕, 把root节点保存到json
		if (mRootNode != null) {
			if (!mRootNode.equalNode(popNode)) {
				throw new IllegalArgumentException("The root node is different: " + mRootNode.name + " >> " + popNode.name);
			}
		} else {
			mRootNode = popNode;
		}
		Log.i(TAG, "===============================================");
		Log.i(TAG, "finishXml root node: " + mRootNode);
		Log.i(TAG, "<<<<<< Xml parse finished, Json object: \n" + mRootNode.toJsonText());
		Log.i(TAG, "===============================================");
	}
	
	/**
     * 处理XmlPullParser.END_TAG节点
     * 检查下一个enventType是否为父节点的END_TAG, 把栈顶元素设置为父节点的一个子元素，栈高度减少1
     * <p>下一个循环如果发现是END_TAG，而且name和depth均与当前栈顶节点相同，说明当前栈顶节点已经解析完毕,
     * 可以把栈顶结点的值转交给其父节点(栈顶次元素)，作为父节点的一个json
     * @throws XmlPullParserException 
     */
    private void parocessEndTag(XmlPullParser parser) throws XmlPullParserException {
		if (parser.getEventType() == XmlPullParser.END_TAG) {
			if(mXmlNodeStack.isEmpty()) {
				Log.w(TAG, "parocessEndTag FAILED mXmlNodeStack is empty !! ");
				return;
			}
			String parserTagName = parser.getName();
			int parserDepth = parser.getDepth();
			
			//栈顶元素的节点遍历完毕,需要把其出栈，并设置为其父节点的元素
			XmlNode topNode = mXmlNodeStack.peek();
			String endTag = parserTagName;
			Log.i(TAG, "parocessEndTag: endTag= " + endTag + ", topNode.name= " + topNode.name
					+ ", parserDepth= " + parserDepth + ", topNode.depth= " + topNode.depth
					+ ", stack size= " + mXmlNodeStack.size());
			if(parserDepth == topNode.depth && endTag.equals(topNode.name)) {
				if (mXmlNodeStack.size() == 1) {
					//最后一个节点为根节点: 堆栈中只有一个元素, 结束解析
					finishXml();
				} else {
					//节点解析完毕，栈顶元素出栈, 并把其保存为其父节点的一个元素
					popXmlNode();
				}
			} else {
				Log.i(TAG, "popXmlNodeIfNeed Not need to process tag: " + parserTagName + ", depth= " + parserDepth);
			}
		}
	}

    /**
     * 栈顶元素出栈, 并把其保存为其父节点的一个元素
     */
    private void popXmlNode() {
    	if(mXmlNodeStack == null || mXmlNodeStack.isEmpty()) {
			Log.w(TAG, "pop XmlNode failed for mXmlNodeStack is empty !! ");
			return;
		}
    	//栈顶元素先出栈
		XmlNode topNode = mXmlNodeStack.pop();
		if(topNode == null) {
			Log.w(TAG, "pop XmlNode failed for top node is null !! ");
			return;
		}
		String topName = topNode.name;
		//此时栈顶元素为父节点
		XmlNode parent = mXmlNodeStack.peek();
		int parentDepth = parent.getChildDepth();
		boolean isParentArray = parent.isArrayNode();
		//检查父节点中是否存在同层级/同名的字段
		Log.i(TAG, "pop XmlNode parent=" + parent.name + ", isParentArray= " + isParentArray
				+ ", parentDepth= " + parentDepth + ", popNode.depth= " + topNode.depth);
		
		//父节点为数组保存到数组中,否则保存到键值对中
		if (parent.isArrayNode()) {
			if(!topNode.isChildPairsEmpty()) {
				parent.putArrayChild(topNode.getChildPairs());
			}
			if(!topNode.isChildArrayEmpty()) {
				parent.putArrayChild(topNode.getChildArray());
			}
		} else {
			if(!topNode.isChildPairsEmpty()) {
				parent.putPairChild(topName, topNode.getChildPairs());
			}
			if(!topNode.isChildArrayEmpty()) {
				parent.putPairChild(topName, topNode.getChildArray());
			}
		}
		Log.i(TAG, "<<<<< pop XmlNode parent: " + parent.name);
	}

    public XmlNode getRootNode() {
		return mRootNode;
	}
    
    /**
     * parse xml text as json text
     * @param mXmlText
     * @param callback
     * @return
     */
    public static String parseXml(String xmlText, XmlCallback callback) {
    	StringReader reader = null;
    	try {
    		reader = new StringReader(xmlText);
        	XmlJsonParser parser = new XmlJsonParser(reader);
        	XmlNode rootNode = parser.parse(callback);
            return rootNode.toJsonText();
		} catch (Exception e) {
			Log.e(TAG, "parseXml: " + e + ", xmlText: \n" + xmlText, e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return "";
    }

    /**
     * parse xml inputStream as json text
     * @param input
     * @param callback
     * @return
     */
    public static String parseXml(InputStream input, XmlCallback callback) {
    	if (input == null) {
			return "";
		}
    	StringReader reader = null;
    	try {
        	XmlJsonParser parser = new XmlJsonParser(input);
        	XmlNode rootNode = parser.parse(callback);
            return rootNode.toJsonText();
		} catch (Exception e) {
			Log.e(TAG, "parseXml: " + e, e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return "";
    }
}
