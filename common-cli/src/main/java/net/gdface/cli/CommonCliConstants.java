package net.gdface.cli;

import java.util.HashSet;
import java.util.Set;

public interface CommonCliConstants {

	final String HELP_OPTION = "h";
	final String HELP_OPTION_LONG = "help";
	final String HELP_OPTION_DESC = "Print this usage information";
	final String DEFINE_OPTION = "D";
	final String DEFINE_OPTION_DESC = "define value for given property for System.getPropety(String)";
	final Set<String> CONTROL_OPTIONS = new HashSet<String>() {
		private static final long serialVersionUID = -496413689043512747L;
		{
			this.add(HELP_OPTION);
			this.add(HELP_OPTION_LONG);
			this.add(DEFINE_OPTION);
		}
	};
}