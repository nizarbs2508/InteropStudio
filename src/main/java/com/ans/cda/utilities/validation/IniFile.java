package com.ans.cda.utilities.validation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

/**
 * IniFile
 * 
 * @author bensa
 */
public class IniFile {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(IniFile.class);
	/**
	 * path file cda meta
	 */
	private static String path;

	/**
	 * IniFile constructor
	 */
	public IniFile() {

	}

	/**
	 * IniFile constructor
	 * 
	 * @param iniPath
	 */
	public IniFile(final String iniPath) {
		if (iniPath == null) {
			final URL resource = getClass().getClassLoader().getResource("InteropStudio2022.ini");
			if (resource == null) {
				throw new IllegalArgumentException("file not found!");
			} else {
				try {
					path = new File(resource.toURI()).getAbsolutePath();
				} catch (final URISyntaxException e) {
					if (LOG.isInfoEnabled()) {
						final String error = e.getMessage();
						LOG.error(error);
					}
				}
			}
		} else {
			path = iniPath;
		}
	}

	/**
	 * read
	 * 
	 * @param key
	 * @param section
	 * @return
	 */
	public String read(final String key, final String section) {
		String value = null;
		try {
			final Wini ini = new Wini(new File(path));
			value = ini.get(section, key, String.class);
		} catch (final InvalidFileFormatException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return value;
	}

	/**
	 * write
	 * 
	 * @param key
	 * @param value
	 * @param section
	 */
	public void write(final String key, final String value, final String section) {
		Wini ini;
		try {
			ini = new Wini(new File(path));
			ini.put(section, key, value);
			ini.store();
		} catch (final InvalidFileFormatException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}
}