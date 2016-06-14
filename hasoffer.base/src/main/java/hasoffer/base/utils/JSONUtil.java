package hasoffer.base.utils;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by glx on 2015/6/6.
 */
public class JSONUtil {
	public static String toJSON(Object obj) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T toObject(String json, Class<T> tClass) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json,tClass);
	}

	public static void main(String[] args) throws IOException {
		List<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		list.add("c");

		T t = new T();
		t.a=5;
		t.b=null;

		System.out.println(toJSON(t));

		List<T> ts = toObject("[{\"a\":5,\"b\":null},{\"a\":5,\"b\":null},{\"a\":5,\"b\":null}]", List.class);

		System.out.println(Arrays.toString(ts.toArray()));
	}

	static  class T{
		private int a=1;
		private String b="aaa";

		public int getA() {
			return a;
		}

		public void setA(int a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(String b) {
			this.b = b;
		}
	}
}
