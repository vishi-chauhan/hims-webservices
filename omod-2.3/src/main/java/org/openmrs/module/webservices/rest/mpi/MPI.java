/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.mpi;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.scheduler.tasks.TestTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.openmrs.api.context.Context;

//Test
/*
 * During registration if ADHAR is available enter column-
 * Person Updated = PERNO
 * Patient Updated = PATNO
 * MPI ID = NA
 * 
 *  
   During registration if ADHAR is not available enter column-
 * Person Updated = PERADNA
 * Patient Updated = PATNO
 * MPI ID = NA
 * 
 * If he later brings adhar
 * Person Updated = PERNO
 * Patient Updated = PATNO
 * MPI ID = NA
   

   								   
  MPI ID = ID received from central instance on insertion. 								   
  MPI ID will be inserted into the MPI ID attribute of person.
  
  
  Patient can also be searched on basis of attributes.(EXAMPLE-ADHAR01)
  
  Status of patient -> PATNO-->PATYES
  Status of patient ->PERNO-->PERYES-->PERCOM
  
  PERNO-->Nothing updated to central instance.
  PERYES-->Person updated to central instance.
  PATNO-->Person updated but patient pending.
  PATYES-->Patient updated to central instance.
  PERCOM-->Person and patient of it updation complete.
  PERADNA-->Person ADHAR not available.
  
   								   
   ### For these columns we only need to call person service.
   
   When person is getting created , below attributes must be created along with it
    Person Updated - PERNO
    Patient Updated -PATNO
    MPI ID          -NOMPI
    
    Patient identifier - Required to be hardcoded accor to central instance
 * 
 */

public class MPI extends AbstractTask {
	
	// Logger 
	private static final Logger log = LoggerFactory.getLogger(TestTask.class);
	
	String jsonstr = "";
	
	String name = "admin";
	
	String password = "Admin123";
	
	String authString = name + ":" + password;
	
	String authStringEnc = Base64.getEncoder().encodeToString(authString.getBytes());
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#initialize(TaskDefinition)
	 */
	@Override
	public void initialize(TaskDefinition taskDefinition) {
		log.info("Initializing task " + taskDefinition);
		
	}
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		try {
			
			List<Person> plist = new ArrayList<Person>();
			List<Person> patlist = new ArrayList<Person>();
			plist = personwithAdhar(); //Fetch person where person updated attribute is PERNO(Person not updated)
			
			String poststauts = "";
			
			for (Person person : plist) {
				poststauts = postPerson(ObjectToJson(person));
				if (!(poststauts.equalsIgnoreCase("FAILURE"))) {
					updatePerson(person, poststauts); // Update person updated attribute to PERYES and MPIID (Person updated to CI)
				}
			}
			
			patlist = patientwithAdhar();
			for (Person pat : patlist) {
				
				if (pat.getIsPatient()) {
					Patient patient = Context.getPatientService().getPatientByUuid(pat.getUuid());
					PersonAttribute patt = new PersonAttribute();
					poststauts = postPatient(PatientObjectToJson(patient, pat.getAttribute("MPI ID").getValue()));
					
					if (!(poststauts.equalsIgnoreCase("FAILURE"))) {
						updatePatient(patient);
					}
					
				}
				
			}
			
		}
		catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	@Override
	public void shutdown() {
		log.info("Shutting down task ...");
		super.shutdown();
	}
	
	public List<Person> personwithAdhar() {
		
		List<Person> plist = new ArrayList<Person>();
		
		plist = Context.getPersonService().getPeople("PERNO", false, false);
		
		return plist;
		
	}
	
	public List<Person> patientwithAdhar() {
		
		List<Person> patlist = new ArrayList<Person>();
		
		//Using PERYES to fetch only those records which are synced to Central instance
		patlist = Context.getPersonService().getPeople("PERYES", false, false);
		
		return patlist;
		
	}
	
