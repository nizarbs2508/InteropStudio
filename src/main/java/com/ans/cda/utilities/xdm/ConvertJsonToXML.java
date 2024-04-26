package com.ans.cda.utilities.xdm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ans.cda.constant.Constant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * create ConvertJsonToXML class to convert JSON data into XML
 * 
 * @author bensa
 */
public final class ConvertJsonToXML {

	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(ConvertJsonToXML.class);

	/**
	 * ConvertJsonToXML
	 */
	private ConvertJsonToXML() {
		// not called
	}

	/**
	 * create convertToXML() method for converting JSOn data into XML
	 * 
	 * @param jsonString
	 * @param root
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public static String convert(final String loc, final String home) {
		final JSONParser parser = new JSONParser();
		String str = null;
		try {
			final Object obj = parser.parse(Files.newBufferedReader(Paths.get(loc)));
			final JSONObject jsonObject = (JSONObject) obj;
			final String dateMaj = (String) jsonObject.get("date");
			String date = null;
			if (dateMaj != null) {
				final String[] words = dateMaj.split("T");
				final String words0 = words[0];
				final String[] words1 = words0.split("-");
				final String words01 = words1[0];
				final String words02 = words1[1];
				final String words03 = words1[2];
				final String words2 = words[1];
				final String[] words21 = words2.split(":");
				final String words211 = words21[0];
				final String words212 = words21[1];
				final String words213 = words21[2];
				final String words31 = words213.substring(0, 2);
				date = words01 + words02 + words03 + words211 + words212 + words31;
			}
			final String description = (String) jsonObject.get("description");
			final String name = (String) jsonObject.get("name");
			final String url = (String) jsonObject.get("url");
			String dateV = null;
			String value;
			String ident = null;
			Map<String, String> start = null;
			String dateValid;
			List<Map<String, String>> conceptMap;
			final JSONArray identifier = (JSONArray) jsonObject.get("identifier");
			final ObjectMapper mapper = new ObjectMapper();
			if (identifier != null) {
				for (final Object object : identifier) {
					final Map<String, String> map = mapper.readValue(object.toString(), HashMap.class);
					for (final Map.Entry<String, String> mapentry : map.entrySet()) {
						if (Constant.VALUE.equals(mapentry.getKey())) {
							value = mapentry.getValue();
							final String[] wordsI = value.split(":");
							ident = wordsI[2];
						}
					}
				}
			}
			final JSONArray extension = (JSONArray) jsonObject.get("extension");
			if (extension != null) {
				for (final Object object : extension) {
					final Map<String, String> map = mapper.readValue(object.toString(), HashMap.class);
					for (final Iterator<Entry<String, String>> iterator = map.entrySet().iterator(); iterator
							.hasNext();) {
						final Map.Entry mapentry = iterator.next();
						if (Constant.VALUEPERIOD.equals(mapentry.getKey())) {
							start = (Map<String, String>) mapentry.getValue();
						}
					}
				}
			}
			if (start != null) {
				for (final Iterator<Entry<String, String>> iterator = start.entrySet().iterator(); iterator
						.hasNext();) {
					final Entry<String, String> mapentry = iterator.next();
					if (Constant.START.equals(mapentry.getKey())) {
						dateValid = mapentry.getValue();
						final String[] wordsV = dateValid.split("T");
						final String words0V = wordsV[0];
						final String[] words1V = words0V.split("-");
						final String words01V = words1V[0];
						final String words02V = words1V[1];
						final String words03V = words1V[2];
						final String words2V = wordsV[1];
						final String[] words21V = words2V.split(":");
						final String words211V = words21V[0];
						final String words212V = words21V[1];
						final String words213V = words21V[2];
						final String words31V = words213V.substring(0, 2);
						dateV = words01V + words02V + words03V + words211V + words212V + words31V;
					}
				}
			}
			List<ConceptList> listConcept = null;
			if (jsonObject.get("compose") != null) {
				final JSONObject compose = (JSONObject) jsonObject.get("compose");
				final JSONArray include = (JSONArray) compose.get("include");
				ConceptList conceptL;
				listConcept = new ArrayList<>();
				for (final Object object : include) {
					final JSONObject inc = (JSONObject) object;
					final Map<String, Object> map = mapper.readValue(inc.toString(), HashMap.class);
					conceptL = new ConceptList();
					for (final Map.Entry<String, Object> mapentry : map.entrySet()) {
						if (Constant.CONCEPT1.equals(mapentry.getKey())) {
							conceptMap = (List<Map<String, String>>) mapentry.getValue();
							for (final Map<String, String> mapV : conceptMap) {
								final ConceptJdv concept = new ConceptJdv();
								for (final Map.Entry<String, String> mapV1 : mapV.entrySet()) {
									if (Constant.CODE.equals(mapV1.getKey())) {
										concept.setCode(mapV1.getValue());
									} else if (Constant.DISPLAY.equals(mapV1.getKey())) {
										concept.setDisplayName(mapV1.getValue());
									}
								}
								conceptL.concept.add(concept);
							}
						} else if (Constant.SYSTEM.equals(mapentry.getKey())) {
							final String system = (String) mapentry.getValue();
							conceptL.setSystem(system);
						}
					}
					listConcept.add(conceptL);
				}
			}
			if (jsonObject.get("concept") != null) {
				final JSONArray compose = (JSONArray) jsonObject.get("concept");
				ConceptList conceptL = new ConceptList();
				listConcept = new ArrayList<>();
				for (final Object object : compose) {
					final JSONObject inc = (JSONObject) object;
					final Map<String, String> map = mapper.readValue(inc.toString(), HashMap.class);
					final ConceptJdv concept = new ConceptJdv();
					for (final Map.Entry<String, String> mapentry : map.entrySet()) {
						concept.setCodeSystem(ident);
						if (Constant.CODE.equals(mapentry.getKey())) {
							concept.setCode(mapentry.getValue());
						}
						if (Constant.DISPLAY.equals(mapentry.getKey())) {
							concept.setDisplayName(mapentry.getValue());
						}
						if (concept.getCode() != null && concept.getDisplayName() != null
								&& concept.getCodeSystem() != null) {
							conceptL.concept.add(concept);
							break;
						}
					}
				}
				listConcept.add(conceptL);
			}
			final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			// root elements
			final Document doc = docBuilder.newDocument();
			final Element rootElement = doc.createElement("RetrieveValueSetResponse");
			rootElement.setAttribute("xmlns", "urn:ihe:iti:svs:2008");
			rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			doc.appendChild(rootElement);
			final Element valueSet = doc.createElement("ValueSet");
			valueSet.setAttribute("dateFin", "");
			valueSet.setAttribute("dateMaj", date);
			valueSet.setAttribute("dateValid", dateV);
			valueSet.setAttribute("description", description);
			valueSet.setAttribute("displayName", name + ".tabs");
			valueSet.setAttribute("id", ident);
			if (name.startsWith("JDV")) {
				valueSet.setAttribute("typeFichier", "JDV");
			} else if (name.startsWith("TRE")) {
				valueSet.setAttribute("typeFichier", "TRE");
			}
			valueSet.setAttribute("urlFichier", url);
			rootElement.appendChild(valueSet);
			final Element conceptList = doc.createElement("ConceptList");
			valueSet.appendChild(conceptList);
			for (final ConceptList conceptListF : listConcept) {
				for (final ConceptJdv concepT : conceptListF.concept) {
					final Element concept = doc.createElement("Concept");
					concept.setAttribute("code", concepT.getCode());
					concept.setAttribute("displayName", concepT.getDisplayName());
					if (name.startsWith("JDV")) {
						final String system = conceptListF.getSystem();
						final String[] systems = system.split("/");
						final String lastOne = systems[systems.length - 1];
						final File file = new File(home + Constant.CODEFOLDER + lastOne + Constant.EXTENSIONJSON);
						if (!file.exists()) {
							XdmUtilities.downloadFileUsingNIO(Constant.CODESYSTEM + system, file.getAbsolutePath());
						}
						final ObjectMapper objectMapper = new ObjectMapper();
						final JsonNode root = objectMapper.readTree(new File(file.getAbsolutePath()));
						final JsonNode field1 = root.get("entry");
						if (field1 != null) {
							if (field1.isArray()) {
								for (final JsonNode objNode : field1) {
									final JsonNode field2 = objNode.get("resource");
									for (final JsonNode objNode2 : field2) {
										try {
											if (objNode2.toString().startsWith("[{\"system\":")) {
												final String identifier2 = objNode2.toString();
												final String[] wordT = identifier2.split("urn:oid:");
												final String words0T = wordT[1];
												final String[] wordTT = words0T.split("\"}]");
												final String words1T = wordTT[0];
												concept.setAttribute("codeSystem", words1T);
												break;
											}
										} catch (final Exception e) {
											if (LOG.isInfoEnabled()) {
												final String error = e.getMessage();
												LOG.error(error);
											}
										}
									}
								}
							}
						} 
					} else if (name.startsWith("TRE")) {
						concept.setAttribute("codeSystem", concepT.getCodeSystem());
					}
					conceptList.appendChild(concept);
				}
			}
			final String fileName = FilenameUtils.removeExtension(loc);
			final String fName = new File(fileName).getName();
			final File folder = new File(home + Constant.IMAGE9);
			// write dom document to a file
			str = folder + "\\" + fName + Constant.EXTXML;
			try (OutputStream output = Files.newOutputStream(Paths.get(str))) {
				writeXml(doc, output);
			} catch (final IOException | TransformerException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		} catch (final IOException | ParserConfigurationException | ParseException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
		return str;
	}

	/**
	 * write doc to output stream
	 * 
	 * @param doc
	 * @param output
	 * @throws TransformerException
	 */
	private static void writeXml(final Document doc, final OutputStream output)
			throws TransformerException, UnsupportedEncodingException {
		final TransformerFactory transformerFac = TransformerFactory.newInstance();
		Transformer transformer = transformerFac.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, Constant.YES);
		transformer.setOutputProperty(OutputKeys.ENCODING, Constant.UTF8);
		final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try (InputStream iStream = classloader.getResourceAsStream(Constant.PRETTY)) {
			transformer = transformerFac.newTransformer(new StreamSource(iStream));
			final DOMSource source = new DOMSource(doc);
			final StreamResult result = new StreamResult(output);
			transformer.transform(source, result);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}
}
