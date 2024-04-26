package com.ans.cda.service.bom;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * BomService
 * 
 * @author bensa
 */
public class BomService {

	/**
	 * saveAsUTF8WithoutBOM
	 * 
	 * @param fileName
	 * @param encoding
	 * @throws IOException
	 */
	public static void saveAsUTF8WithoutBOM(final String fileName, Charset encoding) throws IOException {
		if (fileName == null) {
			throw new IllegalArgumentException("fileName");
		}
		if (encoding == null) {
			encoding = StandardCharsets.UTF_8;
		}
		final String content = new String(Files.readAllBytes(Paths.get(fileName)), encoding);
		final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, StandardCharsets.UTF_8));
		writer.write(content);
		writer.close();
	}

}
