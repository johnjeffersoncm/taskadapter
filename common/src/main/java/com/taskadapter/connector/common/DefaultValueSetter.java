package com.taskadapter.connector.common;

import com.google.common.base.Strings;
import com.taskadapter.model.GTask;
import com.taskadapter.model.GTaskDescriptor;
import com.taskadapter.model.GUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * When saving a task, we need to set some of its fields to some default value if there is nothing there yet.
 * E.g. "environment" field can be set as required in Jira and then saving data to Jira will fail
 * if the source data does not contain that info. To fix this, we have "default value if empty" column on
 * "Task Fields Mapping" panel.
 * <p>
 * This class sets those default values to empty fields.
 */
public class DefaultValueSetter {
    // TODO REVIEW SimpleDateFormat instances are not thread-safe. You could find proper approach in some connectors or libraries (like redmine-java-api)
    /**
     * Format for dates in "default value if empty " fields on "Task Fields Mapping" panel.
     */
    private static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyy MM dd");

    private Map<String, String> fieldNameToDefaultValue;

    /**
     * Set default values for fields that are "empty".
     *
     * @param fieldNameToDefaultValue map: field name -> default value for that field if original value is empty.
     */
    public DefaultValueSetter(Map<String, String> fieldNameToDefaultValue) {
        this.fieldNameToDefaultValue = fieldNameToDefaultValue;
    }

    // TODO REVIEW Method name is misleading and do not match its implementation.
    // Notably, if task have fields not preset in mappings, then that field would not be copied.
    // However, method's name implies cloning (i.e. completely replicating original data)
    // and then replacing selected empty fields with default values.
    public GTask cloneAndReplaceEmptySelectedFieldsWithDefaultValues(GTask task) throws ParseException {
        final GTask result = new GTask(task);
        for (Map.Entry<String, String> entry : fieldNameToDefaultValue.entrySet()) {
            String fieldName = entry.getKey();
//            GTaskDescriptor.FIELD field = GTaskDescriptor.FIELD.valueOf(fieldName.toUpperCase());
            Object currentFieldValue = task.getValue(fieldName);
            if (fieldIsConsideredEmpty(currentFieldValue)) {
                Object valueWithProperType = getValueWithProperType(fieldName, entry.getValue());
                result.setValue(fieldName, valueWithProperType);
            }
        }
        return result;
    }

    private boolean fieldIsConsideredEmpty(Object value) {
        return (value == null)
                || ((value instanceof String) && ((String) value).isEmpty());
    }

    private Object getValueWithProperType(String field, String value) throws ParseException {
        try {
            GTaskDescriptor.FIELD enumElement = GTaskDescriptor.FIELD.valueOf(field);
            return getValueWithProperType(enumElement, value);
        } catch (Exception e) {
            return value; // string by default
        }
    }
        // TODO REVIEW Method's name is confusing. It assumes that there was/is/will be a "getValueWithImproperType" method.
    // The documentation for this method neither describes a difference between proper and improper types nor points to "getValueWithImproperType" method.
    private Object getValueWithProperType(GTaskDescriptor.FIELD field, String value) throws ParseException {
        // TODO REVIEW Should this code be polymorphic and belong to a GTaskDespciptor.FIELD instances? It would be more extensible. Same for fieldIsConsideredEmpty.
        switch (field) {
            case ASSIGNEE:
                return new GUser(value);
            case START_DATE:
                return parseDate(value);
            case DUE_DATE:
                return parseDate(value);
            case ESTIMATED_TIME:
                return parseFloat(value);
            case DONE_RATIO:
                return parseFloat(value);
            case PRIORITY:
                return parseFloat(value);
        }
        return value;
    }

    // TODO REVIEW What about documenting behaviour for the incorrect values? Do you ensure in a UI that default value is valid for the given type?
    // Have you considered a format where "default value" would be stored in an appropriate (non-string) format
    // which guarantees that "value" is always correct? This would involve some polymorphism and extensions in the
    // GTaskDescriptor.FIELD and (de)serialization exceptions (especially backward compatibility).
    private Float parseFloat(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Float.parseFloat(value);
    }

    private Date parseDate(String value) throws ParseException {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return DATE_PARSER.parse(value);
    }
}