	public String postPerson(String personJson) {
		
		Client client = Client.create();
		String output = "FAILURE";
		try {
			
			WebResource webResource = client
			        .resource("http://localhost:8080/openmrs/ws/rest/v1/person");
			
			ClientResponse response = webResource.type("application/json").header("Authorization", "Basic " + authStringEnc)
			        .post(ClientResponse.class, personJson);
			
			if (response.getStatus() != 201) {
				log.warn("MPI - Failed Posting person Response status->  " + response.getStatus());
				
			}
			else {
				
				String resp = response.getEntity(String.class);
				Gson g = new Gson();
				personresponse p = g.fromJson(resp, personresponse.class);
				output = p.getUuid();
				
			}
			
		}
		catch (Exception e) {
			
			log.warn("MPI - Failed Posting person  " + e.getMessage(), e);
			
		}
		return output;
	}
	
	public String postPatient(String patientJson) {
		
		Client client = Client.create();
		String output = "FAILURE";
		try {
			
			WebResource webResource = client.resource("http://localhost:8080/openmrs/ws/rest/v1/patient");
			
			ClientResponse response = webResource.type("application/json").header("Authorization", "Basic " + authStringEnc)
			        .post(ClientResponse.class, patientJson);
			
			if (response.getStatus() == 201) {
				
				output = "SUCCESS";
				
			}
			else {
				log.warn("MPI - Failed Posting patient Response status->  " + response.getStatus());
			}
			
		}
		catch (Exception e) {
			
			log.warn("MPI - Failed Posting person  " + e.getMessage(), e);
			
		}
		return output;
	}
	
	public void updatePerson(Person updperson, String MPIID) {
		
		//		PersonAttribute pat1 = new PersonAttribute();
		//		PersonAttribute pat2 = new PersonAttribute();
		//		PersonAttributeType mpiid = new PersonAttributeType();
		//		PersonAttributeType perupd = new PersonAttributeType();
		//		
		//		mpiid.setName("MPI ID");
		//		pat1.setAttributeType(mpiid);
		//		pat1.setValue(MPIID);
		//		
		//		perupd.setName("Person Updated");
		//		pat2.setAttributeType(perupd);
		//		pat2.setValue("PERYES");
		//		
		//		SortedSet<PersonAttribute> pattr = new TreeSet<PersonAttribute>();
		//		pattr.add(pat1);
		//		pattr.add(pat2);
		//		
		//		updperson.setAttributes(pattr);
		Map<String, PersonAttribute> mperattribs = updperson.getAllAttributeMap();
		if (mperattribs.containsKey("MPI ID")) {
			updperson.getAttribute("MPI ID").setValue(MPIID);
			updperson.getAttribute("Person Updated").setValue("PERYES");
		}
		else {
			updperson.getAttribute("Person Updated").setValue("PERERR");
		}
		Context.getPersonService().savePerson(updperson);
		
	}
	
	public void updatePatient(Patient updpat) {
		
		Person per = new Person();
		per = Context.getPersonService().getPersonByUuid(updpat.getUuid());
		Map<String, PersonAttribute> mperattribs = per.getAllAttributeMap();
		
		if (mperattribs.containsKey("Patient Updated")) {
			per.getAttribute("Patient Updated").setValue("PATYES");
			per.getAttribute("Person Updated").setValue("PERCOM");
			Context.getPersonService().savePerson(per);
		}
		else {
			per.getAttribute("Person Updated").setValue("PERERR");
		}
	}
	
