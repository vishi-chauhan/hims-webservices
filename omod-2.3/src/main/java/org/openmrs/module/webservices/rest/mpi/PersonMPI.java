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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.openmrs.Concept;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.User;

public class PersonMPI {
	
	private List<PersonMPIaddress> addresses = null;
	
	private List<PersonMPIname> names = null;
	
	private List<PersonMPIattribs> attributes = null;
	
	private String gender;
	
	private Date birthdate;
	
	private Boolean birthdateEstimated = false;
	
	private Boolean dead = false;
	
	private Date deathDate;
	
	private String causeOfDeath;
	
	private Integer age;
	
	private boolean deathdateEstimated;
	
	public List<PersonMPIaddress> getAddresses() {
		return addresses;
	}
	
	public void setAddresses(List<PersonMPIaddress> addresses) {
		this.addresses = addresses;
	}
	
	public void setCauseOfDeath(String causeOfDeath) {
		this.causeOfDeath = causeOfDeath;
	}
	
	public String getCauseOfDeath() {
		return causeOfDeath;
	}
	
	public List<PersonMPIname> getNames() {
		return names;
	}
	
	public void setNames(List<PersonMPIname> names) {
		this.names = names;
	}
	
	public List<PersonMPIattribs> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(List<PersonMPIattribs> attributes) {
		this.attributes = attributes;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public Date getBirthdate() {
		return birthdate;
	}
	
	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}
	
	public Boolean getBirthdateEstimated() {
		return birthdateEstimated;
	}
	
	public void setBirthdateEstimated(Boolean birthdateEstimated) {
		this.birthdateEstimated = birthdateEstimated;
	}
	
	public Boolean getDead() {
		return dead;
	}
	
	public void setDead(Boolean dead) {
		this.dead = dead;
	}
	
	public Date getDeathDate() {
		return deathDate;
	}
	
	public void setDeathDate(Date deathDate) {
		this.deathDate = deathDate;
	}
	
	public Integer getAge() {
		return age;
	}
	
	public void setAge(Integer age) {
		this.age = age;
	}
	
	public boolean isDeathdateEstimated() {
		return deathdateEstimated;
	}
	
	public void setDeathdateEstimated(boolean deathdateEstimated) {
		this.deathdateEstimated = deathdateEstimated;
	}
	
}
