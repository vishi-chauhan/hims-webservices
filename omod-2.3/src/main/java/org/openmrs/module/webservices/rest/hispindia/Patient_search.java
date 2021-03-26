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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/hisp/rest/patient_search")
public class Patient_search extends BaseRestController {
	
	private List<Visit> patvisits = new ArrayList<Visit>();
	
	@RequestMapping(method = RequestMethod.GET)
	public void getPatient(HttpServletResponse response, HttpServletRequest request,
	        @RequestParam(value = "name") String name, @RequestParam(required = false, value = "age") String age,
	        @RequestParam(required = false, value = "agerange") Integer agerange,
	        @RequestParam(required = false, value = "lastvisitexact") String lastvisitexact,
	        @RequestParam(required = false, value = "lastvisitapprox") String lastvisitapprox)
	        throws ResponseException, JsonGenerationException, JsonMappingException, IOException, ParseException {
		
		List<Patient> Patientlist = new ArrayList<Patient>();
		List<patient_details> Patientdetais = new ArrayList<patient_details>();
		Date exvdate = null;
		Date appvdate = null;
		Visit pat_last_visit = null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		
		Patientlist = Context.getPatientService().getPatients(name);
		
		if (StringUtils.isNotBlank(lastvisitexact) && !Patientlist.isEmpty()) {
			exvdate = new SimpleDateFormat("dd-MM-yyyy").parse(lastvisitexact);
			patvisits = Context.getVisitService().getVisits(null, Patientlist, null, null,
			    new DateTime(exvdate).minusDays(1).toDate(), new DateTime(exvdate).plusDays(1).toDate(), null, null,
			    null, true, false);
		}
		if (StringUtils.isNotBlank(lastvisitapprox) && StringUtils.isBlank(lastvisitexact) && !Patientlist.isEmpty()) {
			appvdate = new SimpleDateFormat("dd-MM-yyyy").parse(lastvisitapprox);
			patvisits = Context.getVisitService().getVisits(null, Patientlist, null, null, appvdate, null, null, null,
			    null, true, false);
		}
		
		for (Patient pat : Patientlist) {
			pat_last_visit = null;
			if (StringUtils.isNotBlank((CharSequence) age)) {
				if (pat.getAge() < Integer.parseInt(age) - agerange) {
					continue;
				}
				if (pat.getAge() > Integer.parseInt(age) + agerange) {
					continue;
				}
			}
			pat_last_visit = this.get_last_visit(pat);
			if (StringUtils.isNotBlank((CharSequence) lastvisitexact) && pat_last_visit == null) {
				continue;
			}
			
			patient_details pat_det = new patient_details();
			pat_det.setUUID(pat.getUuid());
			pat_det.setGender(pat.getGender());
			pat_det.setAge(pat.getAge().toString());
			
			String identifierf = pat.getIdentifiers().toString();
			if (StringUtils.isNotBlank(identifierf)) {
				identifierf = identifierf.replace("[", "").replace("]", "");
			}
			pat_det.setIdentifier(identifierf);
			
			String namef = pat.getNames().toString();
			if (StringUtils.isNotBlank(namef)) {
				namef = namef.replace("[", "").replace("]", "");
			}
			pat_det.setName(namef);
			
			Map<String, String> pataddress = new HashMap<String, String>();
			
			for (PersonAddress temp : pat.getAddresses()) {
				
				pataddress.put("Address1", temp.getAddress1());
				pataddress.put("Address2", temp.getAddress2());
				pataddress.put("City Village", temp.getCityVillage());
				pataddress.put("State Province", temp.getStateProvince());
				pataddress.put("Country", temp.getCountry());
				pataddress.put("Postal Code", temp.getPostalCode());
			}
			pat_det.setAddress(pataddress);
			
			if (pat_last_visit != null) {
				pat_det.setVisit_date(formatter.format(pat_last_visit.getStartDatetime()));
				pat_det.setVisit_UUID(pat_last_visit.getUuid());
			}
			
			Map<String, String> patattribs = new HashMap<String, String>();
			
			for (Map.Entry<String, PersonAttribute> entry : pat.getAttributeMap().entrySet()) {
				
				PersonAttribute patatr = entry.getValue();
				String spatatr = patatr.getValue();
				String key = entry.getKey();
				patattribs.put(key, spatatr);
				
			}
			
			pat_det.setPerson_attributes(patattribs);
			Patientdetais.add(pat_det);
			
		}
		response.setContentType("application/json");
		ServletOutputStream out = response.getOutputStream();
		
		new ObjectMapper().writeValue(out, Patientdetais);
	}
	
	private Visit get_last_visit(Patient patient) {
		
		Visit vis = null;
		for (Visit v : patvisits) {
			if (v.getPatient().getUuid().equals(patient.getUuid())) {
				return v;
			}
			
		}
		
		return vis;
		
	}
}

class patient_details {
	
	private String UUID;
	
	private String name;
	
	private String age;
	
	private String gender;
	
	private String visit_date;
	
	private String visit_UUID;
	
	private Map<String, String> person_attributes;
	
	private String Identifier;
	
	private Map<String, String> Address;
	
	public String getUUID() {
		return UUID;
	}
	
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	
	public String getVisit_date() {
		return visit_date;
	}
	
	public void setVisit_date(String visit_date) {
		this.visit_date = visit_date;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAge() {
		return age;
	}
	
	public void setAge(String age) {
		this.age = age;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getVisit_UUID() {
		return visit_UUID;
	}
	
	public void setVisit_UUID(String visit_UUID) {
		this.visit_UUID = visit_UUID;
	}
	
	public Map<String, String> getPerson_attributes() {
		return person_attributes;
	}
	
	public void setPerson_attributes(Map<String, String> person_attributes) {
		this.person_attributes = person_attributes;
	}
	
	public String getIdentifier() {
		return Identifier;
	}
	
	public void setIdentifier(String identifier) {
		Identifier = identifier;
	}
	
	public Map<String, String> getAddress() {
		return Address;
	}
	
	public void setAddress(Map<String, String> address) {
		Address = address;
	}
	
}
