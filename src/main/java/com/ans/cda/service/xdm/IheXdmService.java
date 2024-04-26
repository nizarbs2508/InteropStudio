package com.ans.cda.service.xdm;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.Logger;

import com.ans.cda.utilities.xdm.IheXdmUtilities;

/**
 * IheXdmService
 * 
 * @author bensa
 */
public class IheXdmService {
	/**
	 * FILENAME
	 */
	private static final String FILENAME = System.getProperty("user.home");
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(IheXdmService.class);

	/**
	 * generateIheXdmZip
	 * 
	 * @param listCda
	 * @param savePath
	 */
	public static void generateIheXdmZip(final List<String> listCda, final String savePath) {
		final File file = new File(savePath + new File(listCda.get(0)).getName());
		if (file.exists()) {
			IheXdmUtilities.deleteDirectory(file);
		}
		final Path path = IheXdmUtilities
				.cretaeFolder(savePath + "\\" + IheXdmUtilities.removeExtension(new File(listCda.get(0)).getName()));
		final Path contenuZip = IheXdmUtilities.cretaeFolder(path.toFile() + "\\Contenu du ZIP");
		final Path iheXdmFolder = IheXdmUtilities.cretaeFolder(contenuZip.toFile() + "\\IHE_XDM");
		final Path subset01Folder = IheXdmUtilities.cretaeFolder(iheXdmFolder.toFile() + "\\SUBSET01");
		IheXdmUtilities.copyFile(new File(listCda.get(0)), subset01Folder, "\\DOC0001.XML");
		XdmService.generateMeta(listCda);
		final Path pathMeta = Paths.get(FILENAME + "\\" + "nouveauDoc.xml");
		IheXdmUtilities.copyFile(pathMeta.toFile(), subset01Folder, "\\METADATA.XML");
		final String sSuffix = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
				"//*:ClinicalDocument/*:author/*:assignedAuthor/*:assignedPerson/*:name/*:suffix/string()");
		final String sNomAuteur = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
				"//*:ClinicalDocument/*:author/*:assignedAuthor/*:assignedPerson/*:name/*:given/string()");
		final String sPrenomAuteur = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
				"//*:ClinicalDocument/*:author/*:assignedAuthor/*:assignedPerson/*:name/*:family/string()");
		final String sOrganisme = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
				"//*:ClinicalDocument/*:author/*:assignedAuthor/*:representedOrganization/*:name/string()");
		final String sOrganismeFiness = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
				"//*:ClinicalDocument/*:author/*:assignedAuthor/*:representedOrganization/*:id/@extension/string()");
		final String sAdresseAuteur = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
				"//*:ClinicalDocument/*:author/*:assignedAuthor/*:addr/string-join((*:houseNumber,*:streetNameType,*:streetName,*:postalCode,*:city),' ')");
		final String sTelephoneAuteur = IheXdmUtilities.getXpathSingleValue(new File(listCda.get(0)),
				"//*:ClinicalDocument/*:author/*:assignedAuthor/*:telecom/@value/string()");
		createReadmeFile(contenuZip.toFile().getAbsolutePath() + "\\README.TXT", sSuffix, sNomAuteur, sPrenomAuteur,
				sOrganisme, sOrganismeFiness, sAdresseAuteur, sTelephoneAuteur);
		createHtmlFile(contenuZip.toFile().getAbsolutePath() + "\\INDEX.HTM", sOrganisme, sOrganismeFiness);
		IheXdmUtilities.compressFolder(path, contenuZip);
		final String tmpdir = System.getProperty("java.io.tmpdir");
		final File tempFile = new File(tmpdir + "\\" + "7z.exe");
		tempFile.delete();
	}

	/**
	 * createReadmeFile
	 * 
	 * @param filePath
	 * @param sSuffix
	 * @param sNomAuteur
	 * @param sPrenomAuteur
	 * @param sOrganisme
	 * @param sOrganismeFiness
	 * @param sAdresseAuteur
	 * @param sTelephoneAuteur
	 */
	public static void createReadmeFile(final String filePath, final String sSuffix, final String sNomAuteur,
			final String sPrenomAuteur, final String sOrganisme, final String sOrganismeFiness,
			final String sAdresseAuteur, final String sTelephoneAuteur) {
		IheXdmUtilities.createFile(filePath);
		IheXdmUtilities.writeToReadmeFile(filePath, sSuffix, sNomAuteur, sPrenomAuteur, sOrganisme, sOrganismeFiness,
				sAdresseAuteur, sTelephoneAuteur);
	}

	/**
	 * createHtmlFile
	 * 
	 * @param filePath
	 * @param sOrganisme
	 * @param sOrganismeFiness
	 */
	public static void createHtmlFile(final String filePath, final String sOrganisme, final String sOrganismeFiness) {
		IheXdmUtilities.createFile(filePath);
		IheXdmUtilities.writeToHtmFile(filePath, sOrganisme, sOrganismeFiness);
	}

}
