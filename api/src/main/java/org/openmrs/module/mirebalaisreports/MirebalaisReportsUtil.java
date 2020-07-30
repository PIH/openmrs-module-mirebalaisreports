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
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.descriptor.MissingConceptException;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.radiologyapp.RadiologyProperties;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Utility methods used by the module
 */
public class MirebalaisReportsUtil {

    public static String applyMetadataReplacements(String sql) {

        MirebalaisReportsProperties mrp = Context.getRegisteredComponents(MirebalaisReportsProperties.class).get(0);
        DispositionService dispositionService = Context.getService(DispositionService.class);
        RadiologyProperties radiologyProperties = Context.getRegisteredComponents(RadiologyProperties.class).get(0);

        sql = replace(sql, "zlId", mrp.getZlEmrIdentifierType());
        sql = replace(sql, "refNum", mrp.getUserEnteredReferenceNumberIdentifierType());
        sql = replace(sql, "dosId", mrp.getDossierNumberIdentifierType());
        sql = replace(sql, "hivId", mrp.getHivEmrIdentifierType());
        sql = replace(sql, "biometricId", mrp.getBiometricIdentifierType());

        sql = replace(sql, "testPt", mrp.getTestPatientPersonAttributeType());
        sql = replace(sql, "phoneType", mrp.getTelephoneNumberPersonAttributeType());
        sql = replace(sql, "unknownPt", mrp.getUnknownPatientPersonAttributeType());
        sql = replace(sql, "clerkEncRole", mrp.getAdministrativeClerkEncounterRole());

        sql = replace(sql, "regEnc", mrp.getRegistrationEncounterType());
        sql = replace(sql, "chkEnc", mrp.getCheckInEncounterType());
        sql = replace(sql, "vitEnc", mrp.getVitalsEncounterType());
        sql = replace(sql, "consEnc", mrp.getConsultEncounterType());
        sql = replace(sql, "radEnc", mrp.getRadiologyOrderEncounterType());
        sql = replace(sql, "radStudyEnc", mrp.getRadiologyStudyEncounterType());
        sql = replace(sql, "radReportEnc", mrp.getRadiologyReportEncounterType());
        sql = replace(sql, "admitEnc", mrp.getAdmissionEncounterType());
        sql = replace(sql, "exitEnc", mrp.getExitFromInpatientEncounterType());
        sql = replace(sql, "transferEnc", mrp.getTransferEncounterType());
        sql = replace(sql, "postOpNoteEnc", mrp.getPostOpNoteEncounterType());
        sql = replace(sql, "labResultEnc", mrp.getLabResultsEncounterType());
        sql = replace(sql, "oncNoteEnc", mrp.getOncologyEncounterType());
        sql = replace(sql, "oncIntakeEnc", mrp.getOncologyIntakeEncounterType());
        sql = replace(sql, "chemoEnc", mrp.getChemotherapyEncounterType());
        sql = replace(sql, "ncdNoteEnc", mrp.getNCDConsultEncounterType());
        sql = replace(sql, "mentalHealthEnc", mrp.getMentalHealthAssessmentEncounterType());
        sql = replace(sql, "dispEnc", mrp.getMedicationDispensedEncountertype());
        sql = replace(sql, "EDTriageEnc", mrp.getEDTriageEncounterType());
        sql = replace(sql, "PedInitEnc", mrp.getPedsInitialConsultEncounterType());
        sql = replace(sql, "PedFollowEnc", mrp.getPedsFollowupConsultEncounterType());
        sql = replace(sql, "AdultInitEnc", mrp.getAdultInitialConsultEncounterType());
        sql = replace(sql, "AdultFollowEnc", mrp.getAdultFollowupConsultEncounterType());
        sql = replace(sql, "NCDInitEnc", mrp.getNCDInitialEncounterType());
        sql = replace(sql, "NCDFollowEnc", mrp.getNCDFollowupEncounterType());
        sql = replace(sql, "vctEnc", mrp.getVCTEncounterType());
        sql = replace(sql, "ANCInitEnc", mrp.getANCInitialEncounterType());
        sql = replace(sql, "ANCFollowEnc", mrp.getANCFollowupEncounterType());
        sql = replace(sql, "DeliveryEnc", mrp.getDeliveryEncounterType());
        sql = replace(sql, "mexConsultEnc", mrp.getMexicoConsultEncounterType());

        sql = replace(sql, "consultingClinician", mrp.getConsultingClinicianEncounterRole());
        sql = replace(sql, "orderingProvider", mrp.getOrderingProviderEncounterRole());
        sql = replace(sql, "principalResultsInterpreter", mrp.getPrincipalResultsInterpreterEncounterRole());
        sql = replace(sql, "radiologyTech", mrp.getRadiologyTechnicianEncounterRole());
        sql = replace(sql, "nurse", mrp.getNurseEncounterRole());

        sql = replace(sql, "icd10", mrp.getIcd10ConceptSource());

        sql = replace(sql, "paid", mrp.getAmountPaidConcept());
        sql = replace(sql, "reasonForVisit", mrp.getReasonForVisitConcept());
        sql = replace(sql, "wt", mrp.getWeightConcept());
        sql = replace(sql, "ht", mrp.getHeightConcept());
        sql = replace(sql, "muac", mrp.getMuacConcept());
        sql = replace(sql, "temp", mrp.getTemperatureConcept());
        sql = replace(sql, "hr", mrp.getPulseConcept());
        sql = replace(sql, "rr", mrp.getRespiratoryRateConcept());
        sql = replace(sql, "sbp", mrp.getSystolicBpConcept());
        sql = replace(sql, "dbp", mrp.getDiastolicBpConcept());
        sql = replace(sql, "o2", mrp.getBloodOxygenSaturationConcept());
        sql = replace(sql, "coded", mrp.getCodedDiagnosisConcept());
        sql = replace(sql, "noncoded", mrp.getNonCodedDiagnosisConcept());
        sql = replace(sql, "diagnosisOrder", mrp.getDiagnosisOrderConcept());
        sql = replace(sql, "diagnosisCertainty", mrp.getDiagnosisCertaintyConcept());
        sql = replace(sql, "comment", mrp.getClinicalImpressionsConcept());
        sql = replace(sql, "presentingHistory", mrp.getPresentingHistoryConcept());
        sql = replace(sql, "physicalExam", mrp.getPhysicalExamConcept());
        sql = replace(sql, "clinicalImpressionComments", mrp.getClinicalImpressionCommentsConcept());
        sql = replace(sql, "clinicalManagementPlan", mrp.getClinicalManagementPlanConcept());
        sql = replace(sql, "dispensingConstruct", mrp.getDispensingConstruct());
        sql = replace(sql, "medName", mrp.getMedicationNameConcept());
        sql = replace(sql, "medInstructions", mrp.getMedicationInstructionsConcept());
        sql = replace(sql, "notifiable", mrp.getSetOfWeeklyNotifiableDiseases());
        sql = replace(sql, "urgent", mrp.getSetOfUrgentDiseases());
        sql = replace(sql, "santeFamn", mrp.getSetOfWomensHealthDiagnoses());
        sql = replace(sql, "psycho", mrp.getSetOfPsychologicalDiagnoses());
        sql = replace(sql, "peds", mrp.getSetOfPediatricDiagnoses());
        sql = replace(sql, "outpatient", mrp.getSetOfOutpatientDiagnoses());
        sql = replace(sql, "ncd", mrp.getSetOfNcdDiagnoses());
        sql = replace(sql, "notDx", mrp.getSetOfNonDiagnoses());
        sql = replace(sql, "ed", mrp.getSetOfEmergencyDiagnoses());
        sql = replace(sql, "ageRst", mrp.getSetOfAgeRestrictedDiagnoses());
        sql = replace(sql, "oncology", mrp.getSetOfOncologyDiagnoses());

        sql = replace(sql, "transfOut", mrp.getTransferOutLocationConcept());
        sql = replace(sql, "traumaOccur", mrp.getOccurrenceOfTraumaConcept());
        sql = replace(sql, "traumaType", mrp.getTraumaTypeConcept());
        sql = replace(sql, "rvd", mrp.getReturnVisitDate());
        sql = replace(sql, "boardingFor", mrp.getBoardingForConcept());
        sql = replace(sql, "typeOfPatient", mrp.getTypeOfPatientConcept());

        sql = replace(sql, "testOrder", mrp.getTestOrderType());
        sql = replace(sql, "pathologyTestOrder", mrp.getPathologyTestOrderType());

        try {
            sql = replace(sql, "dispo", dispositionService.getDispositionDescriptor().getDispositionConcept());
            sql = replace(sql, "admitDispoConcept", mrp.getAdmissionDispositionConcept());
            sql = replace(sql, "dischargeDispoConcept", mrp.getDischargeDispositionConcept());
            sql = replace(sql, "transferOutDispoConcept", mrp.getTransferOutOfHospitalDispositionConcept());
            sql = replace(sql, "transferWithinDispoConcept", mrp.getTransferWithinHospitalDispositionConcept());
            sql = replace(sql, "deathDispoConcept", mrp.getDeathDispositionConcept());
            sql = replace(sql, "leftWithoutSeeingDispoConcept", mrp.getLeftWithoutSeeingClinicianDispositionConcept());
            sql = replace(sql, "leftWithoutCompletingDispoConcept", mrp.getLeftWithoutCompletingTreatmentDispositionConcept());
            sql = replace(sql, "stillHospitalizedDispoConcept", mrp.getStillHospitalizedDispositionConcept());
            sql = replace(sql, "edObservationDispoConcept", mrp.getEdObservationDispositionConcept());
        }
        catch (MissingConceptException e) {
            // some installs, like Liberia, aren't currently configured with dispositions, so we don't want to fail here
        }

        // sets for radiological exam modalities
        try {
            sql = replace(sql, "xrayOrderables", radiologyProperties.getXrayOrderablesConcept());
            sql = replace(sql, "ctOrderables", radiologyProperties.getCTScanOrderablesConcept());
            sql = replace(sql, "ultrasoundOrderables", radiologyProperties.getUltrasoundOrderablesConcept());
        }
        catch (IllegalStateException e) {
            // some installs aren't currently configured for radiology, so we don't want to fail here
        }

        // sets for anatomical groupings of radiological exams
        sql = replace(sql, "radiologyChest", mrp.getChestRadiologyExamSetConcept());
        sql = replace(sql, "radiologyHeadNeck", mrp.getHeadAndNeckRadiologyExamSetConcept());
        sql = replace(sql, "radiologySpine", mrp.getSpineRadiologyExamSetConcept());
        sql = replace(sql, "radiologyVascular", mrp.getVascularRadiologyExamSetConcept());
        sql = replace(sql, "radiologyAbdomenPelvis", mrp.getAbdomenAndPelvisRadiologyExamSetConcept());
        sql = replace(sql, "radiologyMusculoskeletal", mrp.getMusculoskeletalNonCranialAndSpinalRadiologyExamSetConcept());

        // programs
        sql = replace(sql, "zikaProgram", mrp.getZikaProgram());

        return sql;
    }

    private static String replace(String sql, String oldValue, OpenmrsObject newValue) {
        if (newValue != null) {  // some replacemnets, like the radiology encounter types, aren't available on all systems, so just ignore in these cases
            String s = sql.replace(":" + oldValue, newValue.getId().toString());
            return s;
        }
        else {
            return sql;
        }
    }

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

    // has been moved to ReportUtil in reporting module, use the one there
    @Deprecated
    public static List<Map<String, Object>> simplify(DataSet dataSet) {
        List<Map<String, Object>> simplified = new ArrayList<Map<String, Object>>();
        for (DataSetRow row : dataSet) {
            simplified.add(row.getColumnValuesByKey());
        }
        return simplified;
    }
}
