/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.snail.dnslb4j.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pengmeng
 */
public class Log {

	private static org.slf4j.Logger logger;

	public static org.slf4j.Logger logger() {
		if (logger == null) {
			try {
				LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
				File externalConfigFile = new File(Cfg.getLogbackXmlFilePath());
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(lc);
				lc.reset();
				configurator.doConfigure(externalConfigFile);
				StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
				logger = LoggerFactory.getLogger("logger");
			} catch (JoranException ex) {
				Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return logger;
	}
}
