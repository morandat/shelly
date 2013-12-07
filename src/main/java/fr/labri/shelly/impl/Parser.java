package fr.labri.shelly.impl;

import java.lang.reflect.InvocationTargetException;

import fr.labri.shelly.Option;
import fr.labri.shelly.ShellyItem;

public class Parser {
	Context opts;
	PeekIterator<String> cmdLine;
	Parser(Context ctx, PeekIterator<String> cmdline) {
		opts = ctx;
		this.cmdLine = cmdline;
	}
}
