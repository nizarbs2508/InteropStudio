package com.ans.cda.service.validation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import org.apache.log4j.Logger;

/**
 * ValidationService
 * 
 * @author bensa
 */
public class ValidationService {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(ValidationService.class);

	/**
	 * valideMetaClick
	 * 
	 * @param textfield
	 * @param listeTypeValidation
	 */
	public static String validateMeta(final File textfield, final String sValidationSName, final String sValidatorName,
			final String validationUrl) {
		String display = "";
		if (Files.exists(Paths.get(textfield.getAbsolutePath()))) {
			try {
				final Instant startTime = Instant.now();
				// Récupérer le nom du validateur META
				final String content = new String(Files.readAllBytes(Paths.get(textfield.getAbsolutePath())));
				// call valideDOCUMENT function
				String validationResult = SaxonValidator.valideDocument(content, sValidationSName, sValidatorName,
						validationUrl);
				while (validationResult == null) {
					validationResult = SaxonValidator.valideDocument(content, sValidationSName, sValidatorName,
							validationUrl);
				}
				final Instant endTime = Instant.now();
				System.out.println(Instant.now() + ": Validation result: " + validationResult);
				System.out.println(Duration.between(startTime, endTime).getSeconds() + " seconds.");
				System.out.println("Validation completed.");
				display = Instant.now() + ": Validation result: " + validationResult + "\n"
						+ Duration.between(startTime, endTime).getSeconds() + " seconds." + "\n"
						+ "Validation completed.";
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		} else {
			System.out.println("You must load a valid metadata file.");
			display = "You must load a valid metadata file.";
		}
		return display;
	}
}