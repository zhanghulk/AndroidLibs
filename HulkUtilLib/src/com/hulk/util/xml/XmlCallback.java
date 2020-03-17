package com.hulk.util.xml;

/**
 * Xml解析毁掉接口: 如 获取节点类型
 * @author zhanghao
 *
 */
public interface XmlCallback {

	/**
	 * 节点类型回调， 需要调用者自己根据xml文档结构自己判断并返回响应类型
	 * <p> 实体携带TEXTd的节点不会回电此函数 eg. 
	 * @param nodeName
	 * @param depth
	 * @return one of @NodeType.*
	 */
	NodeType getNodeType(String nodeName, int depth);
}
