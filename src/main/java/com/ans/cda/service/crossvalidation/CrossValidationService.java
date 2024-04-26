package com.ans.cda.service.crossvalidation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import org.apache.log4j.Logger;

public class CrossValidationService {
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(CrossValidationService.class);

	/**
	 * crossValidate
	 * 
	 * @param textfield
	 * @param listeTypeValidation
	 */
	public static String crossValidate(final File textfield, final File textfieldMeta, final String validationUrl) {
		String console = "";
		if (Files.exists(Paths.get(textfield.getAbsolutePath()))) {
			try {
				final Instant startTime = Instant.now();
				// Récupérer le nom du validateur META
				final String content = new String(Files.readAllBytes(Paths.get(textfield.getAbsolutePath())));
				final String contentMeta = new String(Files.readAllBytes(Paths.get(textfieldMeta.getAbsolutePath())));
				// call valideDOCUMENT function
				String validationResult = SaxonCrossValidator.crossValidateDocument(content, contentMeta,
						validationUrl);
				while (validationResult == null) {
					validationResult = SaxonCrossValidator.crossValidateDocument(content, contentMeta, validationUrl);
				}
				final Instant endTime = Instant.now();
				System.out.println(Instant.now() + ": Validation result: " + validationResult);
				System.out.println(Duration.between(startTime, endTime).getSeconds() + " seconds.");
				System.out.println("Validation completed.");
				console = Instant.now() + ": Validation result: " + validationResult + "\n"
						+ Duration.between(startTime, endTime).getSeconds() + " seconds." + "\n"
						+ "Validation completed.";
			} catch (final IOException e) {
				if (LOG.isInfoEnabled()) {
					final String error = e.getMessage();
					LOG.error(error);
				}
			}
		} else {
			console = "You must load a valid metadata file.";
		}
		return console;

	}

}
