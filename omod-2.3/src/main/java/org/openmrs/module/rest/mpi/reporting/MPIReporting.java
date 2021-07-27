/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.rest.mpi.reporting;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/hisp/rest/mpi_reporting")
public class MPIReporting extends BaseRestController {
	
	@RequestMapping(method = RequestMethod.GET)
	public void getPatient(HttpServletResponse response, HttpServletRequest request,
	        @RequestParam(value = "district") String district,
	        @RequestParam(required = false, value = "hosp_name") String hosp_name,
	        @RequestParam(required = false, value = "from_date") String from_date,
	        @RequestParam(required = false, value = "to_date") String to_date)
	        throws ResponseException, JsonGenerationException, JsonMappingException, IOException, ParseException {
		
		List<Patient> Patientlist = new ArrayList<Patient>();
		List<mpi_report> mpi_report = new ArrayList<mpi_report>();
		Integer male_reg = 0;
		Integer female_reg = 0;
		Integer tot_reg = 0;
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		
		if (StringUtils.isNotBlank(district)) {
			Patientlist = Context.getPatientService().getPatients(district);
			
		}
		
		/*
		 * if (StringUtils.isNotBlank(hosp_name)) { Patientlist =
		 * Context.getPatientService().getPatients(hosp_name);
		 * 
		 * }
		 */
		// Collections.sort(Patientlist, compareByhospname);
		
		Map<String, List<Patient>> map = new HashMap<String, List<Patient>>();
		
		for (Patient pat : Patientlist) {
			
			if (pat == null) {
				continue;
			}
			
			if (!(pat.getAttributeMap().containsKey("Hospital Name"))) {
				continue;
			}
			if (StringUtils.isNotBlank(hosp_name)) {
				if (!((pat.getAttribute("Hospital Name").getValue()).equalsIgnoreCase(hosp_name))) {
					continue;
				}
			}
			
			//	if (StringUtils.isNotBlank(hosp_name)) {
			//	if ((pat.getAttribute("Hospital Name").getValue()).equalsIgnoreCase(hosp_name)) {
			
			if (StringUtils.isNotBlank((CharSequence) from_date)) {
				if ((pat.getDateCreated().compareTo(new SimpleDateFormat("dd-MM-yyyy").parse(from_date))) < 1) {
					continue;
				}
				
			}
			if (StringUtils.isNotBlank((CharSequence) to_date)) {
				if ((pat.getDateCreated().compareTo(new SimpleDateFormat("dd-MM-yyyy").parse(to_date))) > 0) {
					continue;
				}
				
			}
			
			String key = pat.getAttribute("Hospital Name").getValue();
			if (map.containsKey(key)) {
				List<Patient> list = map.get(key);
				list.add(pat);
				
			} else {
				List<Patient> list = new ArrayList<Patient>();
				list.add(pat);
				map.put(key, list);
			}
			//	}
			//}
		}
		
		for (Map.Entry<String, List<Patient>> entry : map.entrySet()) {
			List<Patient> sortedpatlist = new ArrayList<Patient>();
			mpi_report mpi = new mpi_report();
			mpi.setDistrict(district);
			
			male_reg = 0;
			female_reg = 0;
			tot_reg = 0;
			
			sortedpatlist = entry.getValue();
			for (Patient pat : sortedpatlist) {
				tot_reg = tot_reg + 1;
				
				if (pat.getGender().equalsIgnoreCase("M")) {
					male_reg = male_reg + 1;
				} else {
					
					female_reg = female_reg + 1;
				}
				
			}
			
			hospital hospital = new hospital();
			hospital.setHosp_name(entry.getKey());
			hospital.setFemale_reg(female_reg);
			hospital.setMale_reg(male_reg);
			hospital.setTot_reg(tot_reg);
			mpi.setHospital(hospital);
			mpi_report.add(mpi);
			
		}
		
		response.setContentType("application/json");
		ServletOutputStream out = response.getOutputStream();
		
		new ObjectMapper().writeValue(out, mpi_report);
		
	}
	
	class mpi_report {
		
		private String district = "";
		
		private hospital hospital;
		
		public String getDistrict() {
			return district;
		}
		
		public void setDistrict(String district) {
			this.district = district;
		}
		
		public hospital getHospital() {
			return hospital;
		}
		
		public void setHospital(hospital hospital) {
			this.hospital = hospital;
		}
		
	}
	
	Comparator<Patient> compareByhospname = new Comparator<Patient>() {
		
		@Override
		public int compare(Patient o1, Patient o2) {
			return o1.getAttribute("hosp_name").compareTo(o2.getAttribute("hosp_name"));
		}
	};
	
	class hospital {
		
		private String hosp_name = "";
		
		private Integer male_reg;
		
		private Integer female_reg;
		
		private Integer tot_reg;
		
		public String getHosp_name() {
			return hosp_name;
		}
		
		public void setHosp_name(String hosp_name) {
			this.hosp_name = hosp_name;
		}
		
		public Integer getMale_reg() {
			return male_reg;
		}
		
		public void setMale_reg(Integer male_reg) {
			this.male_reg = male_reg;
		}
		
		public Integer getFemale_reg() {
			return female_reg;
		}
		
		public void setFemale_reg(Integer female_reg) {
			this.female_reg = female_reg;
		}
		
		public Integer getTot_reg() {
			return tot_reg;
		}
		
		public void setTot_reg(Integer tot_reg) {
			this.tot_reg = tot_reg;
		}
		
	}
	
}
