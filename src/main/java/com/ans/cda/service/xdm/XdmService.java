package com.ans.cda.service.xdm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ans.cda.constant.Constant;
import com.ans.cda.service.bom.BomService;
import com.ans.cda.utilities.validation.IniFile;
import com.ans.cda.utilities.xdm.XdmUtilities;

import javafx.stage.Stage;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

/**
 * XdmService
 * 
 * @author bensa
 */
public class XdmService {

	/**
	 * sCheminCourantExe
	 */
	public static final String SCHEMINCOURANTEXE = System.getProperty("user.dir");
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(XdmService.class);
	/**
	 * sChemin
	 */
	private static Map<String, String> sChemin = new HashMap<>();
	/**
	 * FILENAME
	 */
	private static final String FILENAME = System.getProperty("user.home");
	/**
	 * documentEntryUUID
	 */
	private static String[] documentEntryUUID = new String[20];
	/**
	 * uuidAsStringId
	 */
	private static final String uuidAsStringId = random();
	/**
	 * gTemplateID
	 */
	public static String gTemplateID = "";

	/**
	 * openXDMFile
	 * 
	 * @param stage
	 */
	public static Map<String, String> openXDMFile(final Stage stage, final File file) {
		final IniFile iniFile = new IniFile(null);
		String sRepertoireCourant = iniFile.read("LAST-PATH-USED", "MEMORY");
		if (sRepertoireCourant.length() == 0) {
			sRepertoireCourant = SCHEMINCOURANTEXE + "\\";
		}
		if (file != null) {
			final String filePath = file.getAbsolutePath();
			final String sCheminTempDir = SCHEMINCOURANTEXE + "\\ZIPTEMP";
			final String sCheminDocuments = sCheminTempDir + "\\IHE_XDM\\SUBSET01";
			final String sCheminCDA = sCheminDocuments + "\\" + iniFile.read("DEFAULT_CDA_NAME", "IHE_XDM");
			final String sCheminMETA = sCheminDocuments + "\\" + iniFile.read("DEFAULT_METADATA_NAME", "IHE_XDM");
			try {
				FileUtils.deleteDirectory(new File(sCheminTempDir));
				XdmUtilities.unzip(filePath, sCheminTempDir);
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
			if (!new File(sCheminCDA).exists()) {
				if (LOG.isInfoEnabled()) {
					final String error = "Impossible de trouver le fichier CDA dans " + sCheminCDA;
					LOG.error(error);
				}
			} else {
				iniFile.write("LAST-CDA-FILE", sCheminCDA, "MEMORY");
				iniFile.write("LAST-PATH-USED", sCheminDocuments, "MEMORY");
			}

			if (!new File(sCheminMETA).exists()) {
				if (LOG.isInfoEnabled()) {
					final String error = "Impossible de trouver le fichier metadata dans " + sCheminMETA;
					LOG.error(error);
				}
			} else {
				iniFile.write("LAST-META-FILE", sCheminMETA, "MEMORY");
			}
			sChemin.put(sCheminCDA, sCheminMETA);
		}
		return sChemin;
	}

	/**
	 * writeXml
	 * 
	 * @param doc
	 * @param output
	 * @throws TransformerException
	 */
	private static void writeXml(final Document doc, final OutputStream output, final Path path)
			throws TransformerException {
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		final Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
		final DOMSource source = new DOMSource(doc);
		final StreamResult result = new StreamResult(output);
		transformer.transform(source, result);
		try {
			XdmUtilities.replaceInFile(path.toFile());
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * random
	 * 
	 * @return
	 */
	private static String random() {
		final UUID uuid = UUID.randomUUID();
		return "urn:uuid:" + uuid.toString();
	}

	/**
	 * setRootElement
	 * 
	 * @param doc
	 * @return
	 */
	public static Element setRootElement(final Document doc) {
		final Element rootElement = doc.createElement("ns5:SubmitObjectsRequest");
		doc.appendChild(rootElement);
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
		rootElement.setAttribute("xmlns:ns5", "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0");
		return rootElement;
	}

	/**
	 * setSecondElement
	 * 
	 * @param doc
	 * @return
	 */
	public static Element setSecondElement(final Document doc) {
		final Element secondElement = doc.createElement("ns2:RegistryPackage");
		secondElement.setAttribute("xmlns:ns2", "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
		secondElement.setAttribute("id", uuidAsStringId);
		secondElement.setAttribute("status", "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved");
		return secondElement;
	}

	/**
	 * setFiveElement
	 * 
	 * @param doc
	 * @return
	 */
	public static Element setFiveElement(final Document doc) {
		final Element fiveElement = doc.createElement("ns2:Value");
		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		final LocalDateTime now = LocalDateTime.now();
		fiveElement.setTextContent(dtf.format(now));
		return fiveElement;
	}

	/**
	 * setSevenElement
	 * 
	 * @param doc
	 * @return
	 */
	public static Element setSevenElement(final Document doc, final List<String> pListCda) {
		final Element sevenElement = doc.createElement("ns2:LocalizedString");
		sevenElement.setAttribute("xml:lang", "FR");
		sevenElement.setAttribute("charset", "UTF8");
		final String title = getXpathSingleValue(new File(pListCda.get(0)), "//*:ClinicalDocument/*:title/string()");
		sevenElement.setAttribute("value", title);
		return sevenElement;
	}

	/**
	 * setNineElement
	 * 
	 * @param doc
	 * @return
	 */
	public static Element setNineElement(final Document doc) {
		final Element nineElement = doc.createElement("ns2:LocalizedString");
		nineElement.setAttribute("xml:lang", "FR");
		nineElement.setAttribute("charset", "UTF8");
		nineElement.setAttribute("value", "");
		return nineElement;
	}

	/**
	 * setTeenElement
	 * 
	 * @param doc
	 * @param secondElement
	 * @return
	 */
	public static Element setTeenElement(final Document doc, final Element secondElement) {
		final Element teenElement = doc.createElement("ns2:Classification");
		teenElement.setAttribute("id", "SubmissionSet01_c001");
		teenElement.setAttribute("classificationNode", "urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd");
		teenElement.setAttribute("classifiedObject", uuidAsStringId);
		teenElement.setAttribute("nodeRepresentation", "");
		return teenElement;
	}

	/**
	 * setQuatreElement
	 * 
	 * @param doc
	 * @param sHealthcareFacilityCode
	 * @return
	 */
	public static Element setQuatreElement(final Document doc, final String sHealthcareFacilityCode) {
		final Element quatreElement = doc.createElement("ns2:Classification");
		quatreElement.setAttribute("id", "SubmissionSet01_c002");
		quatreElement.setAttribute("classificationScheme", "urn:uuid:aa543740-bdda-424e-8c96-df4873be8500");
		quatreElement.setAttribute("classifiedObject", uuidAsStringId);
		quatreElement.setAttribute("nodeRepresentation", sHealthcareFacilityCode);
		return quatreElement;
	}

	/**
	 * seteeElement
	 * 
	 * @param doc
	 * @param sHealthcareFacilityDN
	 * @return
	 */
	public static Element seteeElement(final Document doc, final String sHealthcareFacilityDN) {
		final Element eeElement = doc.createElement("ns2:LocalizedString");
		eeElement.setAttribute("xml:lang", "FR");
		eeElement.setAttribute("charset", "UTF8");
		eeElement.setAttribute("value", sHealthcareFacilityDN);
		return eeElement;
	}

	/**
	 * setElem
	 * 
	 * @param doc
	 * @param pListCda
	 * @return
	 */
	public static Element setElem(final Document doc, final List<String> pListCda) {
		final Element eElem = doc.createElement("ns2:ExternalIdentifier");
		eElem.setAttribute("id", random());
		eElem.setAttribute("identificationScheme", "urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446");
		eElem.setAttribute("registryObject", "urn:uuid:a6e06ca8-0c75-4064-9e5d-88b9045a9ab6");
		eElem.setAttribute("value", getPatientId(new File(pListCda.get(0))));
		return eElem;
	}

	/**
	 * setElem1
	 * 
	 * @param doc
	 * @param sSourceId
	 * @return
	 */
	public static Element setElem1(final Document doc, final String sSourceId) {
		final Element eElem1 = doc.createElement("ns2:ExternalIdentifier");
		eElem1.setAttribute("id", random());
		eElem1.setAttribute("identificationScheme", "urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832");
		eElem1.setAttribute("registryObject", "urn:uuid:a6e06ca8-0c75-4064-9e5d-88b9045a9ab6");
		eElem1.setAttribute("value", sSourceId);
		return eElem1;
	}

	/**
	 * boomFile
	 * 
	 * @param pListCda
	 */
	private static void boomFile(final List<String> pListCda) {
		for (final String cda : pListCda) {
			try {
				BomService.saveAsUTF8WithoutBOM(cda, null);
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		}
	}

	/**
	 * generateMeta XDM
	 * 
	 * @param pListCda
	 */
	public static void generateMeta(final List<String> pListCda) {
		boomFile(pListCda);
		final Path path = Paths.get(FILENAME + "\\" + "nouveauDoc.xml");
		try (final FileOutputStream output = new FileOutputStream(path.toFile())) {
			final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			final Document doc = docBuilder.newDocument();
			// ns5:SubmitObjectsRequest
			final Element rootElement = setRootElement(doc);
			// RegistryObjectList
			final Element firstElement = doc.createElement("RegistryObjectList");
			firstElement.setAttribute("xmlns", "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
			rootElement.appendChild(firstElement);
			// ns2:RegistryPackage
			final Element secondElement = setSecondElement(doc);
			firstElement.appendChild(secondElement);
			// ns2:Slot
			final Element thirdElement = doc.createElement("ns2:Slot");
			thirdElement.setAttribute("name", "submissionTime");
			secondElement.appendChild(thirdElement);
			// ns2:ValueList
			final Element fourElement = doc.createElement("ns2:ValueList");
			thirdElement.appendChild(fourElement);
			// ns2:Value
			final Element fiveElement = setFiveElement(doc);
			fourElement.appendChild(fiveElement);
			// ns2:Name
			final Element sixElement = doc.createElement("ns2:Name");
			secondElement.appendChild(sixElement);
			// ns2:LocalizedString
			final Element sevenElement = setSevenElement(doc, pListCda);
			sixElement.appendChild(sevenElement);
			// ns2:Description
			final Element eightElement = doc.createElement("ns2:Description");
			secondElement.appendChild(eightElement);
			// ns2:LocalizedString
			final Element nineElement = setNineElement(doc);
			eightElement.appendChild(nineElement);
			// ns2:Classification
			final Element teenElement = setTeenElement(doc, secondElement);
			secondElement.appendChild(teenElement);
			// ns2:Classification --> Author attributes
			final String submissionSet01_c001 = random();
			long nbAuthors = Long.parseUnsignedLong(
					getXpathSingleValue(new File(pListCda.get(0)), "count(//*:ClinicalDocument/*:author)"), 16);
			for (int i = 0; i < (int) nbAuthors; i++) {
				final Element elevenElement = doc.createElement("ns2:Classification");
				secondElement.appendChild(elevenElement);
				elevenElement.setAttribute("id", submissionSet01_c001);
				elevenElement.setAttribute("classificationScheme", "urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d");
				elevenElement.setAttribute("classifiedObject", uuidAsStringId);
				elevenElement.setAttribute("nodeRepresentation", "");
				final String sAuthorInstitution = getAuthorInstitution(Integer.valueOf(i + 1),
						new File(pListCda.get(0)));
				if (!sAuthorInstitution.isEmpty()) {
					// AuthorInstitution
					final Element twelveElement = doc.createElement("ns2:Slot");
					elevenElement.appendChild(twelveElement);
					twelveElement.setAttribute("name", "authorInstitution");
					final Element thirteenElement = doc.createElement("ns2:ValueList");
					twelveElement.appendChild(thirteenElement);
					final Element fourteenElement = doc.createElement("ns2:Value");
					thirteenElement.appendChild(fourteenElement);
					fourteenElement.setTextContent(sAuthorInstitution);
				}
				final String sAuthorPerson = getAuthorPerson(Integer.valueOf(i + 1), new File(pListCda.get(0)));
				if (!sAuthorPerson.isEmpty()) {
					// sAuthorPerson
					final Element fifteenElement = doc.createElement("ns2:Slot");
					elevenElement.appendChild(fifteenElement);
					fifteenElement.setAttribute("name", "authorPerson");
					final Element seventeenElement = doc.createElement("ns2:ValueList");
					fifteenElement.appendChild(seventeenElement);
					final Element heigtteenElement = doc.createElement("ns2:Value");
					seventeenElement.appendChild(heigtteenElement);
					heigtteenElement.setTextContent(sAuthorPerson);
				}
				final String sAuthorRole = getXpathSingleValue(new File(pListCda.get(0)),
						"//*:author[" + Integer.valueOf(i + 1).toString() + "]/*:functionCode/@displayName/string()");
				if (!sAuthorRole.startsWith("!!!{") && !sAuthorRole.isEmpty()) {
					// sAuthorRole
					final Element unElement = doc.createElement("ns2:Slot");
					elevenElement.appendChild(unElement);
					unElement.setAttribute("name", "authorRole");
					final Element deuxElement = doc.createElement("ns2:ValueList");
					unElement.appendChild(deuxElement);
					final Element troisElement = doc.createElement("ns2:Value");
					deuxElement.appendChild(troisElement);
					troisElement.setTextContent(sAuthorRole);
				}
				final String xPathString = "//*:ClinicalDocument/*:author[" + Integer.valueOf(i + 1).toString()
						+ "]/*:assignedAuthor/*:code/@code";
				final String sAssignedAuthorExiste = getXpathSingleValue(new File(pListCda.get(0)), xPathString);
				if (!sAssignedAuthorExiste.startsWith("!!!{") && !sAssignedAuthorExiste.isEmpty()) {
					// authorSpecialty
					final Element quatreElement = doc.createElement("ns2:Slot");
					elevenElement.appendChild(quatreElement);
					quatreElement.setAttribute("name", "authorSpecialty");
					final Element cinqElement = doc.createElement("ns2:ValueList");
					quatreElement.appendChild(cinqElement);
					final Element sixeElement = doc.createElement("ns2:Value");
					cinqElement.appendChild(sixeElement);
					final String sAuthorSpecialty = getAuthorSpecialty(Integer.valueOf(i + 1),
							new File(pListCda.get(0)));
					sixeElement.setTextContent(sAuthorSpecialty);
				}
			}
			// HealthcareFacility
			final String sHealthcareFacilityCode = getXpathSingleValue(new File(pListCda.get(0)),
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@code/string()");
			final String sHealthcareFacilityCS = getXpathSingleValue(new File(pListCda.get(0)),
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@codeSystem/string()");
			final String sHealthcareFacilityDN = getXpathSingleValue(new File(pListCda.get(0)),
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@displayName/string()");
			// authorSpecialty
			final Element quatreElement = setQuatreElement(doc, sHealthcareFacilityCode);
			secondElement.appendChild(quatreElement);
			final Element septElement = doc.createElement("ns2:Slot");
			quatreElement.appendChild(septElement);
			septElement.setAttribute("name", "codingScheme");
			final Element cinqElement = doc.createElement("ns2:ValueList");
			septElement.appendChild(cinqElement);
			final Element sixeElement = doc.createElement("ns2:Value");
			cinqElement.appendChild(sixeElement);
			sixeElement.setTextContent(sHealthcareFacilityCS);
			final Element eElement = doc.createElement("ns2:Name");
			quatreElement.appendChild(eElement);
			// eeElement
			final Element eeElement = seteeElement(doc, sHealthcareFacilityDN);
			eElement.appendChild(eeElement);
			// PatientId
			final Element eElem = setElem(doc, pListCda);
			secondElement.appendChild(eElem);
			final Element eeElem = doc.createElement("ns2:Name");
			eElem.appendChild(eeElem);
			final Element eeeElem = doc.createElement("ns2:LocalizedString");
			eeElem.appendChild(eeeElem);
			eeeElem.setAttribute("value", "XDSSubmissionSet.patientId");
			// SourceId
			String sSourceId = getXpathSingleValue(new File(pListCda.get(0)),
					"//*:custodian/*:assignedCustodian/*:representedCustodianOrganization/*:id/@root/string()");
			sSourceId += "." + getXpathSingleValue(new File(pListCda.get(0)),
					"//*:custodian/*:assignedCustodian/*:representedCustodianOrganization/*:id/@extension/string()");
			final Element eElem1 = setElem1(doc, sSourceId);
			secondElement.appendChild(eElem1);
			final Element eeElem1 = doc.createElement("ns2:Name");
			eElem1.appendChild(eeElem1);
			final Element eeeElem1 = doc.createElement("ns2:LocalizedString");
			eeElem1.appendChild(eeeElem1);
			eeeElem1.setAttribute("value", "XDSSubmissionSet.sourceId");
			// UniqueId
			final String sRacineOID = "1.2.250.1.213.1.1.1.1";
			final Date date = new Date();
			final LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			int month = localDate.getMonthValue();
			final Calendar calendar = Calendar.getInstance();
			final String sUniqueId = sRacineOID + "." + localDate.getYear() + "." + month + "." + month + "."
					+ calendar.getTimeInMillis();
			final Element eElem2 = doc.createElement("ns2:ExternalIdentifier");
			secondElement.appendChild(eElem2);
			eElem2.setAttribute("id", random());
			eElem2.setAttribute("identificationScheme", "urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8");
			eElem2.setAttribute("registryObject", "urn:uuid:a6e06ca8-0c75-4064-9e5d-88b9045a9ab6");
			eElem2.setAttribute("value", sUniqueId);
			final Element eeElem2 = doc.createElement("ns2:Name");
			eElem2.appendChild(eeElem2);
			final Element eeeElem2 = doc.createElement("ns2:LocalizedString");
			eeElem2.appendChild(eeeElem2);
			eeeElem2.setAttribute("value", "XDSSubmissionSet.uniqueId");
			for (int k = 0; k < pListCda.size(); k++) {
				buildExtrinsicObject(k, firstElement, doc, new File(pListCda.get(k)));
			}
			for (int k = 0; k < pListCda.size(); k++) {
				buildAssociations(firstElement, doc, new File(pListCda.get(k)), k);
			}
			writeXml(doc, output, path);
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} catch (final TransformerException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} catch (final ParserConfigurationException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * buildAssociations
	 * 
	 * @param firstElement
	 * @param doc
	 * @param file
	 */
	private static void buildAssociations(final Element firstElement, final Document doc, final File file,
			final int k) {
		final String associationId = random();
		final Element eeEle15 = doc.createElement("ns2:Association");
		firstElement.appendChild(eeEle15);
		eeEle15.setAttribute("id", associationId);
		eeEle15.setAttribute("objectType", "Original");
		eeEle15.setAttribute("associationType", "urn:oasis:names:tc:ebxml-regrep:AssociationType:HasMember");
		eeEle15.setAttribute("sourceObject", uuidAsStringId);
		eeEle15.setAttribute("targetObject", random());
		eeEle15.setAttribute("xmlns:ns2", "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
		addExtrinsicObjectSlot("SubmissionSetStatus", "Original", doc, eeEle15);
	}

	/**
	 * buildExtrinsicObject
	 * 
	 * @param pDocumentNumber
	 * @param firstElement
	 * @param doc
	 * @param file
	 */
	private static void buildExtrinsicObject(final int pDocumentNumber, final Element firstElement, final Document doc,
			final File file) {
		documentEntryUUID[pDocumentNumber] = random();
		final Element eElem2 = doc.createElement("ns2:ExtrinsicObject");
		firstElement.appendChild(eElem2);
		eElem2.setAttribute("mimeType", "text/xml");
		eElem2.setAttribute("objectType", "urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1");
		eElem2.setAttribute("id", documentEntryUUID[pDocumentNumber]);
		eElem2.setAttribute("xmlns:ns2", "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
		eElem2.setAttribute("status", "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved");
		final String sVersionNumber = getXpathSingleValue(file, "//*:ClinicalDocument/*:versionNumber/@value/string()");
		final int iVersionNumber = (int) Long.parseUnsignedLong(sVersionNumber, 16);
		if (iVersionNumber > 1) {
			final Element eeElem2 = doc.createElement("ns2:Slot");
			eeElem2.setAttribute("name", "urn:action:extraMetadataSlot");
			eElem2.appendChild(eeElem2);
			final Element eeeElem2 = doc.createElement("ns2:ValueList");
			eeElem2.appendChild(eeeElem2);
			final Element eeeElem3 = doc.createElement("ns2:Value");
			eeeElem2.appendChild(eeeElem3);
			eeeElem3.setTextContent("C");
		}

		final String sHash = XdmUtilities.getHash(file.getAbsolutePath());
		addExtrinsicObjectSlot("hash", sHash, doc, eElem2);
		final long lSize = file.length();
		final String sSize = String.valueOf(lSize);
		addExtrinsicObjectSlot("size", sSize, doc, eElem2);
		final String sDocName = "DOC" + ("000") + String.valueOf(pDocumentNumber + 1) + ".XML";
		addExtrinsicObjectSlot("URI", sDocName, doc, eElem2);
		final String sDHCreationLocale = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:effectiveTime/@value/string()");
		final String[] words = sDHCreationLocale.split("[+]");
		final String year = words[0].substring(0, 4);
		final String month = words[0].substring(4, 6);
		final String day = words[0].substring(6, 8);
		final String heure = words[0].substring(8, 10);
		final String min = words[0].substring(10, 12);
		final String sec = words[0].substring(12, 14);
		final String heureUtc = words[1].substring(0, 2);
		final String minUtc = words[1].substring(2, 4);
		final String sDateHeureCreationUTC = year + month + day + (Integer.valueOf(heure) - Integer.valueOf(heureUtc))
				+ (Integer.valueOf(min) - Integer.valueOf(minUtc)) + sec;
		addExtrinsicObjectSlot("creationTime", sDateHeureCreationUTC, doc, eElem2);
		addExtrinsicObjectSlot("languageCode",
				getXpathSingleValue(file, "//*:ClinicalDocument/*:languageCode/@code/string()"), doc, eElem2);
		addExtrinsicObjectSlot("legalAuthenticator", getLegalAuthenticator(file), doc, eElem2);
		final String sEffectiveTimeLow = getXpathSingleValue(file,
				"//*:documentationOf/*:serviceEvent/*:effectiveTime/*:low/@value/string()");
		final String[] wordss = sEffectiveTimeLow.split("[+]");
		final String years = wordss[0].substring(0, 4);
		final String months = wordss[0].substring(4, 6);
		final String days = wordss[0].substring(6, 8);
		final String heures = wordss[0].substring(8, 10);
		final String mins = wordss[0].substring(10, 12);
		final String secs = wordss[0].substring(12, 14);
		final String heureUtcs = wordss[1].substring(0, 2);
		final String minUtcs = wordss[1].substring(2, 4);
		final String sHeureTransformee = years + months + days + (Integer.valueOf(heures) - Integer.valueOf(heureUtcs))
				+ (Integer.valueOf(mins) - Integer.valueOf(minUtcs)) + secs;
		addExtrinsicObjectSlot("serviceStartTime", sHeureTransformee, doc, eElem2);

		String sEffectiveTimeHigh = getXpathSingleValue(file,
				"//*:documentationOf/*:serviceEvent/*:effectiveTime/*:high/@value/string()");
		final String[] wordsss = sEffectiveTimeHigh.split("[+]");
		final String yearss = wordsss[0].substring(0, 4);
		final String monthss = wordsss[0].substring(4, 6);
		final String dayss = wordsss[0].substring(6, 8);
		final String heuress = wordsss[0].substring(8, 10);
		final String minss = wordsss[0].substring(10, 12);
		final String secss = wordsss[0].substring(12, 14);
		final String heureUtcss = wordsss[1].substring(0, 2);
		final String minUtcss = wordsss[1].substring(2, 4);
		final String sHeureTransformees = yearss + monthss + dayss
				+ (Integer.valueOf(heuress) - Integer.valueOf(heureUtcss))
				+ (Integer.valueOf(minss) - Integer.valueOf(minUtcss)) + secss;
		addExtrinsicObjectSlot("serviceStopTime", sHeureTransformees, doc, eElem2);
		final String sSourcePatientIDXpathReq = "if ( count( //*:patientRole/*:id[not(@root='1.2.250.1.213.1.4.8') and not(@root='1.2.250.1.213.1.4.9')  and not(@root='1.2.250.1.213.1.4.10')   and not(@root='1.2.250.1.213.1.4.11') and not(@root='1.2.250.1.213.1.4.2')] ) > 0 ) then string-join(( //*:patientRole/*:id[not(@root='1.2.250.1.213.1.4.8') and not(@root='1.2.250.1.213.1.4.9') and not(@root='1.2.250.1.213.1.4.10')  and not(@root='1.2.250.1.213.1.4.11') and not(@root='1.2.250.1.213.1.4.2')][1]/@extension /string() , '^^^&amp;' , //*:patientRole/*:id[not(@root='1.2.250.1.213.1.4.8') and not(@root='1.2.250.1.213.1.4.9') and not(@root='1.2.250.1.213.1.4.10')  and not(@root='1.2.250.1.213.1.4.11') and not(@root='1.2.250.1.213.1.4.2')][1]/@root /string() , '&amp;ISO^PI'), '') else if (count( //*:patientRole/*:id[@root='1.2.250.1.213.1.4.8']) >0 ) then string-join(( //*:patientRole/*:id[@root='1.2.250.1.213.1.4.8']/@extension /string(), '^^^&amp;','1.2.250.1.213.1.4.8' , '&amp;ISO^NH'),'') else if (count(  //*:patientRole/*:id[@root='1.2.250.1.213.1.4.9'] )>0) then string-join(( //*:patientRole/*:id[@root='1.2.250.1.213.1.4.9']/@extension /string() , '^^^&amp;', '1.2.250.1.213.1.4.9' , '&amp;ISO^NH'), '') else if (count(  //*:patientRole/*:id[@root='1.2.250.1.213.1.4.10'] )>0) then string-join(( //*:patientRole/*:id[@root='1.2.250.1.213.1.4.10']/@extension /string() , '^^^&amp;', '1.2.250.1.213.1.4.10' , '&amp;ISO^NH'), '') else if (count(  //*:patientRole/*:id[@root='1.2.250.1.213.1.4.11'] )>0) then string-join(( //*:patientRole/*:id[@root='1.2.250.1.213.1.4.11']/@extension /string() , '^^^&amp;', '1.2.250.1.213.1.4.11' , '&amp;ISO^NH'), '') else 'ERREUR'";
		final String sSourcePatientID = getXpathSingleValue(file, sSourcePatientIDXpathReq);
		addExtrinsicObjectSlot("sourcePatientId", sSourcePatientID, doc, eElem2);
		final String sNomBR = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:name/*:family[@qualifier='BR']/string()");
		final String sNomCL = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:name/*:family[@qualifier='CL']/string()");
		String sPrenomCL = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:name/*:given[@qualifier='CL']/string()");
		String sPrenomBR = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:name/*:given[@qualifier='BR']/string()");
		final String sPrenom = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:name/*:given[1]/string()");
		if (sPrenomBR.startsWith("!!!{") || sPrenomBR.isEmpty()) {
			sPrenomBR = sPrenom;
		}
		if (sPrenomCL.startsWith("!!!{") || sPrenomCL.isEmpty()) {
			sPrenomCL = sPrenom;
		}
		// SourcePatientInfo
		final Element eeElem2 = doc.createElement("ns2:Slot");
		eeElem2.setAttribute("name", "sourcePatientInfo");
		eElem2.appendChild(eeElem2);
		final Element eeeElem2 = doc.createElement("ns2:ValueList");
		eeElem2.appendChild(eeeElem2);
		if (!sNomBR.startsWith("!!!{") && !sNomBR.isEmpty()) {
			final Element eeeElem3 = doc.createElement("ns2:Value");
			eeeElem2.appendChild(eeeElem3);
			eeeElem3.setTextContent("PID-5|" + sNomBR + "^" + sPrenomBR + "^^^^^" + "L");
		}
		if (!sNomCL.startsWith("!!!{") && !sNomCL.isEmpty()) {
			if (sPrenomCL.startsWith("!!!{") && !sPrenomCL.isEmpty()) {
				final Element eeeElem3 = doc.createElement("ns2:Value");
				eeeElem2.appendChild(eeeElem3);
				eeeElem3.setTextContent("PID-5|" + sNomCL + "^" + sPrenomBR + "^^^^^" + "D");
			} else {
				final Element eeeElem3 = doc.createElement("ns2:Value");
				eeeElem2.appendChild(eeeElem3);
				eeeElem3.setTextContent("PID-5|" + sNomCL + "^" + sPrenomCL + "^^^^^" + "D");
			}
		}
		final String sIPP2 = getXpathSingleValue(file,
				"//*:patientRole/*:id[not(@root='1.2.250.1.213.1.4.8') and not(@root='1.2.250.1.213.1.4.9')  and not(@root='1.2.250.1.213.1.4.10')  and not(@root='1.2.250.1.213.1.4.11')][2]/@extension/string()");
		final String sIPP2Root = getXpathSingleValue(file,
				"//*:patientRole/*:id[not(@root='1.2.250.1.213.1.4.8') and not(@root='1.2.250.1.213.1.4.9')  and not(@root='1.2.250.1.213.1.4.10')  and not(@root='1.2.250.1.213.1.4.11')][2]/@root/string()");
		if (!sIPP2.startsWith("!!!{") && !sIPP2.isEmpty()) {
			final Element eeeElem3 = doc.createElement("ns2:Value");
			eeeElem2.appendChild(eeeElem3);
			eeeElem3.setTextContent("PID-3|" + sIPP2 + "^^^&amp;" + sIPP2Root + "&amp;ISO^PI");
		}
		final String sDateNais = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:birthTime/@value/string()");
		if (!sDateNais.startsWith("!!!{") && !sDateNais.isEmpty()) {
			final Element eeeElem3 = doc.createElement("ns2:Value");
			eeeElem2.appendChild(eeeElem3);
			eeeElem3.setTextContent("PID-7|" + sDateNais);
		}
		final String sGenre = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:recordTarget/*:patientRole/*:patient/*:administrativeGenderCode/@code/string()");
		if (!sGenre.startsWith("!!!{") && !sGenre.isEmpty()) {
			final Element eeeElem3 = doc.createElement("ns2:Value");
			eeeElem2.appendChild(eeeElem3);
			eeeElem3.setTextContent("PID-8|" + sGenre);
		}
		// Name
		final String sName = getXpathSingleValue(file, "//*:ClinicalDocument/*:title/string()");
		final Element eeElem4 = doc.createElement("ns2:Name");
		eElem2.appendChild(eeElem4);
		final Element eeElem5 = doc.createElement("ns2:LocalizedString");
		eeElem4.appendChild(eeElem5);
		eeElem5.setAttribute("xml:lang", "FR");
		eeElem5.setAttribute("charset", "UTF8");
		eeElem5.setAttribute("value", sName);
		// Description
		final String sDesc = "";
		final Element eeElem6 = doc.createElement("ns2:Description");
		eElem2.appendChild(eeElem6);
		final Element eeElem7 = doc.createElement("ns2:LocalizedString");
		eeElem6.appendChild(eeElem7);
		eeElem7.setAttribute("xml:lang", "FR");
		eeElem7.setAttribute("charset", "UTF8");
		eeElem7.setAttribute("value", sDesc);
		// EventCodeList
		final String sNbrDocumentationOf = getXpathSingleValue(file, "count(//*:ClinicalDocument/*:documentationOf)");
		final long sNbrDocumentation = Long.parseUnsignedLong(sNbrDocumentationOf, 16);
		for (int i = 1; i <= sNbrDocumentation; i++) {
			final String sEventCodeNodeRepresentation = getXpathSingleValue(file,
					"//*:ClinicalDocument/*:documentationOf[" + i + "]/*:serviceEvent/*:code/@code/string()");
			final String sEventCodeDisplayName = getXpathSingleValue(file,
					"//*:ClinicalDocument/*:documentationOf[" + i + "]/*:serviceEvent/*:code/@displayName/string()");
			final String sEventCodeCodeSystem = getXpathSingleValue(file,
					"//*:ClinicalDocument/*:documentationOf[" + i + "]/*:serviceEvent/*:code/@codeSystem/string()");
			if (!sEventCodeNodeRepresentation.startsWith("!!!{")) {
				final Element eeElem8 = doc.createElement("ns2:Classification");
				eElem2.appendChild(eeElem8);
				eeElem8.setAttribute("classificationScheme", "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4");
				eeElem8.setAttribute("classifiedObject", documentEntryUUID[pDocumentNumber]);
				eeElem8.setAttribute("id", random());
				eeElem8.setAttribute("nodeRepresentation", sEventCodeNodeRepresentation);
				eeElem8.setAttribute("objectType",
						"urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification");
				addExtrinsicObjectSlot("codingScheme", sEventCodeCodeSystem, doc, eeElem8);
				final Element eeElem9 = doc.createElement("ns2:Name");
				eeElem8.appendChild(eeElem9);
				final Element eeElem10 = doc.createElement("ns2:LocalizedString");
				eeElem9.appendChild(eeElem10);
				eeElem10.setAttribute("xml:lang", "FR");
				eeElem10.setAttribute("charset", "UTF8");
				eeElem10.setAttribute("value", sEventCodeDisplayName);
			}
		}
		final long nbAuthors = Long
				.parseUnsignedLong(getXpathSingleValue(file, " count(//*:ClinicalDocument/*:author)"), 16);
		for (int iAuthor = 1; iAuthor <= nbAuthors; iAuthor++) {
			final Element eeElem11 = doc.createElement("ns2:Classification");
			eElem2.appendChild(eeElem11);
			eeElem11.setAttribute("id", random());
			eeElem11.setAttribute("classificationScheme", "urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d");
			eeElem11.setAttribute("classifiedObject", uuidAsStringId);
			eeElem11.setAttribute("nodeRepresentation", "");
			final String sAuthorInstitution = getAuthorInstitution(iAuthor, file);
			if (!sAuthorInstitution.isEmpty()) {
				final Element eeElem12 = doc.createElement("ns2:Slot");
				eeElem11.appendChild(eeElem12);
				eeElem12.setAttribute("name", "authorInstitution");
				final Element eeElem13 = doc.createElement("ns2:ValueList");
				eeElem12.appendChild(eeElem13);
				final Element eeElem14 = doc.createElement("ns2:Value");
				eeElem13.appendChild(eeElem14);
				eeElem14.setTextContent(getAuthorInstitution(iAuthor, file));
			}
			final Element eeElem12 = doc.createElement("ns2:Slot");
			eeElem11.appendChild(eeElem12);
			eeElem12.setAttribute("name", "authorPerson");
			final Element eeElem13 = doc.createElement("ns2:ValueList");
			eeElem12.appendChild(eeElem13);
			final Element eeElem14 = doc.createElement("ns2:Value");
			eeElem13.appendChild(eeElem14);
			eeElem14.setTextContent(getAuthorPerson(iAuthor, file));

			final String sAuthorRole = getXpathSingleValue(file,
					"//*:author[" + iAuthor + "]/*:functionCode/@displayName/string()");
			if (!sAuthorRole.startsWith("!!!{") && !sAuthorRole.isEmpty()) {
				final Element eeElem15 = doc.createElement("ns2:Slot");
				eeElem11.appendChild(eeElem15);
				eeElem15.setAttribute("name", "authorRole");
				final Element eeElem16 = doc.createElement("ns2:ValueList");
				eeElem15.appendChild(eeElem16);
				final Element eeElem17 = doc.createElement("ns2:Value");
				eeElem13.appendChild(eeElem17);
				eeElem17.setTextContent(sAuthorRole);
			}
			final String xPathString = "//*:ClinicalDocument/*:author[" + iAuthor + "]/*:assignedAuthor/*:code/@code";
			final String sAssignedAuthorExiste = getXpathSingleValue(file, xPathString);
			if (!sAssignedAuthorExiste.startsWith("!!!{") && !sAssignedAuthorExiste.isEmpty()) {
				final Element eeElem15 = doc.createElement("ns2:Slot");
				eeElem11.appendChild(eeElem15);
				eeElem15.setAttribute("name", "authorSpecialty");
				final Element eeElem16 = doc.createElement("ns2:ValueList");
				eeElem15.appendChild(eeElem16);
				final Element eeElem17 = doc.createElement("ns2:Value");
				eeElem16.appendChild(eeElem17);
				eeElem17.setTextContent(getAuthorSpecialty(iAuthor, file));
			}
		}
		final String sLoincCode = getXpathSingleValue(file, "//*:ClinicalDocument/*:code/@code/string()");
		final String home = System.getProperty("user.home");
		final File fileNos = new File(home + Constant.IMAGE9 + Constant.URLFILEJSON);
		final File fileNosTre = new File(home + Constant.IMAGE9 + Constant.URLFILEJSONTRE);
		List<File> xmlFile = null;
		File xFile = null;
		File aFile = null;
		File jFile = null;
		File jjFile = null;
		File tFile = null;
		File ttFile = null;
		try {
			final Path resourceDirectory = Paths.get("src", "main", "resources");
			final String absolutePath = resourceDirectory.toFile().getAbsolutePath() + "\\xml"
					+ "\\ASS_X04-CorrespondanceType-Classe-CISIS.xml";
			xFile = new File(absolutePath);
			final String absolutePath1 = resourceDirectory.toFile().getAbsolutePath() + "\\xml"
					+ "\\ASS_A11-CorresModeleCDA-XdsFormatCode-CISIS.xml";
			aFile = new File(absolutePath1);
			final String absolutePath2 = resourceDirectory.toFile().getAbsolutePath() + "\\xml" + "\\TRE_A04-Loinc.xml";
			tFile = new File(absolutePath2);
			xmlFile = XdmUtilities.downloadUsingNIO(Constant.URLNOS, fileNos.getAbsolutePath());
			final List<File> xmlFileCs = XdmUtilities.downloadUsingNIO(Constant.URLNOSCS, fileNosTre.getAbsolutePath());
			for (final File fileX : xmlFile) {
				if (fileX.getName().startsWith("JDV_J06_")) {
					jFile = fileX;
				}
				if (fileX.getName().startsWith("JDV_J10_")) {
					jjFile = fileX;
				}
			}
			for (final File fileX : xmlFileCs) {
				if (fileX.getName().startsWith("TRE_A05_")) {
					ttFile = fileX;
				}
			}
			final String retour = XdmUtilities.marshelling(xFile, sLoincCode);
			final List<String> retourJdv = XdmUtilities.marshellingJdv(jFile, retour);
			getXpathSingleValue(xFile,
					"//*:RetrieveValueSetResponse/*:ValueSet/*:MappedConceptList/*:MappedConcept/*:Concept[@code=" + "'"
							+ sLoincCode + "'" + "]");
			final String sTemplateID = recupereTemplateId(file);
			gTemplateID = sTemplateID;
			final Element eeElem11 = doc.createElement("ns2:Classification");
			eElem2.appendChild(eeElem11);
			eeElem11.setAttribute("id", random());
			eeElem11.setAttribute("classificationScheme", "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a");
			eeElem11.setAttribute("classifiedObject", uuidAsStringId);
			eeElem11.setAttribute("nodeRepresentation", retour);
			addExtrinsicObjectSlot("codingScheme", retourJdv.get(0), doc, eeElem11);
			// ns2:Name
			final Element sixElement = doc.createElement("ns2:Name");
			eeElem11.appendChild(sixElement);
			// ns2:LocalizedString
			final Element sevenElement = doc.createElement("ns2:LocalizedString");
			sevenElement.setAttribute("value", retourJdv.get(1));
			sixElement.appendChild(sevenElement);
			final String sConfidentialityDisplayName = getXpathSingleValue(file,
					"//*:ClinicalDocument/*:confidentialityCode/@displayName/string()");
			final String sConfidentialityCode = getXpathSingleValue(file,
					"//*:ClinicalDocument/*:confidentialityCode/@code/string()");
			final String sCodingScheme = getXpathSingleValue(file,
					"//*:ClinicalDocument/*:confidentialityCode/@codeSystem/string()");
			final Element eeEle11 = doc.createElement("ns2:Classification");
			eElem2.appendChild(eeEle11);
			eeEle11.setAttribute("id", random());
			eeEle11.setAttribute("classificationScheme", "urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f");
			eeEle11.setAttribute("classifiedObject", uuidAsStringId);
			eeEle11.setAttribute("nodeRepresentation", sConfidentialityCode);
			addExtrinsicObjectSlot("codingScheme", sCodingScheme, doc, eeEle11);
			// ns2:Name
			final Element sixxElement = doc.createElement("ns2:Name");
			eeEle11.appendChild(sixxElement);
			// ns2:LocalizedString
			final Element sevennElement = doc.createElement("ns2:LocalizedString");
			sevennElement.setAttribute("value", sConfidentialityDisplayName);
			sixxElement.appendChild(sevennElement);
			final String sNonXMLMediaType = getXpathSingleValue(file,
					"/*:ClinicalDocument/*:component/*:nonXMLBody/*:text/@mediaType/string()");
			String sFormatCode = XdmUtilities.getXmlns(aFile, gTemplateID);
			List<String> listJdv = XdmUtilities.marshellingJdv(jjFile, sFormatCode);
			String sFormatCodeDisplayName = listJdv.get(1);
			String sFormatCodeSystem = listJdv.get(0);
			switch (sNonXMLMediaType) {
			case "application/pdf":
				sFormatCode = "urn:ihe:iti:xds-sd:pdf:2008";
				sFormatCodeDisplayName = "Document à corps non structuré en Pdf/A-1";
				sFormatCodeSystem = "1.2.250.1.213.1.1.4.12";
				break;
			default:
				break;
			}
			final Element eeEle12 = doc.createElement("ns2:Classification");
			eElem2.appendChild(eeEle12);
			eeEle12.setAttribute("id", random());
			eeEle12.setAttribute("classificationScheme", "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d");
			eeEle12.setAttribute("classifiedObject", uuidAsStringId);
			eeEle12.setAttribute("nodeRepresentation", sFormatCode);
			addExtrinsicObjectSlot("codingScheme", sFormatCodeSystem, doc, eeEle12);
			// ns2:Name
			final Element sixxxElement = doc.createElement("ns2:Name");
			eeEle12.appendChild(sixxxElement);
			// ns2:LocalizedString
			final Element sevennnElement = doc.createElement("ns2:LocalizedString");
			sevennnElement.setAttribute("value", sFormatCodeDisplayName);
			sixxxElement.appendChild(sevennnElement);

			final String sHealthcareFacilityCode = getXpathSingleValue(file,
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@code/string()");
			final String sHealthcareFacilityCodesystem = getXpathSingleValue(file,
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@codeSystem/string()");
			final String sHealthcareFacilityDisplayName = getXpathSingleValue(file,
					"//*:componentOf/*:encompassingEncounter/*:location/*:healthCareFacility/*:code/@displayName/string()");
			final Element eeEle13 = doc.createElement("ns2:Classification");
			eElem2.appendChild(eeEle13);
			eeEle13.setAttribute("id", random());
			eeEle13.setAttribute("classificationScheme", "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1");
			eeEle13.setAttribute("classifiedObject", uuidAsStringId);
			eeEle13.setAttribute("nodeRepresentation", sHealthcareFacilityCode);
			addExtrinsicObjectSlot("codingScheme", sHealthcareFacilityCodesystem, doc, eeEle13);
			// ns2:Name
			final Element septElement = doc.createElement("ns2:Name");
			eeEle13.appendChild(septElement);
			// ns2:LocalizedString
			final Element septtElement = doc.createElement("ns2:LocalizedString");
			septtElement.setAttribute("value", sHealthcareFacilityDisplayName);
			septElement.appendChild(septtElement);
			final String sPracticeSettingCode = getXpathSingleValue(file, "//*:documentationOf[" + 1
					+ "]/*:serviceEvent/*:performer/*:assignedEntity/*:representedOrganization/*:standardIndustryClassCode/@code/string()");
			final String sPracticeSettingDisplayName = getXpathSingleValue(file, "//*:documentationOf[" + 1
					+ "]/*:serviceEvent/*:performer/*:assignedEntity/*:representedOrganization/*:standardIndustryClassCode/@displayName/string()");
			;
			final String sCodingSchem = getXpathSingleValue(file, "//*:documentationOf[" + 1
					+ "]/*:serviceEvent/*:performer/*:assignedEntity/*:representedOrganization/*:standardIndustryClassCode/@codeSystem/string()");
			final Element eeEle14 = doc.createElement("ns2:Classification");
			eElem2.appendChild(eeEle14);
			eeEle14.setAttribute("id", random());
			eeEle14.setAttribute("classificationScheme", "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead");
			eeEle14.setAttribute("classifiedObject", uuidAsStringId);
			eeEle14.setAttribute("nodeRepresentation", sPracticeSettingCode);
			addExtrinsicObjectSlot("codingScheme", sCodingSchem, doc, eeEle14);
			// ns2:Name
			final Element sepElement = doc.createElement("ns2:Name");
			eeEle14.appendChild(sepElement);
			// ns2:LocalizedString
			final Element septttElement = doc.createElement("ns2:LocalizedString");
			septttElement.setAttribute("xml:lang", "FR");
			septttElement.setAttribute("charset", "UTF8");
			septttElement.setAttribute("value", sPracticeSettingDisplayName);
			sepElement.appendChild(septttElement);
			List<String> list = XdmUtilities.marshellingTre(tFile, sLoincCode);
			String sTypeDisplayName = list.get(1);
			String sTypeCodeSystem = list.get(0);
			if (list.isEmpty()) {
				list = new ArrayList<>();
				list = XdmUtilities.marshellingJdv(ttFile, sLoincCode);
				sTypeDisplayName = list.get(1);
				sTypeCodeSystem = list.get(0);
			}
			final Element eeEle15 = doc.createElement("ns2:Classification");
			eElem2.appendChild(eeEle15);
			eeEle15.setAttribute("id", random());
			eeEle15.setAttribute("classificationScheme", "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983");
			eeEle15.setAttribute("classifiedObject", uuidAsStringId);
			eeEle15.setAttribute("nodeRepresentation", sLoincCode);
			addExtrinsicObjectSlot("codingScheme", sTypeCodeSystem, doc, eeEle15);
			// ns2:Name
			final Element sepElementt = doc.createElement("ns2:Name");
			eeEle15.appendChild(sepElementt);
			// ns2:LocalizedString
			final Element septtttElement = doc.createElement("ns2:LocalizedString");
			septtttElement.setAttribute("xml:lang", "FR");
			septtttElement.setAttribute("charset", "UTF8");
			septtttElement.setAttribute("value", sTypeDisplayName);
			sepElementt.appendChild(septtttElement);
			// ExternalIdentifier
			final Element eeEle16 = doc.createElement("ns2:ExternalIdentifier");
			eElem2.appendChild(eeEle16);
			eeEle16.setAttribute("id", random());
			eeEle16.setAttribute("identificationScheme", "urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427");
			eeEle16.setAttribute("registryObject", "urn:uuid:4a889fd4-012f-4000-e008-12c48372fe64");
			eeEle16.setAttribute("value", getPatientId(file));
			// ns2:Name
			final Element sepElementtt = doc.createElement("ns2:Name");
			eeEle16.appendChild(sepElementtt);
			// ns2:LocalizedString
			final Element septtttElementt = doc.createElement("ns2:LocalizedString");
			septtttElementt.setAttribute("value", "XDSDocumentEntry.patientId");
			sepElementtt.appendChild(septtttElementt);
			// ExternalIdentifier
			final String sExtension = getXpathSingleValue(file, "//*:ClinicalDocument/*:id/@extension/string()");
			String sUniqueId = "";
			if (sExtension.isEmpty()) {
				sUniqueId = getXpathSingleValue(file, "//*:ClinicalDocument/*:id/@root/string()");
			} else {
				sUniqueId = getXpathSingleValue(file, "//*:ClinicalDocument/*:id/@root/string()") + "^" + sExtension;
			}
			final Element eeEle17 = doc.createElement("ns2:ExternalIdentifier");
			eElem2.appendChild(eeEle17);
			eeEle17.setAttribute("id", random());
			eeEle17.setAttribute("identificationScheme", "urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab");
			eeEle17.setAttribute("registryObject", "b576aac4-33be-4875-b875-f8ee50bf66e0");
			eeEle17.setAttribute("value", sUniqueId);
			// ns2:Name
			final Element sepElementttt = doc.createElement("ns2:Name");
			eeEle17.appendChild(sepElementttt);
			// ns2:LocalizedString
			final Element septtttElementtt = doc.createElement("ns2:LocalizedString");
			septtttElementtt.setAttribute("xml:lang", "FR");
			septtttElementtt.setAttribute("charset", "UTF8");
			septtttElementtt.setAttribute("value", "XDSDocumentEntry.uniqueId");
			sepElementttt.appendChild(septtttElementtt);
		} catch (final Exception e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		} finally {
			jFile.delete();
			jjFile.delete();
			ttFile.delete();
		}
	}

	/**
	 * recupereTemplateId
	 * 
	 * @param file
	 * @return
	 */
	private static String recupereTemplateId(final File file) {
		final String sTemplateID = getXpathSingleValue(file,
				"//*:ClinicalDocument/*:templateId[last()]/@root/string()");
		return sTemplateID;
	}

	/**
	 * getLegalAuthenticator
	 * 
	 * @param SaxonWrapperDocument
	 * @return
	 */
	private static String getLegalAuthenticator(final File file) {
		final String sC1Id = getXpathSingleValue(file,
				"//*:legalAuthenticator/*:assignedEntity/*:id/@extension/string()");
		final String sC2Nom = getXpathSingleValue(file,
				"//*:legalAuthenticator/*:assignedEntity/*:assignedPerson/*:name/*:family/string()");
		final String sC3Prenom = getXpathSingleValue(file,
				"//*:legalAuthenticator/*:assignedEntity/*:assignedPerson/*:name/*:given/string()");
		final String sC9AssigningAut = getXpathSingleValue(file,
				"//*:legalAuthenticator/*:assignedEntity/*:id/@root//string()");
		final String sC10TypeNom = "D";
		final String sC13TypeId = "IDNPS";
		String valRet = sC1Id + "^" + sC2Nom + "^" + sC3Prenom + "^^^^^^&amp;" + sC9AssigningAut + "&amp;ISO^"
				+ sC10TypeNom + "^^^" + sC13TypeId;

		return valRet;

	}

	/**
	 * addExtrinsicObjectSlot
	 * 
	 * @param pNomMetadonnee
	 * @param pValeurMetadonnee
	 */
	private static void addExtrinsicObjectSlot(final String pNomMetadonnee, final String pValeurMetadonnee,
			final Document doc, final Element elem) {
		final Element eeElem3 = doc.createElement("ns2:Slot");
		eeElem3.setAttribute("name", pNomMetadonnee);
		elem.appendChild(eeElem3);
		final Element eeeElem2 = doc.createElement("ns2:ValueList");
		eeElem3.appendChild(eeeElem2);
		final Element eeeElem3 = doc.createElement("ns2:Value");
		eeeElem2.appendChild(eeeElem3);
		eeeElem3.setTextContent(String.valueOf(pValeurMetadonnee));

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
	 * getAuthorInstitution
	 * 
	 * @param iAuthor
	 * @param cda
	 * @return
	 */
	private static String getAuthorInstitution(final Integer iAuthor, final File cda) {
		final String sRoot = getXpathSingleValue(cda, "//*:ClinicalDocument/*:author[" + iAuthor.toString()
				+ "]/*:assignedAuthor/*:representedOrganization/string()");
		if (sRoot.startsWith("!!!")) {
			return "";
		}
		String sC1Nom = getXpathSingleValue(cda, "//*:ClinicalDocument/*:author[" + iAuthor.toString()
				+ "]/*:assignedAuthor/*:representedOrganization/*:name/string()");
		if (sC1Nom.startsWith("!!!"))
			sC1Nom = "";
		String sC6AssigningAuth = getXpathSingleValue(cda, "//*:ClinicalDocument/*:author[" + iAuthor.toString()
				+ "]/*:assignedAuthor/*:representedOrganization/*:id/@root/string()");
		if (sC6AssigningAuth.startsWith("!!!"))
			sC6AssigningAuth = "";
		final String sC7TypeId = "IDNST";
		String sC10TypeId = getXpathSingleValue(cda, "//*:ClinicalDocument/*:author[" + iAuthor.toString()
				+ "]/*:assignedAuthor/*:representedOrganization/*:id/@extension/string()");
		if (sC10TypeId.startsWith("!!!"))
			sC10TypeId = "";
		final String valAuthorInstitution = sC1Nom + "^^^^^&amp;" + sC6AssigningAuth + "&amp;ISO^" + sC7TypeId + "^^^"
				+ sC10TypeId;
		return valAuthorInstitution;
	}

	/**
	 * 
	 * @param SaxonWrapperDocument
	 * @return
	 */
	private static String getAuthorPerson(final Integer iAuthor, final File cda) {
		final String sC1Id = getXpathSingleValue(cda,
				"//*:author[" + iAuthor.toString() + "]/*:assignedAuthor/*:id/@extension/string()");
		final String sPersonneExiste = getXpathSingleValue(cda,
				"//*:author[" + iAuthor.toString() + "]/*:assignedAuthor/*:assignedPerson/string()");
		String sC2Nom = "";
		String sC3Prenom = "";
		String sC10TypeNom = "";
		String sC13TypeId = "";
		if (!sPersonneExiste.startsWith("!!!{")) {
			sC2Nom = getXpathSingleValue(cda, "//*:author[" + iAuthor.toString()
					+ "]/*:assignedAuthor/*:assignedPerson/*:name/*:family/string()");
			sC3Prenom = getXpathSingleValue(cda,
					"//*:author[" + iAuthor.toString() + "]/*:assignedAuthor/*:assignedPerson/*:name/*:given/string()");
			sC10TypeNom = "D";
			if (sC1Id.contains("/")) {
				sC13TypeId = "EI";
			} else {
				sC13TypeId = "IDNPS";
			}
		} else { // Pas de personne trouvée. Il s'agit d'un dispositif
			sC2Nom = getXpathSingleValue(cda, "//*:author[" + iAuthor.toString()
					+ "]/*:assignedAuthor/*:assignedAuthoringDevice/*:softwareName/string()");
			sC3Prenom = getXpathSingleValue(cda, "//*:author[" + iAuthor.toString()
					+ "]/*:assignedAuthor/*:assignedAuthoringDevice/*:manufacturerModelName/string()");
			sC10TypeNom = "U";
			sC13TypeId = "RI";
		}
		final String sC9assigningAut = getXpathSingleValue(cda,
				"//*:author[" + iAuthor.toString() + "]/*:assignedAuthor/*:id/@root/string()");
		final String valAuthorPerson = sC1Id + "^" + sC2Nom + "^" + sC3Prenom + "^^^^^^&amp;" + sC9assigningAut
				+ "&amp;ISO^" + sC10TypeNom + "^^^" + sC13TypeId;
		return valAuthorPerson;
	}

	/**
	 * getAuthorSpecialty
	 * 
	 * @param iAuthor
	 * @param cda
	 * @return
	 */
	private static String getAuthorSpecialty(final Integer iAuthor, final File cda) {
		final String sAssignedAuthorExiste = getXpathSingleValue(cda,
				"//*:author[" + iAuthor.toString() + "]/*:assignedAuthor/string()");
		if (sAssignedAuthorExiste.startsWith("!!!{")) {
			return "";
		}
		final String sC1Id = getXpathSingleValue(cda,
				"//*:ClinicalDocument/*:author[" + iAuthor.toString() + "]/*:assignedAuthor/*:code/@code/string()");
		final String sC2Intitule = getXpathSingleValue(cda, "//*:ClinicalDocument/*:author[" + iAuthor.toString()
				+ "]/*:assignedAuthor/*:code/@displayName/string()");
		final String sC3CodeSystem = getXpathSingleValue(cda, "//*:ClinicalDocument/*:author[" + iAuthor.toString()
				+ "]/*:assignedAuthor/*:code/@codeSystem/string()");
		final String valAuthorSpecialty = sC1Id + "^" + sC2Intitule + "^" + sC3CodeSystem;
		return valAuthorSpecialty;
	}

	/**
	 * getPatientId
	 * 
	 * @return
	 */
	private static String getPatientId(final File cda) {
		final String[][] toId = { { "1.2.250.1.213.1.4.8", "NH" }, { "1.2.250.1.213.1.4.9", "NH" },
				{ "1.2.250.1.213.1.4.10", "NH" }, { "1.2.250.1.213.1.4.11", "NH" } };
		String sOIDReconnu = "";
		String sTypeINSReconnu = "";
		String sIdentifiant = "";
		for (int i = 0; i < toId.length; i++) {
			final String sQueryResult = getXpathSingleValue(cda,
					"//*:recordTarget/*:patientRole/*:id[@root='" + toId[i][0] + "']/@extension/string()");
			if (!sQueryResult.startsWith("!!!{") && !sQueryResult.isEmpty()) {
				sIdentifiant = sQueryResult;
				sOIDReconnu = toId[i][0];
				sTypeINSReconnu = toId[i][1];
				final String valRet = sIdentifiant + "^^^&amp;" + sOIDReconnu + "&amp;ISO^" + sTypeINSReconnu;
				return valRet;
			}
		}
		final String sC1IdLocal = getXpathSingleValue(cda,
				"//*:recordTarget/*:patientRole/*:id[1]/@extension/string()");
		final String sC4AssigningAut = getXpathSingleValue(cda, "//*:recordTarget/*:patientRole/*:id/@root/string()");
		final String sC5TypeId = "PI";
		if (!sC1IdLocal.startsWith("!!!{") && !sC1IdLocal.isEmpty()) {
			final String valRet = sC1IdLocal + "^^^&amp;" + sC4AssigningAut + "&amp;ISO^" + sC5TypeId;
			return valRet;
		} else {
			return "!!!{getPatientId():Pas d'INS-C ni de NIR trouvé}";
		}
	}
}