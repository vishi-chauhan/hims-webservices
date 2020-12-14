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

public class PersonMPIname {
	
	private String givenName = "";
	
	private String middleName = "";
	
	private String familyName = "";
	
	private String familyName2 = "";
	
	private boolean preferred = true;
	
	private String prefix = "";
	
	private String familyNamePrefix = "";
	
	private String familyNameSuffix = "";
	
	private String degree = "";
	
	public String getGivenName() {
		return givenName;
	}
	
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	public String getMiddleName() {
		return middleName;
	}
	
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	
	public String getFamilyName() {
		return familyName;
	}
	
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	public String getFamilyName2() {
		return familyName2;
	}
	
	public void setFamilyName2(String familyName2) {
		this.familyName2 = familyName2;
	}
	
	public Boolean getPreferred() {
		return preferred;
	}
	
	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public String getFamilyNamePrefix() {
		return familyNamePrefix;
	}
	
	public void setFamilyNamePrefix(String familyNamePrefix) {
		this.familyNamePrefix = familyNamePrefix;
	}
	
	public String getFamilyNameSuffix() {
		return familyNameSuffix;
	}
	
	public void setFamilyNameSuffix(String familyNameSuffix) {
		this.familyNameSuffix = familyNameSuffix;
	}
	
	public String getDegree() {
		return degree;
	}
	
	public void setDegree(String degree) {
		this.degree = degree;
	}
	
}
