/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("module/webservices/rest/symptoms.list")
public class CDSSEY {
	
	DatabaseConnector databaseConnector = new DatabaseConnector();
	
	DataSource dataSource = databaseConnector.dataSource();
	
	@RequestMapping(method = RequestMethod.POST)
	public void getApi(HttpServletResponse response, HttpServletRequest request, @RequestParam(value = "q") String q)
	        throws Exception {
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String[] symptoms = q.split(",");
		String uuids = "";
		for (int i = 0; i < symptoms.length; i++) {
			uuids += "'" + symptoms[i] + "'";
			if (i < symptoms.length - 1) {
				uuids += ", ";
			}
		}
		
		String sql = "select a.diagC,a.diagN, a.diagUuid, a.symC, a.symptom, a.symUuid,b.investC,b.investN,b.investUuid,b.drugC,b.drugN,b.drugUuid from ( select cs.concept_set 'diagC',cn.name 'diagN',c1.uuid 'diagUuid',cs.concept_id 'symC',cn1.name 'symptom', c.uuid 'symUuid'from concept_set cs inner join concept c on c.concept_id=cs.concept_id and c.class_id in (12,13) inner join concept c1 on c1.concept_id=cs.concept_set and c1.class_id in (4) inner join concept_name cn on cn. concept_id=cs.concept_set and cn.locale='en' and cn.concept_name_type like 'FULLY_SPECIFIED' inner join concept_name cn1 on cn1.concept_id=cs.concept_id and cn1.locale='en' and cn1.concept_name_type like 'FULLY_SPECIFIED' where c.uuid in ("
		        + uuids
		        + ")) a left join ( select c.concept_id 'diagC', ca.answer_concept 'investC', cn1.name 'investN', c1.uuid 'investUuid', ca1.answer_concept 'drugC', cn2.name 'drugN',c2.uuid 'drugUuid' from concept c left join concept_answer ca on ca.concept_id=c.concept_id left join concept_answer ca1 on ca1.concept_id=ca.answer_concept inner join concept c1 on c1.concept_id=ca.answer_concept inner join concept c2 on c2.concept_id=ca1.answer_concept inner join concept_name cn1 on cn1.concept_id=ca.answer_concept and cn1.locale='en' and cn1.concept_name_type like 'FULLY_SPECIFIED' inner join concept_name cn2 on cn2.concept_id=ca1.answer_concept and cn2.locale='en' and cn2.concept_name_type like 'FULLY_SPECIFIED' )b on a.diagC=b.diagC";
		
		List<Map<String, Object>> symptomslList = jdbcTemplate.queryForList(sql);
		
		String symptomUuid = "";
		String diagnosisUuid = "";
		String symptomName = "";
		String diagnosisName = "";
		
		List<Symptom> symptomList = new ArrayList<Symptom>();
		List<Diagnosis> diagnosisList = new ArrayList<Diagnosis>();
		List<Odject> investigationList = new ArrayList<Odject>();
		List<Odject> drugList = new ArrayList<Odject>();
		
		Symptom symptom = new Symptom();
		Diagnosis diagnosis = new Diagnosis();
		
		if (symptomslList.size() > 0) {
			symptomUuid = symptomslList.get(0).get("symUuid").toString();
			symptomName = symptomslList.get(0).get("symptom").toString();
			diagnosisUuid = symptomslList.get(0).get("diagUuid").toString();
			diagnosisName = symptomslList.get(0).get("diagN").toString();
			
			for (int i = 0; i < symptomslList.size(); i++) {
				if (symptomUuid.equals(symptomslList.get(i).get("symUuid").toString())) {
					if (diagnosisUuid.equals(symptomslList.get(i).get("diagUuid").toString())) {
						Odject odject = new Odject();
						odject.setName(symptomslList.get(i).get("drugN").toString());
						odject.setUuid(symptomslList.get(i).get("drugUuid").toString());
						
						if (symptomslList.get(i).get("investN").toString().contains("INVESTIGATION")) {
							investigationList.add(odject);
						} else {
							drugList.add(odject);
						}
						
					} else {
						diagnosis.setUuid(diagnosisUuid);
						diagnosis.setName(diagnosisName);
						diagnosis.setInvestigations(investigationList);
						diagnosis.setDrugs(drugList);
						diagnosisList.add(diagnosis);
						
						diagnosisUuid = symptomslList.get(i).get("diagUuid").toString();
						diagnosisName = symptomslList.get(i).get("diagN").toString();
						
						diagnosis = new Diagnosis();
						investigationList = new ArrayList<Odject>();
						drugList = new ArrayList<Odject>();
						
						i -= 1;
					}
					
				} else {
					diagnosis.setUuid(diagnosisUuid);
					diagnosis.setName(diagnosisName);
					diagnosis.setInvestigations(investigationList);
					diagnosis.setDrugs(drugList);
					diagnosisList.add(diagnosis);
					
					symptom.setUuid(symptomUuid);
					symptom.setName(symptomName);
					symptom.setDiagnosis(diagnosisList);
					symptomList.add(symptom);
					
					symptomUuid = symptomslList.get(i).get("symUuid").toString();
					symptomName = symptomslList.get(i).get("symptom").toString();
					diagnosisUuid = symptomslList.get(i).get("diagUuid").toString();
					diagnosisName = symptomslList.get(i).get("diagN").toString();
					
					diagnosisList = new ArrayList<Diagnosis>();
					investigationList = new ArrayList<Odject>();
					drugList = new ArrayList<Odject>();
					diagnosis = new Diagnosis();
					symptom = new Symptom();
					
					i -= 1;
				}
			}
			
			diagnosis.setUuid(diagnosisUuid);
			diagnosis.setName(diagnosisName);
			diagnosis.setInvestigations(investigationList);
			diagnosis.setDrugs(drugList);
			diagnosisList.add(diagnosis);
			
			symptom.setUuid(symptomUuid);
			symptom.setName(symptomName);
			symptom.setDiagnosis(diagnosisList);
			symptomList.add(symptom);
		}
		
		String requestOrigin = request.getHeader("Origin");
		
		response.addHeader("Access-Control-Allow-Origin", requestOrigin);
		response.addHeader("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS, DELETE");
		response.setHeader("Access-Control-Allow-Headers", "authorization, content-type");
		response.setContentType("application/json");
		
		if (request.getMethod().equals("OPTIONS")) {
			response.setStatus(HttpServletResponse.SC_OK);
		}
		
		ServletOutputStream out = response.getOutputStream();
		
		new ObjectMapper().writeValue(out, symptomList);
	}
	
}

class Symptom {
	
	String name;
	
	String uuid;
	
	List<Diagnosis> diagnosis;
	
	public String getName() {
		return name;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public List<Diagnosis> getDiagnosis() {
		return diagnosis;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public void setDiagnosis(List<Diagnosis> diagnosis) {
		this.diagnosis = diagnosis;
	}
}

class Diagnosis {
	
	String name;
	
	String uuid;
	
	List<Odject> investigations;
	
	List<Odject> drugs;
	
	public String getName() {
		return name;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public List<Odject> getInvestigations() {
		return investigations;
	}
	
	public List<Odject> getDrugs() {
		return drugs;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public void setInvestigations(List<Odject> investigations) {
		this.investigations = investigations;
	}
	
	public void setDrugs(List<Odject> drugs) {
		this.drugs = drugs;
	}
}

class Odject {
	
	String name;
	
	String uuid;
	
	public String getName() {
		return name;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
