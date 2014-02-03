package org.openmrs.module.mirebalaisreports.dataset.definition.evaluator;


import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.mirebalaisreports.dataset.definition.AwaitingAdmissionDataSetDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Locale;

@Handler(supports = AwaitingAdmissionDataSetDefinition.class)
public class AwaitingAdmissionDataSetEvaluator implements DataSetEvaluator{

    @Autowired
    AdtService adtService;

    @Autowired
    DispositionService dispositionService;

    @Autowired
    ConceptService conceptService;

    @Autowired
    EmrApiProperties emrApiProperties;

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        String locale = Context.getLocale().toString();
        AwaitingAdmissionDataSetDefinition dsd = new AwaitingAdmissionDataSetDefinition();
        Location location = dsd.getLocation();
        Location visitLocation = null ;
        if (location != null ) {
            visitLocation = adtService.getLocationThatSupportsVisits(location);
        }
        PatientIdentifierType primaryIdentifierType = emrApiProperties.getPrimaryIdentifierType();
        PatientIdentifierType dossierIdentifierType = emrApiProperties.getDossierIdentifierType();
        EncounterType admissionEncounterType = emrApiProperties.getAdmissionEncounterType();

        Disposition admitToHospital = dispositionService.getDispositionByUniqueId("admitToHospital");
        Concept admissionLocationConcept = dispositionService.getDispositionDescriptor().getAdmissionLocationConcept();
        Integer admissionLocationConceptId = null;
        if (admissionLocationConcept != null ){
            admissionLocationConceptId = admissionLocationConcept.getConceptId();
        }

