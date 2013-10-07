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

package org.openmrs.module.mirebalaisreports.library;

import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementations of this class can conveniently implement on-the-fly-created reporting definitions, with inline
 * documentation on how they'll behave.
 * <br/>
 * Remember to annotate implementations as @Handler(supports = T.class)
 */
public abstract class BaseDefinitionLibrary<T extends Definition> implements DefinitionLibrary<T> {

    public abstract String getKeyPrefix();

    public T getDefinition(String key) {
        String lookFor = key.startsWith(getKeyPrefix()) ? key.substring(getKeyPrefix().length()) : key;
        return findAndInvokeMethod(lookFor);
    }

    private T findAndInvokeMethod(String annotationValue) {
        Method method = findMethod(annotationValue);
        if (method == null) {
            return null;
        }
        return buildDefinition(method);
    }

    private T buildDefinition(Method method) {
        try {
            @SuppressWarnings("unchecked")
            T definition = (T) method.invoke(this);

            DocumentedDefinition documented = method.getAnnotation(DocumentedDefinition.class);
            String key = getKeyPrefix() + documented.value();
            String name = documented.name();
            if (name == null) {
                name = key + ".name";
            }
            String description = documented.definition();
            if (description == null) {
                description = key + ".description";
            }
            definition.setUuid(key);
            definition.setName(name);
            definition.setDescription(description);
            return definition;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Method findMethod(String annotationValue) {
        for (Method candidate : this.getClass().getMethods()) {
            DocumentedDefinition annotation = candidate.getAnnotation(DocumentedDefinition.class);
            if (annotation != null && annotation.value().equals(annotationValue)) {
                return candidate;
            }
        }
        return null;
    }

    public List<T> getAllDefinitions(boolean includeRetired) {
        List<T> definitions = new ArrayList<T>();
        for (Method candidate : this.getClass().getMethods()) {
            DocumentedDefinition annotation = candidate.getAnnotation(DocumentedDefinition.class);
            if (annotation != null) {
                definitions.add(buildDefinition(candidate));
            }
        }
        return definitions;
    }

    public int getNumberOfDefinitions(boolean includeRetired) {
        int count = 0;
        for (Method candidate : this.getClass().getMethods()) {
            DocumentedDefinition annotation = candidate.getAnnotation(DocumentedDefinition.class);
            if (annotation != null) {
                ++count;
            }
        }
        return count;
    }

    protected <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
        if (parameterizable == null) {
            throw new NullPointerException("Programming error: missing parameterizable");
        }
        if (mappings == null) {
            mappings = ""; // probably not necessary, just to be safe
        }
        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
    }

    protected <T extends Parameterizable> Mapped<T> noMappings(T parameterizable) {
        if (parameterizable == null) {
            throw new NullPointerException("Programming error: missing parameterizable");
        }
        return new Mapped<T>(parameterizable, Collections.<String, Object>emptyMap());
    }

    protected DataConverter[] converters(Object... converterOrArray) {
        List<DataConverter> converters = new ArrayList<DataConverter>();
        for (Object o : converterOrArray) {
            if (o instanceof DataConverter) {
                converters.add((DataConverter) o);
            }
            else if (o instanceof DataConverter[]) {
                converters.addAll(Arrays.asList((DataConverter[]) o));
            }
            else if (o == null) {
                continue;
            }
            else {
                throw new IllegalArgumentException("inputs must be DataConverter or DataConverter[]");
            }
        }
        return converters.toArray(new DataConverter[converters.size()]);
    }
}
