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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.openmrs.api.context.Context;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class DatabaseConnector {
	
	public DriverManagerDataSource dataSource() {
		Properties props = Context.getRuntimeProperties();
		mergeDefaultRuntimePropertiess(props);
		
		// String driver = props.getProperty("hibernate.connection.driver_class");
		String username = props.getProperty("hibernate.connection.username");
		String password = props.getProperty("hibernate.connection.password");
		String url = props.getProperty("hibernate.connection.url");
		
		// System.out.println("driver---------------" + driver);
		// System.out.println("username---------------" + username);
		// System.out.println("password---------------" + password);
		// System.out.println("url---------------" + url);
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setUrl(url);
		return dataSource;
	}
	
	private static void mergeDefaultRuntimePropertiess(Properties runtimeProperties) {
		Set<Object> runtimePropertyKeys = new HashSet<Object>();
		runtimePropertyKeys.addAll(runtimeProperties.keySet()); // must do it this way to prevent concurrent mod errors
		for (Object key : runtimePropertyKeys) {
			String prop = (String) key;
			String value = (String) runtimeProperties.get(key);
			if (!prop.startsWith("hibernate") && !runtimeProperties.containsKey("hibernate." + prop))
				runtimeProperties.setProperty("hibernate." + prop, value);
		}
	}
}
