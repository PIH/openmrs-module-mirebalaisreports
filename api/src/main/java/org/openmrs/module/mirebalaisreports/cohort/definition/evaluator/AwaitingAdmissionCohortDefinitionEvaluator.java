package org.openmrs.module.mirebalaisreports.cohort.definition.evaluator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.mirebalaisreports.cohort.definition.AwaitingAdmissionCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Handler(supports = AwaitingAdmissionCohortDefinition.class)
public class AwaitingAdmissionCohortDefinitionEvaluator  implements CohortDefinitionEvaluator {

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
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        AwaitingAdmissionCohortDefinition cd = (AwaitingAdmissionCohortDefinition) cohortDefinition;
        Location location = cd.getLocation();

        Location visitLocation = null ;
        if (location != null ) {
            visitLocation = adtService.getLocationThatSupportsVisits(location);
        }
        EncounterType admissionEncounterType = emrApiProperties.getAdmissionEncounterType();

        Disposition admitToHospital = Context.getService(DispositionService.class).getDispositionByUniqueId("admitToHospital");
        Concept admissionDispositionConcept = null;
        String conceptCode = admitToHospital.getConceptCode();
        if ( StringUtils.isNotBlank(conceptCode) ) {
            String[] conceptMap = conceptCode.split(":");
            if ( (conceptMap !=null) && (conceptMap.length == 2) ) {
                admissionDispositionConcept = conceptService.getConceptByMapping(conceptMap[1], conceptMap[0]);
            }
        }

        Integer dispositionConceptId = Context.getService(DispositionService.class).getDispositionDescriptor().getDispositionConcept().getId();
        
        StringBuilder sb = new StringBuilder("select distinct v.patient_id " +
                "from visit v " +
                "inner join encounter dispo_encounter " +
                    "on dispo_encounter.visit_id = v.visit_id and dispo_encounter.voided = 0 " +
                "inner join obs dispo " +
                    "on dispo.encounter_id = dispo_encounter.encounter_id " +
                    "and dispo.concept_id = :dispositionConceptId " +
                    "and dispo.value_coded = :admissionDispositionConceptId " +
                    "and dispo.voided = 0 " +
                "where v.date_stopped is null and v.voided = 0 ");
        if (visitLocation != null) {
            sb.append("  and v.location_id = :visitLocation ");
        }
                    sb.append("and ( " +
                    "select count(*) " +
                    "from encounter admission " +
                    "where admission.visit_id = v.visit_id " +
                        "and admission.encounter_type = :admissionEncounterType " +
                        "and admission.voided = 0 " +
                    ") = 0; ");

        SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sb.toString());
        query.setInteger("admissionEncounterType", admissionEncounterType.getId());
        query.setInteger("dispositionConceptId", dispositionConceptId);
        query.setInteger("admissionDispositionConceptId", admissionDispositionConcept.getId());

        if (visitLocation != null) {
            query.setInteger("visitLocation", visitLocation.getId());
        }

        Cohort c = new Cohort();
        for (Integer i : (List<Integer>) query.list()){
            c.addMember(i);
        }
        return new EvaluatedCohort(c, cohortDefinition, evaluationContext);
    }
}