        Concept admissionDispositionConcept = null;
        String conceptCode = admitToHospital.getConceptCode();
        if ( StringUtils.isNotBlank(conceptCode) ) {
            String[] conceptMap = conceptCode.split(":");
            if ( (conceptMap !=null) && (conceptMap.length == 2) ) {
                admissionDispositionConcept = conceptService.getConceptByMapping(conceptMap[1], conceptMap[0]);
            }
        }
        Integer dispositionConceptId = dispositionService.getDispositionDescriptor().getDispositionConcept().getId();
        Concept codedDiagnosis = conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_CODED_DIAGNOSIS, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        Concept diagnosisOrder = conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        Concept primaryDiagnosis = conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_PRIMARY, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);

        StringBuilder sb = new StringBuilder("select distinct v.patient_id " +
                ", id1.identifier as primaryIdentifier" +
                ", id2.identifier as dossierNumber" +
                ", n.given_name as patientFirstName, n.family_name as patientLastName" +
                ", loc1.name as consultationLocation" +
                ", concat(loc1.uuid, '') as consultationLocationUuid" +
                ", dispo_encounter.encounter_datetime as consultationDateTime" +
                ", n2.given_name as providerFirstName, n2.family_name as providerLastName " +
                ", case when (dispo.value_coded is not null ) then " +
                "       (select uuid from location where location_id in " +
                "           (select CAST(o2.value_text as UNSIGNED) " +
                "           from obs o2 where o2.obs_group_id = dispo.obs_group_id and o2.concept_id= :admissionLocationConceptId)" +
                "       )" +
                "  else ifnull (dispo.value_coded, '') " +
                "  end as 'admissionLocationUuid'  " +
                ", case when (diagnosis.value_coded is not null ) then " +
                "       (select cn.name " +
                "        from concept c, concept_name cn " +
                "        where c.concept_id=cn.concept_id " +
                "            and c.concept_id= diagnosis.value_coded and cn.voided=0 " +
                "            and cn.locale='");
                                sb.append(locale);
                             sb.append("' " +
                "            and cn.concept_name_type = 'FULLY_SPECIFIED' " +
                "        ) " +
                "  else ifnull (diagnosis.value_coded, '') " +
                "  end as 'diagnosis', " +
                "v.visit_id as visitId   " +
                "from visit v " +
                "inner join person_name as n on ( v.patient_id = n.person_id and n.voided = 0 and n.preferred=1 ) " +
                "inner join patient_identifier as id1 on ( v.patient_id = id1.patient_id and id1.identifier_type = :primaryIdentifierType and id1.voided = 0 and id1.preferred =1 ) " +
                "left join patient_identifier as id2 on (v.patient_id = id2.patient_id and id2.identifier_type = :dossierIdentifierType and id2.voided = 0) " +
                "inner join encounter dispo_encounter " +
                "   on dispo_encounter.visit_id = v.visit_id " +
                "   and dispo_encounter.voided = 0 " +
                "inner join obs dispo " +
                "   on dispo.encounter_id = dispo_encounter.encounter_id " +
                "   and dispo.concept_id = :dispositionConceptId" +
                "   and dispo.value_coded = :admissionDispositionConceptId" +
                "   and dispo.voided = 0 " +
                "inner join obs diagnosis " +
                "   on (dispo_encounter.encounter_id = diagnosis.encounter_id " +
                "   and diagnosis.voided =0  " +
                "  and diagnosis.concept_id = :codedDiagnosis)  " +
                "inner join obs primaryDiagnosis  " +
                " on (primaryDiagnosis.obs_group_id = diagnosis.obs_group_id " +
                " and primaryDiagnosis.voided = 0  " +
                " and primaryDiagnosis.concept_id= :diagnosisOrder  " +
                " and primaryDiagnosis.value_coded= :primaryDiagnosis) " +
                "inner join location as loc1 on (dispo_encounter.location_id = loc1.location_id) " +
                "inner join encounter_provider as ep on (dispo_encounter.encounter_id = ep.encounter_id)  " +
                "inner join provider on (ep.provider_id = provider.provider_id) " +
                "inner join person_name as n2 on (provider.person_id = n2.person_id and n2. voided = 0 and n. preferred=1 ) " +
                "where v.date_stopped is null " +
                "  and v.voided = 0  ");
        if (visitLocation != null) {
            sb.append(" and v.location_id= :visitLocation ");
        }
        sb.append("     and ( " +
                        "  select count(*) " +
                        "  from encounter admission " +
                        "  where admission.visit_id = v.visit_id " +
                        "    and admission.encounter_type = :admissionEncounterType " +
                        "    and admission.voided = 0 " +
                        "   ) = 0; ");

        SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sb.toString());
        if (visitLocation != null) {
            query.setInteger("visitLocation", visitLocation.getId());
        }

        query.setInteger("admissionLocationConceptId", admissionLocationConceptId);
        query.setInteger("primaryIdentifierType", primaryIdentifierType.getId());
        query.setInteger("dossierIdentifierType", dossierIdentifierType.getId());
        query.setInteger("dispositionConceptId", dispositionConceptId);
        query.setInteger("admissionDispositionConceptId", admissionDispositionConcept.getId());
        query.setInteger("codedDiagnosis", codedDiagnosis.getId());
        query.setInteger("diagnosisOrder", diagnosisOrder.getId());
        query.setInteger("primaryDiagnosis", primaryDiagnosis.getId());
        query.setInteger("admissionEncounterType", admissionEncounterType.getId());

        List<Object[]> list = query.list();
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evaluationContext);
        for (Object[] o : list) {
            DataSetRow row = new DataSetRow();
            row.addColumnValue(new DataSetColumn("patientId", "patientId", String.class), o[0]);
            row.addColumnValue(new DataSetColumn("primaryIdentifier", "primaryIdentifier", String.class), o[1]);
            row.addColumnValue(new DataSetColumn("dossierNumber", "dossierNumber", String.class), o[2]);
            row.addColumnValue(new DataSetColumn("patientFirstName", "patientFirstName", String.class), o[3]);
            row.addColumnValue(new DataSetColumn("patientLastName", "patientLastName", String.class), o[4]);
            row.addColumnValue(new DataSetColumn("consultationLocation", "consultationLocation", String.class), o[5]);
            row.addColumnValue(new DataSetColumn("consultationLocationUuid", "consultationLocationUuid", String.class), o[6]);
            row.addColumnValue(new DataSetColumn("consultationDateTime", "consultationDateTime", String.class), o[7]);
            row.addColumnValue(new DataSetColumn("providerFirstName", "providerFirstName", String.class), o[8]);
            row.addColumnValue(new DataSetColumn("providerLastName", "providerLastName", String.class), o[9]);
            row.addColumnValue(new DataSetColumn("admissionLocationUuid", "admissionLocationUuid", String.class), o[10]);
            row.addColumnValue(new DataSetColumn("diagnosis", "diagnosis", String.class), o[11]);
            row.addColumnValue(new DataSetColumn("visitId", "visitId", String.class), o[12]);
            dataSet.addRow(row);
        }
        return dataSet;
    }
}
