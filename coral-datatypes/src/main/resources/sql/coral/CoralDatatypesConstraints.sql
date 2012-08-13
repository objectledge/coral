-- generic resource ---------------------------------------------------------

ALTER TABLE coral_generic_resource 
        ADD FOREIGN KEY (attribute_definition_id)
        REFERENCES coral_attribute_definition (attribute_definition_id);

ALTER TABLE coral_generic_resource
        ADD FOREIGN KEY (resource_id)
        REFERENCES coral_resource (resource_id);

-- resource class attribute

ALTER TABLE coral_attribute_resource_class 
        ADD FOREIGN KEY (ref)
        REFERENCES coral_resource_class (resource_class_id);

-- resource attribute

ALTER TABLE coral_attribute_resource 
        ADD FOREIGN KEY (ref)
        REFERENCES coral_resource (resource_id);

-- subject attribute 
        
ALTER TABLE coral_attribute_subject 
        ADD FOREIGN KEY (ref)
        REFERENCES coral_subject (subject_id);

-- role attribute
        
ALTER TABLE coral_attribute_role 
        ADD FOREIGN KEY (ref)
        REFERENCES coral_role (role_id);

-- permission attribute        

ALTER TABLE coral_attribute_permission 
        ADD FOREIGN KEY (ref)
        REFERENCES coral_permission (permission_id);

-- resource list attribute

ALTER TABLE coral_attribute_resource_list
        ADD FOREIGN KEY (ref)
        REFERENCES coral_resource (resource_id);


