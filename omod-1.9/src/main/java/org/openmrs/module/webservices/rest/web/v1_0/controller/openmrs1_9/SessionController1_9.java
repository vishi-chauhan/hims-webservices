/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.LocaleUtils;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * Controller that lets a client check the status of their session, and log out. (Authenticating is
 * handled through a filter, and may happen through this or any other resource.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/session")
public class SessionController1_9 extends BaseRestController {
	
	public static final String USER_CUSTOM_REP = "(uuid,display,username,systemId,userProperties,person:(uuid),privileges:(uuid,name),roles:(uuid,name))";
	
	@Autowired
	RestService restService;
	
	/**
	 * Tells the user their sessionId, and whether or not they are authenticated.
	 * 
	 * @param request
	 * @return
	 * @should return the session id if the user is authenticated
	 * @should return the session id if the user is not authenticated
	 */
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Object get(WebRequest request) {
		boolean authenticated = Context.isAuthenticated();
		SimpleObject session = new SimpleObject();
		session.add("sessionId", request.getSessionId()).add("authenticated", authenticated);
		if (authenticated) {
			
			session.add("user", ConversionUtil.convertToRepresentation(Context.getAuthenticatedUser(),
			    new CustomRepresentation(USER_CUSTOM_REP)));
			session.add("locale", Context.getLocale());
			session.add("allowedLocales", Context.getAdministrationService().getAllowedLocales());
			session.add("sessionLocation",
			    ConversionUtil.convertToRepresentation(Context.getUserContext().getLocation(), Representation.REF));
			
		}
		
		return session;
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public void post(HttpServletRequest request, @RequestBody Map<String, String> body) {
		String localeStr = body.get("locale");
		if (localeStr != null) {
			Locale locale = null;
			try {
				locale = LocaleUtils.toLocale(localeStr);
			}
			catch (IllegalArgumentException e) {
				throw new APIException(" '" + localeStr + "' does not represent a valid locale.");
			}
			Set<Locale> allowedLocales = new HashSet<Locale>(Context.getAdministrationService().getAllowedLocales());
			if (allowedLocales.contains(locale)) {
				Context.setLocale(locale);
			} else {
				throw new APIException(" '" + localeStr + "' is not in the list of allowed locales.");
			}
		}
		String locationUuid = body.get("sessionLocation");
		if (locationUuid != null) {
			Location location = Context.getLocationService().getLocationByUuid(locationUuid);
			if (location == null) {
				throw new APIException(" '" + locationUuid + "' is not the UUID of any location.");
			}
			Context.getUserContext().setLocation(location);
			{ // for compatability with AppUi session location
				request.getSession().setAttribute("emrContext.sessionLocationId", location.getId());
			}
		}
	}
	
	/**
	 * Logs the client out
	 * 
	 * @should log the client out
	 */
	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void delete() {
		
		Context.logout();
	}
	
}
