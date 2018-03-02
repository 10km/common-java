package net.gdface.cli;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * 参数配置抽象类
 * @author guyadong
 *
 */
public abstract class AbstractConfiguration extends Context implements CommonCliConstants, CmdConfig {
	/** 子类提供命令行参数的默认值 */
	protected abstract Map<String, Object> getDefaultValueMap();
	@Override
	public void loadConfig(Options options, CommandLine cmd) throws ParseException {
		Iterator<Option> it = options.getOptions().iterator();
		Option opt;
		String key;
		Map<String, Object> defaultMap = getDefaultValueMap();
		if (defaultMap == null){
			defaultMap = new HashMap<String, Object>();
		}
		while (it.hasNext()) {
			opt = it.next();
			// 优先用长值
			key = opt.getLongOpt() == null ? opt.getOpt() : opt.getLongOpt();
			if(isNeedOption(key)){				
				Object value;
				try{
					value=cmd.getParsedOptionValue(key);
				}catch(NoClassDefFoundError e){
					System.out.printf("key=%s %s\n",key,cmd.getOptionValue(key));	
					throw e;
				}

				if (null == value) {
					if (opt.isRequired()) {
						throw new IllegalArgumentException(String.format("%s or %s not define", opt.getOpt(),
								opt.getLongOpt()));
					} else {
						if (null == (value = defaultMap.get(key))) {
							throw new IllegalArgumentException(String.format("%s or %s not default value", opt.getOpt(),
									opt.getLongOpt()));
						}
					}
				}
				this.setProperty(key, value);
			}
		}
	}
	private final boolean isNeedOption(String opt){
		return !CONTROL_OPTIONS.contains(opt);
	}
}
