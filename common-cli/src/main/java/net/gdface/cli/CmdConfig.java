package net.gdface.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public interface CmdConfig {

	/**
	 * @param options
	 * @param cmd
	 * @throws ParseException
	 * @see #loadConfig(Options, CommandLine)
	 */
	public abstract void loadConfig(Options options, CommandLine cmd) throws ParseException;

}