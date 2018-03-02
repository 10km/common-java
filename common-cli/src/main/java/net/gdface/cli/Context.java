package net.gdface.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * 上下文数据管理类
 * @author guyadong
 *
 */
public class Context {
	public static final class Builder {
		private final Map<String, Object> map = new HashMap<String, Object>();

		private Builder() {
		}

		public Builder addProperties(Map<String, ? extends Object> properties) {
			if (properties != null){
				map.putAll(properties);
			}
			return this;
		}

		public <T> Builder addProperty(String name, T property) {
			map.put(name, property);
			return this;
		}

		public Context build() {
			return new Context(map);
		}
		public <T extends Context> T build(Class<T> clazz) {
			if(null == clazz){
				throw new NullPointerException("clazz must not be null");
			}
			T instance;
			try {
				instance = clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			instance.setProperties(map);
			return instance;
		}
	}

	public static final Builder builder() {
		return new Builder();
	}

	private Map<String, Object> context = new HashMap<String, Object>();

	public Context() {
	}

	public Context(Map<String, Object> map) {
		context.putAll(map);
	}
	public Context(Context context) {
		this.context.putAll(context.getContext());
	}
	/**
	 * @return map
	 */
	public Map<String, Object> getContext() {
		return context;
	}

	/**
	 * @param name
	 * @return
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String name) {
		return (T) context.get(name);
	}

	/**
	 * @param name
	 * @return
	 * @see #getProperty(String)
	 */
	public boolean hasProperty(String name) {
		return getProperty(name) != null;
	}

	/**
	 * @param properties
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void setProperties(Map<String, ? extends Object> properties) {
		context.putAll(properties);
	}

	/**
	 * @param name
	 * @param property
	 * @return
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public <T> T setProperty(String name, T property) {
		return (T) context.put(name, property);
	}

}
