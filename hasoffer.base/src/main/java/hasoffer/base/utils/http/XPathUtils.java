package hasoffer.base.utils.http;

import hasoffer.base.exception.ContentParseException;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.util.ArrayList;
import java.util.List;

public class XPathUtils {
	public static TagNode getSubNodeByXPath(TagNode node, String xpath, ContentParseException exceptionToThrow)
			throws ContentParseException {
		try {
			Object[] nodes = node.evaluateXPath(xpath);
			if (nodes == null || nodes.length == 0 || nodes.length > 1) {
				if (exceptionToThrow != null) {
					throw exceptionToThrow;
				}
				return null;
			}
			return (TagNode) nodes[0];
		} catch (XPatherException e) {
			if (exceptionToThrow != null) {
				throw exceptionToThrow;
			}
			return null;
		}
	}

	public static String getSubNodeStringByXPath(TagNode node, String xpath, ContentParseException exceptionToThrow)
			throws ContentParseException {
		TagNode subNode = getSubNodeByXPath(node, xpath, exceptionToThrow);
		if (subNode == null) {
			return null;
		}
		return subNode.getText().toString().trim();
	}

	public static List<TagNode> getSubNodesByXPath(TagNode node, String xpath, ContentParseException exceptionToThrow)
			throws ContentParseException {
		ArrayList<TagNode> result = new ArrayList<TagNode>();
		try {
			Object[] nodes = node.evaluateXPath(xpath);
			if (nodes == null) {
				if (exceptionToThrow != null) {
					throw exceptionToThrow;
				}
				return result;
			}
			for (Object subNode : nodes) {
				result.add((TagNode) subNode);
			}
			return result;
		} catch (XPatherException e) {
			if (exceptionToThrow != null) {
				throw exceptionToThrow;
			}
			return result;
		}
	}

	public static List<String> getStringsByXPath(TagNode node, String xpath, ContentParseException exceptionToThrow)
			throws ContentParseException {
		ArrayList<String> result = new ArrayList<String>();
		try {
			Object[] nodes = node.evaluateXPath(xpath);
			if (nodes == null) {
				if (exceptionToThrow != null) {
					throw exceptionToThrow;
				}
				return result;
			}
			for (Object string : nodes) {
				if (string instanceof String) {
					result.add((String) string);
				} else if (string instanceof StringBuilder) {
					result.add(string.toString());
				}
			}
			return result;
		} catch (XPatherException e) {
			if (exceptionToThrow != null) {
				throw exceptionToThrow;
			}
			return result;
		}
	}

	public static List<String> getSubNodesStringsByXPath(TagNode node, String xpath, ContentParseException exceptionToThrow)
			throws ContentParseException {
		List<TagNode> nodes = getSubNodesByXPath(node, xpath, exceptionToThrow);
		List<String> result = new ArrayList<String>();
		for (TagNode subNode : nodes) {
			String text = subNode.getText().toString().trim();
			result.add(text);
		}
		return result;
	}
}