	public String ObjectToJson(Person person) {
		
		PersonMPI permpi = new PersonMPI();
		Person per = new Person();
		per = Context.getPersonService().getPersonByUuid(person.getUuid());
		Set<PersonName> names = null;
		Set<PersonAddress> address = null;
		List<PersonAttribute> attribs = null;
		
		names = per.getNames();
		address = per.getAddresses();
		attribs = per.getActiveAttributes();
		
		List<PersonMPIname> lpname = new ArrayList<PersonMPIname>();
		List<PersonMPIaddress> lpadd = new ArrayList<PersonMPIaddress>();
		List<PersonMPIattribs> lpattribs = new ArrayList<PersonMPIattribs>();
		
		names = per.getNames();
		for (PersonName pname : names) {
			PersonMPIname pnameobj = new PersonMPIname();
			pnameobj.setDegree(pname.getDegree());
			pnameobj.setFamilyName(pname.getFamilyName());
			pnameobj.setFamilyName2(pname.getFamilyName2());
			pnameobj.setFamilyNamePrefix(pname.getFamilyNamePrefix());
			pnameobj.setFamilyNameSuffix(pname.getFamilyNameSuffix());
			pnameobj.setGivenName(pname.getGivenName());
			pnameobj.setMiddleName(pname.getMiddleName());
			pnameobj.setPreferred(pname.getPreferred());
			pnameobj.setPrefix(pname.getPrefix());
			lpname.add(pnameobj);
		}
		
		for (PersonAddress padd : address) {
			PersonMPIaddress paddobj = new PersonMPIaddress();
			paddobj.setAddress1(padd.getAddress1());
			paddobj.setAddress2(padd.getAddress2());
			paddobj.setAddress3(padd.getAddress3());
			paddobj.setAddress4(padd.getAddress4());
			paddobj.setAddress5(padd.getAddress5());
			paddobj.setAddress6(padd.getAddress6());
			paddobj.setCityVillage(padd.getCityVillage());
			paddobj.setCountry(padd.getCountry());
			paddobj.setCountyDistrict(padd.getCountyDistrict());
			paddobj.setPostalCode(padd.getPostalCode());
			paddobj.setPreferred(padd.getPreferred());
			paddobj.setStateProvince(padd.getStateProvince());
			lpadd.add(paddobj);
			
		}
		for (PersonAttribute patt : attribs) {
			PersonMPIattribs pattrobj = new PersonMPIattribs();
			pattrobj.setAttributeType(patt.getAttributeType().getUuid());
			pattrobj.setValue(patt.getValue());
			lpattribs.add(pattrobj);
			
		}
		
		permpi.setNames(lpname);
		permpi.setAddresses(lpadd);
		permpi.setAttributes(lpattribs);
		
		if (per.getBirthdateEstimated()) {
			permpi.setAge(per.getAge());
		} else {
			
			permpi.setBirthdate(per.getBirthdate());
		}
		
		permpi.setCauseOfDeath(per.getCauseOfDeathNonCoded());
		permpi.setDead(per.getDead());
		permpi.setGender(per.getGender());
		permpi.setBirthdateEstimated(per.getBirthdateEstimated());
		permpi.setDeathDate(per.getDeathDate());
		permpi.setDeathdateEstimated(per.getDeathdateEstimated());
		
		try {
			
			jsonstr = new Gson().toJson(permpi);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("JSON String --" + jsonstr);
		return jsonstr;
	}
	
	public String PatientObjectToJson(Patient pat, String mpiid) {
		
		PatientMPI patmpi = new PatientMPI();
		
		Set<PatientIdentifier> spatiden = null;
		
		List<PatientMPIidentifier> lpatidentifier = new ArrayList<PatientMPIidentifier>();
		
		spatiden = pat.getIdentifiers();
		
		for (PatientIdentifier piden : spatiden) {
			PatientMPIidentifier pidenobj = new PatientMPIidentifier();
			
			pidenobj.setIdentifier(piden.getIdentifier() + "1");//+1 Is for testing only
			pidenobj.setIdentifierType(piden.getIdentifierType().getUuid());//Need to be hardcoded accor to central instance
			pidenobj.setLocation(piden.getLocation().getUuid().toString());
			pidenobj.setPreferred(piden.getPreferred());
			
			lpatidentifier.add(pidenobj);
		}
		
		patmpi.setPerson(mpiid); // Person ID received from central instance as patient will 
		                         //be created there with person UUID existing in remote system
		patmpi.setIdentifiers(lpatidentifier);
		
		try {
			
			jsonstr = new Gson().toJson(patmpi);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Patient JSON String --" + jsonstr);
		return jsonstr;
	}
}

class personresponse {
	
	private String uuid = "FAILURE";
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
}
