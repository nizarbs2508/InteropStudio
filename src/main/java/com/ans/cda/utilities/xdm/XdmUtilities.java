package com.ans.cda.utilities.xdm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ans.cda.constant.Constant;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * XdmUtilities
 * 
 * @author bensa
 */
public class XdmUtilities {
	/**
	 * Size of the buffer to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;
	/**
	 * HOME
	 */
	private static final String HOME = System.getProperty("user.home");
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(XdmUtilities.class);

	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified by
	 * destDirectory (will be created if does not exists)
	 * 
	 * @param zipFilePath
	 * @param destDirectory
	 * @throws IOException
	 */
	public static void unzip(final String zipFilePath, final String destDirectory) throws IOException {
		final File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		final ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		while (entry != null) {
			final String filePath = destDirectory + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				extractFile(zipIn, filePath);
			} else {
				final File dir = new File(filePath);
				dir.mkdirs();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	/**
	 * downloadFileUsingNIO
	 * 
	 * @param urlStr
	 * @param file
	 * @throws IOException
	 */
	public static void downloadFileUsingNIO(final String urlStr, final String file) throws IOException {
		final URL url = new URL(urlStr);
		FileUtils.copyURLToFile(url, new File(file));
	}

	/**
	 * readJsonFile
	 * 
	 * @param file
	 */
	public static List<File> readJsonFile(final String file) {
		final JSONParser jsonP = new JSONParser();
		final List<File> listFile = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(new File(file).toURI()))) {
			final Object obj = jsonP.parse(reader);
			final org.json.simple.JSONObject jObject = (org.json.simple.JSONObject) obj;
			final JSONArray jArray = (JSONArray) jObject.get("entry");
			List<File> lfiles = new ArrayList<>();
			if (jArray != null) {
				for (final Object jObj : jArray) {
					final org.json.simple.JSONObject object = (org.json.simple.JSONObject) jObj;
					final String fullUrl = (String) object.get("fullUrl");
					if (fullUrl.contains("JDV-J06-") || fullUrl.contains("JDV-J10-") || fullUrl.contains("TRE-A05-")) {
						final org.json.simple.JSONObject resources = (org.json.simple.JSONObject) object
								.get("resource");
						final String name = (String) resources.get("name");
						final File file2 = new File(HOME + Constant.JSONFOLDER + name.concat(Constant.EXTENSIONJSON));
						downloadFileUsingNIO(fullUrl, file2.getAbsolutePath());
						if (file2.getName().startsWith(Constant.JDVFIRST)) {
							lfiles.add(file2);
						}
						if (file2.getName().startsWith(Constant.TREFIRST)) {
							lfiles.add(file2);
						}
					}
				}
			}
			if (!lfiles.isEmpty()) {
				for (final File file3 : lfiles) {
					final String str = ConvertJsonToXML.convert(file3.getAbsolutePath(), HOME);
					final String json = xmlToJson(str, file3.getParentFile());
					listFile.add(new File(json));
				}
			}
		} catch (final IOException | ParseException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return listFile;
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
	 * xmlToJson
	 * 
	 * @param xml
	 * @return
	 */
	public static String xmlToJson(final String xml, final File file) {
		String line = "", str = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(xml));
			while ((line = br.readLine()) != null) {
				str += line;
			}
		} catch (final FileNotFoundException e) {
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
		JSONObject jsondata = XML.toJSONObject(str);
		final File fileJson = new File(file + "\\" + removeExtension(new File(xml).getName()) + ".json");
		try {
			Files.writeString(fileJson.toPath(), jsondata.toString(), StandardCharsets.UTF_8);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return fileJson.getAbsolutePath();

	}

	/**
	 * deleteDirectory
	 * 
	 * @param directoryToBeDeleted
	 * @return
	 */
	static boolean deleteDirectory(final File directoryToBeDeleted) {
		final File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (final File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	/**
	 * décompresse le fichier zip dans le répertoire donné
	 * 
	 * @param folder  le répertoire où les fichiers seront extraits
	 * @param zipfile le fichier zip à décompresser
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<File> unzipFile(final String zipFilePath, final String destFilePath) throws IOException {
		final File destination = new File(destFilePath);
		final List<File> file = new ArrayList<>();
		if (!destination.exists()) {
			destination.mkdir();
		}
		if (new File(destFilePath).exists()) {
			new File(destFilePath).delete();
		}
		final Charset cp866 = Charset.forName(Constant.CHARSET);
		try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)), cp866)) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				final String filePath = destination + File.separator + zipEntry.getName();
				if (zipEntry.isDirectory()) {
					final File directory = new File(filePath);
					directory.mkdirs();
				} else {
					if (zipEntry.getName().startsWith("ASS_X04-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
					if (zipEntry.getName().startsWith("ASS_A11-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
					if (zipEntry.getName().startsWith("JDV_J06-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
					if (zipEntry.getName().startsWith("JDV_J10-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
					if (zipEntry.getName().startsWith("TRE_A04-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
					if (zipEntry.getName().startsWith("TRE_A05-")) {
						final String str = extractZipFile(zipInputStream, filePath);
						file.add(new File(str));
					}
				}
				zipInputStream.closeEntry();
				zipEntry = zipInputStream.getNextEntry();
			}
		}
		return file;
	}

	/**
	 * extractFile
	 * 
	 * @param Zip_Input_Stream
	 * @param File_Path
	 * @throws IOException
	 */
	private static String extractZipFile(final ZipInputStream zipInputStream, final String filePath)
			throws IOException {
		try (final BufferedOutputStream bufferedOutput = new BufferedOutputStream(
				Files.newOutputStream(Paths.get(filePath)))) {
			final byte[] bytes = new byte[4096];
			int readByte = zipInputStream.read(bytes);
			while (readByte != -1) {
				bufferedOutput.write(bytes, 0, readByte);
				readByte = zipInputStream.read(bytes);
			}
		}
		final Path source = Paths.get(filePath);
		final String sourceReplace = source.toFile().getName().replace('-', '_');
		final Path sourceFile = source.resolveSibling(sourceReplace);
		if (sourceFile.toFile().exists()) {
			sourceFile.toFile().delete();
		}
		Files.move(source, sourceFile);
		String name = new File(filePath).getName();
		name = name.substring(0, name.indexOf('.')) + ".tabs";
		final String sourceName = sourceReplace.substring(0, sourceReplace.indexOf('.')) + ".tabs";
		new File(filePath).delete();
		final Path path = Paths.get(sourceFile.toUri());
		final Charset charset = StandardCharsets.UTF_8;
		String content = new String(Files.readAllBytes(path), charset);
		content = content.replaceAll(name, sourceName);
		Files.write(path, content.getBytes(charset));
		return sourceFile.toString();
	}

	/**
	 * extractFile
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private static void extractFile(final ZipInputStream zipIn, final String filePath) throws IOException {
		final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		final byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	/**
	 * marshelling
	 * 
	 * @param file
	 */
	public static String marshelling(final File file, final String sLoincCode) {
		BufferedReader reader;
		String retour = "";
		String retours = "";
		try {
			reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				retour = retour + line;
			}
			reader.close();
			final JSONObject json = XML.toJSONObject(retour);
			final String jsonString = json.toString(4);
			final ObjectMapper objectMapper = new ObjectMapper();
			final Root root = objectMapper.readValue(jsonString, Root.class);
			final List<MappedConcept> mappedConceptList = root.retrieveValueSetResponse.valueSet.mappedConceptList.mappedConcept;
			for (final MappedConcept mapped : mappedConceptList) {
				for (int i = 0; i < mapped.concept.size(); i++) {
					if (sLoincCode.equals(mapped.concept.get(i).code)) {
						retours = mapped.concept.get(i + 1).code;
					}
				}
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return retours;
	}

	/**
	 * marshelling
	 * 
	 * @param file
	 */
	public static List<String> marshellingTre(final File file, final String sLoincCode) {
		List<String> retours = new ArrayList<>();
		BufferedReader reader;
		String retour = "";
		try {
			reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				retour = retour + line;
			}
			reader.close();
			final JSONObject json = XML.toJSONObject(retour);
			final String jsonString = json.toString(4);
			final ObjectMapper objectMapper = new ObjectMapper();
			final RootJdv root = objectMapper.readValue(jsonString, RootJdv.class);
			final List<ConceptJdv> mappedConceptList = root.retrieveValueSetResponse.valueSet.conceptList.concept;
			for (final ConceptJdv mapped : mappedConceptList) {
				if (sLoincCode.equals(mapped.code)) {
					retours.add(mapped.codeSystem);
					retours.add(mapped.displayName);
				}
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return retours;
	}

	/**
	 * marshellingJdv
	 * 
	 * @param file
	 */
	public static List<String> marshellingJdv(final File file, final String code) {
		List<String> retours = new ArrayList<>();
		try {
			final Object o = new JSONParser().parse(new FileReader(file));
			final org.json.simple.JSONObject json = (org.json.simple.JSONObject) o;
			final String jsonString = json.toJSONString();
			final ObjectMapper objectMapper = new ObjectMapper();
			final RootJ root = objectMapper.readValue(jsonString, RootJ.class);
			objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			final List<ConceptJ> mappedConceptList = root.retrieveValueSetResponse.valueSet.conceptList.concept;
			for (final ConceptJ mapped : mappedConceptList) {
				if (code.equals(mapped.code)) {
					retours.add(mapped.codeSystem);
					retours.add(mapped.displayName);
				}
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} catch (ParseException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return retours;
	}

	/**
	 * marshelling
	 * 
	 * @param file
	 */
	public static String getXmlns(final File file, final String code) {
		BufferedReader reader;
		String retour = "";
		String retours = "";
		try {
			reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				retour = retour + line;
			}
			reader.close();
			final JSONObject json = XML.toJSONObject(retour);
			final String jsonString = json.toString(4);
			final ObjectMapper objectMapper = new ObjectMapper();
			final Root root = objectMapper.readValue(jsonString, Root.class);
			final List<MappedConcept> mappedConceptList = root.retrieveValueSetResponse.valueSet.mappedConceptList.mappedConcept;
			for (final MappedConcept mapped : mappedConceptList) {
				for (int i = 0; i < mapped.concept.size(); i++) {
					if (code.equals(mapped.concept.get(i).code)) {
						retours = mapped.concept.get(i + 1).code;
					}
				}
			}
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return retours;
	}

	/**
	 * getHash
	 * 
	 * @param pCheminDocumentXML
	 * @return
	 */
	public static String getHash(final String pCheminDocumentXML) {
		String result;
		try {
			final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			try (final FileInputStream fis = new FileInputStream(pCheminDocumentXML);
					BufferedInputStream bis = new BufferedInputStream(fis)) {
				final byte[] buffer = new byte[8192];
				int read;
				while ((read = bis.read(buffer)) != -1) {
					sha1.update(buffer, 0, read);
				}
			}
			final byte[] hash = sha1.digest();
			final StringBuilder formatted = new StringBuilder(2 * hash.length);
			for (final byte bytes : hash) {
				formatted.append(String.format("%02X", bytes));
			}
			result = formatted.toString();
		} catch (final NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * downloadUsingNIO
	 * 
	 * @param urlStr
	 * @param file
	 * @throws IOException
	 */
	public static List<File> downloadUsingNIO(final String urlStr, final String file) throws IOException {
		final URL url = new URL(urlStr);
		List<File> xmlFile = null;
		FileUtils.copyURLToFile(url, new File(file));
		final String destFilePath = new File(file).getParent();
		try {
			if (file.endsWith(Constant.EXTENSIONJSON)) {
				xmlFile = readJsonFile(file);
			}
		} catch (final Exception e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} finally {
			new File(file).delete();
			new File(destFilePath).delete();
		}
		return xmlFile;
	}

	/**
	 * replaceInFile
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void replaceInFile(final File file) throws IOException {
		final File fileToBeModified = file;
		String oldContent = "";
		BufferedReader reader = null;
		FileWriter writer = null;
		try {
			reader = new BufferedReader(new FileReader(fileToBeModified));
			String line = reader.readLine();
			while (line != null) {
				oldContent = oldContent + line + System.lineSeparator();
				line = reader.readLine();
			}
			final String newContent = oldContent.replaceAll("&amp;amp;", "&amp;");
			final String secondContent = newContent.replaceAll("xmlns=\"\"", "");
			writer = new FileWriter(fileToBeModified);
			writer.write(secondContent);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} finally {
			try {
				reader.close();
				writer.close();
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
	}
}