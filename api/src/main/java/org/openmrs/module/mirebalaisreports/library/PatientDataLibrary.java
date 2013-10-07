package org.openmrs.module.mirebalaisreports.library;

import org.openmrs.Encounter;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.User;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.reporting.common.Birthdate;
import org.openmrs.module.reporting.common.VitalStatus;
import org.openmrs.module.reporting.data.MappedData;
import org.openmrs.module.reporting.data.converter.AgeConverter;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.CountConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.EarliestCreatedConverter;
import org.openmrs.module.reporting.data.converter.MostRecentlyCreatedConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.EncountersForPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PersonToPatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.AgeAtDateOfOtherDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredAddressDataDefinition;
import org.openmrs.module.reporting.data.person.definition.VitalStatusDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 *
 */
@Component
public class PatientDataLibrary extends BaseDefinitionLibrary<PatientDataDefinition> {

    @Autowired
    private MirebalaisReportsProperties mirebalaisReportsProperties;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Override
    public String getKeyPrefix() {
        return "mirebalais.patientDataCalculation.";
    }

    @DocumentedDefinition("patientId")
    public PatientDataDefinition getPatientId() {
        return new PatientIdDataDefinition();
    }

    @DocumentedDefinition("birthdate.ymd")
    public PatientDataDefinition getBirthdateYmd() {
        return getBirthdate(new BirthdateConverter("yyyy-MM-dd"));
    }

    @DocumentedDefinition("birthdate.estimated")
    public PatientDataDefinition getBirthdateEstimated() {
        return getBirthdate(new PropertyConverter(Birthdate.class, "estimated"));
    }

    @DocumentedDefinition("gender")
    public PatientDataDefinition getGender() {
        return new PersonToPatientDataDefinition(new GenderDataDefinition());
    }

    @DocumentedDefinition("numberOfZlEmrIds")
    public PatientDataDefinition getNumberOfZlEmrIds() {
        return getIdentifiersOf(mirebalaisReportsProperties.getZlEmrIdentifierType(), new CountConverter());
    }

    @DocumentedDefinition("numberOfDossierNumbers")
    public PatientDataDefinition getNumberOfDossierNumbers() {
        return getIdentifiersOf(mirebalaisReportsProperties.getDossierNumberIdentifierType(), new CountConverter());
    }

    @DocumentedDefinition("numberOfHivEmrIds")
    public PatientDataDefinition getNumberOfHivEmrIds() {
        return getIdentifiersOf(mirebalaisReportsProperties.getHivEmrIdentifierType(), new CountConverter());
    }

    @DocumentedDefinition("mostRecentZlEmrId.identifier")
    public PatientDataDefinition getMostRecentZlEmrIdIdentifier() {
        return getMostRecentIdentifierOf(
                mirebalaisReportsProperties.getZlEmrIdentifierType(),
                new PropertyConverter(PatientIdentifier.class, "identifier"));
    }

    @DocumentedDefinition("mostRecentZlEmrId.location")
    public PatientDataDefinition getMostRecentZlEmrIdLocation() {
        return getMostRecentIdentifierOf(
                mirebalaisReportsProperties.getZlEmrIdentifierType(),
                new PropertyConverter(PatientIdentifier.class, "location"),
                new ObjectFormatter());
    }

    @DocumentedDefinition("mostRecentDossierNumber.identifier")
    public PatientDataDefinition getMostRecentDossierNumberIdentifier() {
        return getMostRecentIdentifierOf(
                mirebalaisReportsProperties.getDossierNumberIdentifierType(),
                new PropertyConverter(PatientIdentifier.class, "identifier"));
    }

    @DocumentedDefinition("mostRecentHivEmrId.identifier")
    public PatientDataDefinition getMostRecentHivEmrIdIdentifier() {
        return getMostRecentIdentifierOf(
                mirebalaisReportsProperties.getHivEmrIdentifierType(),
                new PropertyConverter(PatientIdentifier.class, "identifier"));
    }

    @DocumentedDefinition("unknownPatient.value")
    public PatientDataDefinition getUnknownPatient() {
        return new ConvertedPatientDataDefinition(
                new PersonToPatientDataDefinition(
                        new PersonAttributeDataDefinition(emrApiProperties.getUnknownPatientPersonAttributeType())),
                new PropertyConverter(PersonAttribute.class, "value"));
    }

