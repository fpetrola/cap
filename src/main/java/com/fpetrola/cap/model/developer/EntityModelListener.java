package com.fpetrola.cap.model.developer;

public interface EntityModelListener {
    void propertyCreated(EntityModel model, Property property, PropertyMapping propertyMapping);
}
