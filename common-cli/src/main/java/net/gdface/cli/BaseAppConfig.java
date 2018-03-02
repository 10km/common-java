package net.gdface.cli;

import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * 应用程序配置参数抽象类
 * @author guyadong
 *
 */
public abstract class BaseAppConfig extends AbstractConfiguration 
	implements CommonCliConstants {
	private static final Logger logger = Logger.getLogger(BaseAppConfig.class.getSimpleName());

	protected final Options options = new Options();
	protected final Context defaultValue = Context.builder().build();
	public BaseAppConfig() {
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
		Options options = getOptions();
		options.addOption(HELP_OPTION, HELP_OPTION_LONG, false, HELP_OPTION_DESC);
		String formatstr = getCmdLineSyntax();
		boolean exit = false;
		try {
			// 处理Options和参数
			cl = parser.parse(options, args);
			if (!cl.hasOption(HELP_OPTION)) {
				if (cl.hasOption(DEFINE_OPTION)) {
					setSystemProperty(cl.getOptionValues(DEFINE_OPTION));
				}
				loadConfig(options, cl);
			} else{
				exit = true;
			}
		}catch (ParseException e) {
			logger.warning(e.getMessage());
			exit = true;
		}
		if (exit) {
			formatter.printHelp(formatstr, options); // 如果发生异常，则打印出帮助信息
			System.exit(1);
		}
		return this;
	}
	private void setSystemProperty(String[] properties) {
		for (int i = 0; i < properties.length; i += 2) {
			System.setProperty(properties[i], properties[i + 1]);
			logger.info(String.format("set property [{}]=[{}]", properties[i], properties[i + 1]));
		}
	}
	protected String getCmdLineSyntax() {
		return String.format("run%s [options]", this.getClass().getSimpleName());
	}

	public Options getOptions() {
		return options;
	}

}
