package org.openmrs.module.mirebalaisreports.definitions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.descriptor.MissingConceptException;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.config.Config;
import org.openmrs.module.radiologyapp.RadiologyProperties;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportProcessorConfiguration;
import org.openmrs.module.reporting.report.processor.DiskReportProcessor;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Properties;

/**
 * Includes helpful methods for dealing with Mirebalais Metadata (this class exists so that someday we might consider
 * moving BaseReportManager into a shared refapp module)
 */
public abstract class BaseMirebalaisReportManager extends BaseReportManager {

    public static final String SQL_DIR = "org/openmrs/module/mirebalaisreports/sql/";

    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
	protected MirebalaisReportsProperties mirebalaisReportsProperties;

    @Autowired
    protected DispositionService dispositionService;

    @Autowired
    protected EmrApiProperties emrApiProperties;

    @Autowired
    protected RadiologyProperties radiologyProperties;

    @Autowired
    protected Config config;

    public abstract String getUuid();

    public void setMirebalaisReportsProperties(MirebalaisReportsProperties mirebalaisReportsProperties) {
        this.mirebalaisReportsProperties = mirebalaisReportsProperties;
    }

    public Parameter getStartDateParameter() {
        return new Parameter("startDate", "mirebalaisreports.parameter.startDate", Date.class);
    }

    public Parameter getEndDateParameter() {
        return new Parameter("endDate", "mirebalaisreports.parameter.endDate", Date.class);
    }

    public Parameter getLocationParameter() {
        return new Parameter("location", "mirebalaisreports.parameter.location", Location.class);
    }

    public CohortIndicator buildIndicator(String name, CohortDefinition cd, String mappings) {
        CohortIndicator indicator = new CohortIndicator(name);
        indicator.addParameter(getStartDateParameter());
        indicator.addParameter(getEndDateParameter());
        indicator.addParameter(getLocationParameter());
        indicator.setCohortDefinition(map(cd, mappings));
        return indicator;
    }

    protected String applyMetadataReplacements(String sql) {
        log.debug("Replacing metadata references");
        MirebalaisReportsProperties mrp = mirebalaisReportsProperties;

        sql = replace(sql, "zlId", mrp.getZlEmrIdentifierType());
        sql = replace(sql, "refNum", mrp.getUserEnteredReferenceNumberIdentifierType());
        sql = replace(sql, "dosId", mrp.getDossierNumberIdentifierType());
        sql = replace(sql, "hivId", mrp.getHivEmrIdentifierType());

        sql = replace(sql, "testPt", mrp.getTestPatientPersonAttributeType());
        sql = replace(sql, "phoneType", mrp.getTelephoneNumberPersonAttributeType());
        sql = replace(sql, "unknownPt", mrp.getUnknownPatientPersonAttributeType());

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

        sql = replace(sql, "consultingClinician", mrp.getConsultingClinicianEncounterRole());
        sql = replace(sql, "orderingProvider", mrp.getOrderingProviderEncounterRole());
        sql = replace(sql, "principalResultsInterpreter", mrp.getPrincipalResultsInterpreterEncounterRole());
        sql = replace(sql, "radiologyTech", mrp.getRadiologyTechnicianEncounterRole());
        sql = replace(sql, "nurse", mrp.getNurseEncounterRole());

        sql = replace(sql, "icd10", mrp.getIcd10ConceptSource());

        sql = replace(sql, "paid", mrp.getAmountPaidConcept());
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

        log.debug("Replacing metadata references complete.");
        return sql;
    }

    protected String replace(String sql, String oldValue, OpenmrsObject newValue) {
        if (newValue != null) {  // some replacemnets, like the radiology encounter types, aren't available on all systems, so just ignore in these cases
            String s = sql.replace(":" + oldValue, newValue.getId().toString());
            return s;
        }
        else {
            return sql;
        }
    }

    protected ReportProcessorConfiguration constructSaveToDiskReportProcessorConfiguration() {

        Properties saveToDiskProperties = new Properties();
        saveToDiskProperties.put(DiskReportProcessor.SAVE_LOCATION, OpenmrsUtil.getApplicationDataDirectory() + "reports");
        saveToDiskProperties.put(DiskReportProcessor.COMPRESS_OUTPUT, "true");

        ReportProcessorConfiguration saveToDiskProcessorConfiguration
                = new ReportProcessorConfiguration("saveToDisk", DiskReportProcessor.class, saveToDiskProperties, true, false);
        saveToDiskProcessorConfiguration.setProcessorMode(ReportProcessorConfiguration.ProcessorMode.AUTOMATIC);

        return saveToDiskProcessorConfiguration;

    }
}
