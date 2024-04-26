package com.ans.cda.validation;

import java.io.File;

import com.ans.cda.constant.Constant;
import com.ans.cda.service.validation.ValidationService;

/**
 * ValidationTest
 * 
 * @author bensa
 */
public class ValidationTest {
	/**
	 * testValidateMeta
	 */
	@org.junit.jupiter.api.Test
	void testValidateMeta() {
		ValidationService.validateMeta(new File(Constant.FILEPATH), Constant.MODEL, Constant.ASIPXDM, Constant.URLVALIDATION);
	}
}
