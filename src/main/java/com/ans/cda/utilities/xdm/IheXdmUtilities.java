package com.ans.cda.utilities.xdm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;

import com.ans.cda.constant.Constant;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

/**
 * IheXdmUtilities
 * 
 * @author bensa
 */
public class IheXdmUtilities {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(IheXdmUtilities.class);

	/**
	 * writeToFile
	 * 
	 * @param file
	 */
	public static void writeToReadmeFile(final String file, final String sSuffix, final String sNomAuteur,
			final String sPrenomAuteur, final String sOrganisme, final String sOrganismeFiness,
			final String sAdresseAuteur, final String sTelephoneAuteur) {
		try {
			final FileWriter myWriter = new FileWriter(file);
			myWriter.write(Constant.EMETTEUR);
			myWriter.write(Constant.LINE);
			myWriter.write(Constant.NAME + sSuffix + " " + sPrenomAuteur + " " + sNomAuteur + "\n");
			myWriter.write(Constant.ORGANISME + sOrganisme + " (" + sOrganismeFiness + ") " + "\n");
			myWriter.write(Constant.ADRESS + sAdresseAuteur + "\n");
			myWriter.write(Constant.TEL + sTelephoneAuteur + "\n\n");
			myWriter.write(Constant.APPEMETT);
			myWriter.write(Constant.LINE);
			myWriter.write(Constant.ADK);
			myWriter.write(Constant.VERSION);
			myWriter.write(Constant.EDITEUR);
			myWriter.write(Constant.INSTRUCTION);
			myWriter.write(Constant.LINE);
			myWriter.write(Constant.MESSAGERIE);
			myWriter.write(Constant.ARBO);
			myWriter.write(Constant.LINE);
			myWriter.write(Constant.README);
			myWriter.write(Constant.INDEX);
			myWriter.write(Constant.IHE);
			myWriter.write(Constant.SUBSET);
			myWriter.write(Constant.METADATA);
			myWriter.write(Constant.DOC);
			myWriter.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * writeToHtmFile
	 * 
	 * @param file
	 * @param sOrganisme
	 * @param sOrganismeFiness
	 */
	public static void writeToHtmFile(final String file, final String sOrganisme, final String sOrganismeFiness) {
		try {
			final FileWriter myWriter = new FileWriter(file);
			myWriter.write(Constant.HEADER);
			myWriter.write(Constant.DOCTYPE);
			myWriter.write(Constant.HTML);
			myWriter.write(Constant.EMT + sOrganisme + " (" + sOrganismeFiness + ")" + "\n");
			myWriter.write(Constant.HREF);
			myWriter.write(Constant.HTM);
			myWriter.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * getXpathSingleValue
	 * 
	 * @param file
	 * @return
	 */
	public static String getXpathSingleValue(final File file, final String expression) {
		String content = "";
		final Processor saxonProcessor = new Processor(false);
		final net.sf.saxon.s9api.DocumentBuilder builde = saxonProcessor.newDocumentBuilder();
		try {
			final XdmNode doc = builde.build(file);
			final XPathCompiler xpath = saxonProcessor.newXPathCompiler();
			final XPathSelector xdm = xpath.compile(expression).load();
			xdm.setContextItem(doc);
			if (xdm.evaluateSingle() != null) {
				content = xdm.evaluateSingle().toString();
			}
		} catch (final SaxonApiException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return content;
	}

	/**
	 * deleteDirectory
	 * 
	 * @param file
	 */
	public static void deleteDirectory(final File file) {
		for (final File subfile : file.listFiles()) {
			if (subfile.isDirectory()) {
				deleteDirectory(subfile);
			}
			subfile.delete();
		}
	}

	/**
	 * copyFile
	 * 
	 * @param original
	 * @param copied
	 */
	public static void copyFile(final File original, final Path copied, final String suffix) {
		final String copiedFileStr = copied + suffix;
		final File copiedFile = new File(copiedFileStr);
		final Path originalPath = original.toPath();
		try {
			Files.copy(originalPath, copiedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * cretaeFolder
	 */
	public static Path cretaeFolder(final String dir) {
		Path path = null;
		try {
			path = Paths.get(dir);
			Files.createDirectories(path);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return path;
	}

	/**
	 * removeExtension
	 * 
	 * @param s
	 * @return
	 */
	public static String removeExtension(final String s) {
		return s != null && s.lastIndexOf(".") > 0 ? s.substring(0, s.lastIndexOf(".")) : s;
	}

	/**
	 * CreateFile
	 * 
	 * @param fileName
	 */
	public static void createFile(final String fileName) {
		try {
			final File myObj = new File(fileName);
			myObj.createNewFile();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * compressFolder
	 * 
	 * @param path
	 * @param contenuZip
	 */
	public static void compressFolder(final Path path, final Path contenuZip) {
		final String strPath = contenuZip.toString() + "\\*";
		final Path resourceDirectory = Paths.get("src", "main", "resources");
		final String absolutePath = resourceDirectory.toFile().getAbsolutePath() + Constant.APIPATH;
		final String[] params = new String[] { Constant.CMD, Constant.SLASHC,
				"7z a " + path.toString() + Constant.IHEXDM + "\"" + strPath + "\"" };
		try {
			final ProcessBuilder builder = new ProcessBuilder();
			builder.directory(new File(absolutePath));
			builder.command(params);
			builder.redirectErrorStream(true);
			final Process process = builder.start();
			final InputStream is = process.getInputStream();
			new Thread(() -> {
				try (final BufferedReader reader = new BufferedReader(new InputStreamReader(is));) {
					String line = null;
					while ((line = reader.readLine()) != null) {
						// TODO: handle line
					}
				} catch (final IOException e) {
					if (LOG.isInfoEnabled()) {
						final String error = e.getMessage();
						LOG.error(error);
					}
				}
			}).start();
			Thread waitForThread = new Thread(() -> {
				try {
					process.waitFor();
				} catch (final InterruptedException e) {
					if (LOG.isInfoEnabled()) {
						final String error = e.getMessage();
						LOG.error(error);
					}
				}
			});
			waitForThread.start();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}
}
