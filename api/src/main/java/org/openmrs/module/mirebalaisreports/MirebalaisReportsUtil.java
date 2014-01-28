/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.mirebalaisreports;

import org.apache.commons.io.IOUtils;
import org.openmrs.module.reporting.common.ContentType;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.renderer.ExcelTemplateRenderer;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.openmrs.module.reporting.report.renderer.TextTemplateRenderer;
import org.openmrs.util.OpenmrsClassLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Utility methods used by the module
 */
public class MirebalaisReportsUtil {

	/**
	 * Given a location on the classpath, return the contents of this resource as a String
	 */
	public static String getStringFromResource(String resourceName) {
		InputStream is = null;
		try {
			is = OpenmrsClassLoader.getInstance().getResourceAsStream(resourceName);
			return IOUtils.toString(is, "UTF-8");
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to load resource: " + resourceName, e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Copied from the reporting module ReportUtil class.
	 * Using this one due to bug fix that will be available in 0.7.8
	 * Also added ability to specify properties
	 * Also, needed to remove the character encoding specification here... :/
	 *
	 * @throws java.io.UnsupportedEncodingException
	 */
	public static RenderingMode renderingModeFromResource(String label, String resourceName, Properties properties) {
		InputStreamReader reader;

		try {
			reader = new InputStreamReader(OpenmrsClassLoader.getInstance().getResourceAsStream(resourceName));
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Error reading template from stream", e);
		}

		final ReportDesign design = new ReportDesign();
		ReportDesignResource resource = new ReportDesignResource();
		resource.setName("template");
		String extension = resourceName.substring(resourceName.lastIndexOf(".") + 1);
		resource.setExtension(extension);
		String contentType = "text/plain";
		for (ContentType type : ContentType.values()) {
			if (type.getExtension().equals(extension)) {
				contentType = type.getContentType();
			}
		}
		resource.setContentType(contentType);
		ReportRenderer renderer = null;
		try {
			resource.setContents(IOUtils.toByteArray(reader));
		}
		catch (Exception e) {
			throw new RuntimeException("Error reading template from stream", e);
		}

		design.getResources().add(resource);
		design.setProperties(properties);
		if ("xls".equals(extension)) {
			renderer = new ExcelTemplateRenderer() {

				public ReportDesign getDesign(String argument) {
					return design;
				}
			};
		} else {
			renderer = new TextTemplateRenderer() {

				public ReportDesign getDesign(String argument) {
					return design;
				}
			};
		}
		return new RenderingMode(renderer, label, extension, null);
	}

    public static List<Map<String, Object>> simplify(DataSet dataSet) {
        List<Map<String, Object>> simplified = new ArrayList<Map<String, Object>>();
        for (DataSetRow row : dataSet) {
            simplified.add(row.getColumnValuesByKey());
        }
        return simplified;
    }
}
