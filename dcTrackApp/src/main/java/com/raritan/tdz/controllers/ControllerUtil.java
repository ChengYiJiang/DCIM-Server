package com.raritan.tdz.controllers;

import javax.servlet.http.HttpServletResponse;

/**
 * Common utility functions for all controllers.
 * @author andrewc
 *
 */
class ControllerUtil {

	public static void addCommonResponseHeaders(HttpServletResponse resp) {
		resp.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	}
}
