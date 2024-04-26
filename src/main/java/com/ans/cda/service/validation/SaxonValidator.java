package com.ans.cda.service.validation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;

/**
 * SaxonValidator
 * 
 * @author bensa
 */
public class SaxonValidator {
	/**
	 * wXpe
	 */
	protected static XPathCompiler wXpe;
	/**
	 * wProcessor
	 */
	public static Processor wProcessor = new Processor();
	/**
	 * wXmlDoc
	 */
	public static XdmNode wXmlDoc;
	/**
	 * wBuilder
	 */
	protected static DocumentBuilder wBuilder;
	/**
	 * wCheminDocument
	 */
	public static String wCheminDocument;
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(SaxonValidator.class);
	/**
	 * fileName
	 */
	private static final String FILENAME = System.getProperty("user.home");
	/**
	 * newFilePath
	 */
	private static final Path NEWFILEPATH = Paths.get(FILENAME + "//document_validation_last_result_URL.txt");
	/**
	 * newFilePath1
	 */
	private static final Path NEWFILEPATH1 = Paths.get(FILENAME + "//document_validation_last_result.xml");
	/**
	 * newFilePath2
	 */
	private static final Path NEWFILEPATH2 = Paths.get(FILENAME + "//document_validation_last_report.xml");
	/**
	 * newFilePath3
	 */
	private static final Path NEWFILEPATH3 = Paths.get(FILENAME + "//document_validation_last_request.xml");

	/**
	 * getXpathSingleValue
	 * 
	 * @param file
	 * @return
	 */
	public static String getXpathSingleValue(final File file, final boolean done) {
		String content = null;
		if (done) {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document document;
			try {
				final javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);
				document.getDocumentElement().normalize();
				final NodeList nList = document.getElementsByTagName("evs:status");
				if (nList != null && nList.getLength() > 0) {
					for (int temp = 0; temp < nList.getLength(); temp++) {
						final Node node = nList.item(temp);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							final Element eElement = (Element) node;
							content = eElement.getTextContent();
						}
					}
				}
			} catch (final ParserConfigurationException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			} catch (final SAXException e) {
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
		return content;
	}

