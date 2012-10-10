package com.taskadapter.connector.definition;

import com.taskadapter.model.GTaskDescriptor;
import com.taskadapter.model.GTaskDescriptor.FIELD;

/**
 * Mappings factory. Provides canonical and simple ways to create a mappings.
 */
public final class MappingFactory {
    private MappingFactory() {
        throw new UnsupportedOperationException("Cannot create UTILITY class");
    }

    /**
     * Creates a new mappings with a passed fields set as "enabled". No "map to"
     * value is set for any of this fields.
     * 
     * @param fields
     *            fields to set as "enabled" in created mappings.
     * @return mappings with required fields selected but not "mapped to".
     */
    public static Mappings createWithEnabled(GTaskDescriptor.FIELD... fields) {
        final Mappings result = new Mappings();
        for (FIELD field : fields) {
            result.setFieldSelected(field, true);
        }
        return result;
    }
}
