/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.hispindia;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mchange.io.FileUtils;

@Controller
@RequestMapping("/hisp/rest/addresslist")
public class Address {
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String getAddress(HttpServletResponse response, HttpServletRequest request,
	        @RequestParam(value = "address_name") String filename)
	        throws ResponseException, JsonGenerationException, JsonMappingException, IOException, ParseException {
		
		File file = new File(OpenmrsUtil.getApplicationDataDirectory() + "/address_files/" + filename + ".json");
		response.setContentType("application/json");
		return FileUtils.getContentsAsString(file);
		
	}
}