    @DocumentedDefinition("vitalStatus.dead")
    public PatientDataDefinition getVitalStatusDead() {
        return getVitalStatus(new PropertyConverter(VitalStatus.class, "dead"));
    }

    @DocumentedDefinition("vitalStatus.deathDate")
    public PatientDataDefinition getVitalStatusDeathDate() {
        return getVitalStatus(new PropertyConverter(VitalStatus.class, "deathDate"));
    }

    @DocumentedDefinition("preferredAddress.department")
    public PatientDataDefinition getPreferredAddressDepartment() {
        return getPreferredAddress("stateProvince");
    }

    @DocumentedDefinition("preferredAddress.commune")
    public PatientDataDefinition getPreferredAddressCommune() {
        return getPreferredAddress("cityVillage");
    }

    @DocumentedDefinition("preferredAddress.section")
    public PatientDataDefinition getPreferredAddressSection() {
        return getPreferredAddress("address3");
    }

    @DocumentedDefinition("preferredAddress.locality")
    public PatientDataDefinition getPreferredAddressLocality() {
        return getPreferredAddress("address1");
    }

    @DocumentedDefinition("preferredAddress.streetLandmark")
    public PatientDataDefinition getPreferredAddressStreetLandmark() {
        return getPreferredAddress("address2");
    }

    @DocumentedDefinition("registration.encounterDatetime")
    public PatientDataDefinition getRegistrationDatetime() {
        return getRegistrationEncounter(new PropertyConverter(Encounter.class, "encounterDatetime"));
    }

    @DocumentedDefinition("registration.location")
    public PatientDataDefinition getRegistrationLocation() {
        return getRegistrationEncounter(new PropertyConverter(Encounter.class, "location"),
                new ObjectFormatter());
    }

    @DocumentedDefinition("registration.creator.name")
    public PatientDataDefinition getRegistrationCreatorName() {
        return getRegistrationEncounter(new PropertyConverter(Encounter.class, "creator"),
                new PropertyConverter(User.class, "personName"),
                new ObjectFormatter("{givenName} {familyName}"));
    }

    @DocumentedDefinition("registration.age")
    public PatientDataDefinition getRegistrationAge() {
        MappedData<PatientDataDefinition> effectiveDate = new MappedData<PatientDataDefinition>(getRegistrationDatetime(), null);

        AgeAtDateOfOtherDataDefinition ageAtRegistration = new AgeAtDateOfOtherDataDefinition();
        ageAtRegistration.setEffectiveDateDefinition(effectiveDate);

        return new ConvertedPatientDataDefinition(new PersonToPatientDataDefinition(ageAtRegistration), new AgeConverter("{y:1}"));
    }

    private PatientDataDefinition getBirthdate(DataConverter... converters) {
        return new ConvertedPatientDataDefinition(
                new PersonToPatientDataDefinition(
                        new BirthdateDataDefinition()),
                converters);
    }

    private PatientDataDefinition getIdentifiersOf(PatientIdentifierType patientIdentifierType, DataConverter... converters) {
        return new ConvertedPatientDataDefinition(
                new PatientIdentifierDataDefinition(null, patientIdentifierType),
                converters);
    }

    private PatientDataDefinition getMostRecentIdentifierOf(PatientIdentifierType patientIdentifierType, DataConverter... converters) {
        return getIdentifiersOf(patientIdentifierType,
                converters(new MostRecentlyCreatedConverter(PatientIdentifier.class), converters));
    }

    private PatientDataDefinition getVitalStatus(DataConverter... converters) {
        return new ConvertedPatientDataDefinition(
                new PersonToPatientDataDefinition(
                        new VitalStatusDataDefinition()),
                converters);
    }

    private PatientDataDefinition getPreferredAddress(String property) {
        return new ConvertedPatientDataDefinition(
                new PersonToPatientDataDefinition(
                        new PreferredAddressDataDefinition()),
                new PropertyConverter(PersonAddress.class, property));
    }

    private PatientDataDefinition getRegistrationEncounter(DataConverter... converters) {
        EncountersForPatientDataDefinition registrationEncounters = new EncountersForPatientDataDefinition();
        registrationEncounters.setTypes(Arrays.asList(mirebalaisReportsProperties.getRegistrationEncounterType()));

        return new ConvertedPatientDataDefinition(registrationEncounters,
                converters(new EarliestCreatedConverter(Encounter.class), converters));
    }

}
