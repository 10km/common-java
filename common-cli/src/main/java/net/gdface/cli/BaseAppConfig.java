package net.gdface.cli;

import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 * 应用程序配置参数抽象类
 * @author guyadong
 *
 */
public abstract class BaseAppConfig extends AbstractConfiguration 
	implements CommonCliConstants {
	protected static final Logger logger = Logger.getLogger(BaseAppConfig.class.getSimpleName());

	/**
	 * 定义命令行参数
	 */
	protected final Options options = new Options();
	/**
	 * 定义命令行参数的默认值
	 */
	protected final Context defaultValue = Context.builder().build();
	protected BaseAppConfig() {
	}

	@Override
	protected Map<String, Object> getDefaultValueMap() {
		return defaultValue.getContext();
	}

	/**
	 * 解析命令行参数
	 * @param args
	 * @return
	 */
	public BaseAppConfig parseCommandLine(String[] args) {
		HelpFormatter formatter = new HelpFormatter();
		CommandLineParser parser = new DefaultParser();
		CommandLine cl = null;
		Options opts = getOptions();
		opts.addOption(HELP_OPTION, HELP_OPTION_LONG, false, HELP_OPTION_DESC);
		boolean exit = false;
		try {
			// 处理Options和参数
			cl = parser.parse(opts, args);
			if (!cl.hasOption(HELP_OPTION)) {
				if (cl.hasOption(DEFINE_OPTION)) {
					setSystemProperty(cl.getOptionValues(DEFINE_OPTION));
				}
				loadConfig(opts, cl);
			} else{
				exit = true;
			}
		}catch (Exception e) {
			logger.warning(e.getMessage());
			exit = true;
		}
		if (exit) {
			 // 如果发生异常，则打印出帮助信息
			formatter.printHelp(getCmdLineSyntax(), getHeader(),getOptions(),getFooter());
			System.exit(1);
		}
		return this;
	}
	private void setSystemProperty(String[] properties) {
		if(properties.length %2 != 0){
			throw new IllegalArgumentException("INVALID properties length");
		}
		for (int i = 0; i < properties.length; i += 2) {
			System.setProperty(properties[i], properties[i + 1]);
			logger.info(String.format("set property [%s]=[%s]", properties[i], properties[i + 1]));
		}
	}
	protected String getCmdLineSyntax() {
		return String.format("%s [options]", getAppName());
	}

	public Options getOptions() {
		return options;
	}
	protected String getAppName(){
		return "Appname";
	}
	/**
	 * @return
	 * @see HelpFormatter#printHelp(String, String, Options, String)
	 */
	protected String getHeader() {
		return null;
	}
	/**
	 * @return
	 * @see HelpFormatter#printHelp(String, String, Options, String)
	 */
	protected String getFooter() {
		return null;
		
	}
}
