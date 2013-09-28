package org.openmrs.module.mirebalaisreports.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

import java.util.Collection;

/**
 * TODO move this to the reporting module
 */
public class CountConverter implements DataConverter {

    @Override
    public Object convert(Object original) {
        Collection c = (Collection) original;
        return c == null ? 0 : c.size();
    }

    @Override
    public Class<?> getInputDataType() {
        return Collection.class;
    }

    @Override
    public Class<?> getDataType() {
        return Integer.class;
    }
}
