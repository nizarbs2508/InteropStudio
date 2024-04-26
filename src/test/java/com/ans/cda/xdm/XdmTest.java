package com.ans.cda.xdm;

import java.util.ArrayList;
import java.util.List;

import com.ans.cda.service.xdm.XdmService;

/**
 * XdmTest
 * 
 * @author bensa
 */
public class XdmTest {
	/**
	 * testGenerateMeta
	 */
	@org.junit.jupiter.api.Test
	void testGenerateMeta() {
		List<String> pList = new ArrayList<>();
		pList.add(
				"C:\\Users\\bensa\\Downloads\\TestContenuCDA-3-0-main(8)\\TestContenuCDA-3-0-main\\ExemplesCDA\\AVC-AUNV_2022.01.xml");
		XdmService.generateMeta(pList);
	}
}
