package com.ans.cda.crossvalidation;

import java.io.File;

import com.ans.cda.constant.Constant;
import com.ans.cda.service.crossvalidation.CrossValidationService;

/**
 * CrossValidationTest
 * 
 * @author bensa
 */
public class CrossValidationTest {
	/**
	 * testCrossValidate
	 */
	@org.junit.jupiter.api.Test
	void testCrossValidate() {
		CrossValidationService.crossValidate(new File(Constant.FILEPATHCDA), new File(Constant.FILEPATH),
				Constant.URLVALIDATION);
	}
}
