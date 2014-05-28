package org.openmrs.module.mirebalaisreports.library;

import org.openmrs.PersonAttribute;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonDataLibrary extends BaseDefinitionLibrary<PersonDataDefinition> {

    @Autowired
    private MirebalaisReportsProperties mirebalaisReportsProperties;


    @Override
    public Class<? super PersonDataDefinition> getDefinitionType() {
        return PersonDataDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return "mirebalais.personDataCalculation.";
    }

    @DocumentedDefinition("telephoneNumber")
    public PersonDataDefinition getTelephoneNumber() {
        return new ConvertedPersonDataDefinition("telephoneNumber.value",
                new PersonAttributeDataDefinition(mirebalaisReportsProperties.getTelephoneNumberPersonAttributeType()),
                new PropertyConverter(PersonAttribute.class, "value"));
    }
}
