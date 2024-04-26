package com.ans.cda.xdm;

import java.util.ArrayList;
import java.util.List;

import com.ans.cda.service.xdm.IheXdmService;

public class IheXdmTest {

	/**
	 * testGenerateMeta
	 */
	@org.junit.jupiter.api.Test
	void generateIheXdmZip() {
		List<String> pList = new ArrayList<>();
		pList.add(
				"C:\\Users\\bensa\\Downloads\\TestContenuCDA-3-0-main(8)\\TestContenuCDA-3-0-main\\ExemplesCDA\\AVC-AUNV_2022.01.xml");
		IheXdmService.generateIheXdmZip(pList, "C:\\Users\\bensa");
	}

}