	/**
	 * valideDocument
	 * 
	 * @param pDocumentContent
	 * @param pValidationServiceName
	 * @param pValidatorName
	 * @return
	 */
	public static String valideDocument(final String pDocumentContent, final String pValidationSName,
			final String pValidatorName, final String validationUrl) {
		NEWFILEPATH3.toFile().delete();
		NEWFILEPATH2.toFile().delete();
		NEWFILEPATH1.toFile().delete();
		NEWFILEPATH.toFile().delete();
		final String encodedDocument = encodeBase64(pDocumentContent);
		String locationHeader;
		boolean done = false;
		// Récupérer le nom du validateur CDA
		final URL fileF = SaxonValidator.class.getResource("/API/templateRequeteValidation.txt");
		// Set cursor as hourglass
		String sRequete = null;
		try {
			if (new File(fileF.toURI()).exists()) {
				sRequete = readFile(new File(fileF.toURI()).getAbsolutePath());
				sRequete = sRequete.replace("$CONTENT$", encodedDocument);
				sRequete = sRequete.replace("$VALIDATION-SERVICE-NAME$", pValidationSName);
				sRequete = sRequete.replace("$VALIDATOR$", pValidatorName);
				if (!NEWFILEPATH3.toFile().exists()) {
					Files.createFile(NEWFILEPATH3);
				}
				if (!NEWFILEPATH.toFile().exists()) {
					Files.createFile(NEWFILEPATH);
				}
				if (!NEWFILEPATH1.toFile().exists()) {
					Files.createFile(NEWFILEPATH1);
				}
				if (!NEWFILEPATH2.toFile().exists()) {
					Files.createFile(NEWFILEPATH2);
				}
				try {
					Files.writeString(NEWFILEPATH3.toFile().toPath(), sRequete, StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (final URISyntaxException e) {
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
		try {
			final URL url = new URL(validationUrl);
			final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			httpCon.setRequestProperty("Content-Type", "application/xml");
			httpCon.setConnectTimeout(30000000);
			final String postData = sRequete;
			try (OutputStream os = httpCon.getOutputStream()) {
				final byte[] input = postData.getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			final int responseCode = httpCon.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED
					|| responseCode == HttpURLConnection.HTTP_CREATED) {
				locationHeader = httpCon.getHeaderField("Location");
				writeFile(NEWFILEPATH.toFile().getAbsolutePath(), locationHeader);
				final URL url1 = new URL(locationHeader);
				final HttpURLConnection httpConnnection = (HttpURLConnection) url1.openConnection();
				httpConnnection.setRequestMethod("GET");
				httpConnnection.setRequestProperty("Accept", "application/xml");
				httpConnnection.setConnectTimeout(30000000);
				final int responseCode1 = httpConnnection.getResponseCode();
				if (responseCode1 == HttpURLConnection.HTTP_OK || responseCode1 == HttpURLConnection.HTTP_ACCEPTED
						|| responseCode1 == HttpURLConnection.HTTP_CREATED) {
					try (InputStream inputStream = httpConnnection.getInputStream();
							InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
						StringBuilder response = new StringBuilder();
						int read;
						final char[] buffer = new char[200000];
						while ((read = inputStreamReader.read(buffer)) != -1) {
							response.append(buffer, 0, read);
						}
						Files.writeString(NEWFILEPATH1.toFile().toPath(), response.toString(), StandardCharsets.UTF_8);
						done = true;
					}
				} else {
					Files.writeString(NEWFILEPATH1.toFile().toPath(),
							"!!!{Erreur lors de la récupération du résultat de validation - TimeOut}",
							StandardCharsets.UTF_8);
				}
				final URL url2 = new URL(locationHeader + "/report");
				final HttpURLConnection httpConnnection2 = (HttpURLConnection) url2.openConnection();
				httpConnnection2.setRequestMethod("GET");
				httpConnnection2.setRequestProperty("Accept", "application/xml");
				httpConnnection2.setConnectTimeout(30000000);
				final int responseCode2 = httpConnnection2.getResponseCode();
				if (responseCode2 == HttpURLConnection.HTTP_OK || responseCode2 == HttpURLConnection.HTTP_ACCEPTED
						|| responseCode2 == HttpURLConnection.HTTP_CREATED) {
					try (InputStream inputStream = httpConnnection2.getInputStream();
							InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
						StringBuilder response = new StringBuilder();
						int read;
						final char[] buffer = new char[200000];
						while ((read = inputStreamReader.read(buffer)) != -1) {
							response.append(buffer, 0, read);
						}
						Files.writeString(NEWFILEPATH2.toFile().toPath(), response.toString(), StandardCharsets.UTF_8);
						done = true;
					}
				} else {
					Files.writeString(NEWFILEPATH2.toFile().toPath(),
							"!!!{Erreur lors de la récupération du résultat de validation - TimeOut}",
							StandardCharsets.UTF_8);
				}
			} else {
				writeFile(NEWFILEPATH.toFile().getAbsolutePath(), "!!!ERROR_FETCHING_RESULT");
			}
		} catch (final IOException e) {
			writeFile(NEWFILEPATH.toFile().getAbsolutePath(), "!!!ERROR_FETCHING_RESULT");
		}
		final String xpath = getXpathSingleValue(NEWFILEPATH1.toFile(), done);
		return xpath;
	}

	/**
	 * encodeBase64
	 * 
	 * @param input
	 * @return
	 */
	private static String encodeBase64(final String input) {
		return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * readFile
	 * 
	 * @param filePath
	 * @return
	 */
	private static String readFile(final String filePath) {
		final Path path = Path.of(filePath);
		String content = null;
		try {
			content = Files.readString(path);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return content;
	}

	/**
	 * writeFile
	 * 
	 * @param filePath
	 * @param content
	 */
	private static void writeFile(final String filePath, final String content) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(filePath));
			writer.write(content);
			writer.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}
}