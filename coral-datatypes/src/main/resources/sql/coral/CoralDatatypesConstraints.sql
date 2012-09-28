-- generic resource ---------------------------------------------------------

ALTER TABLE coral_generic_resource 
        ADD CONSTRAINT coral_generic_resource_attribute_definition_id_fkey FOREIGN KEY (attribute_definition_id)
        REFERENCES coral_attribute_definition (attribute_definition_id);

ALTER TABLE coral_generic_resource
        ADD CONSTRAINT coral_generic_resource_resource_id_fkey FOREIGN KEY (resource_id)
        REFERENCES coral_resource (resource_id);

-- resource class attribute

ALTER TABLE coral_attribute_resource_class 
        ADD CONSTRAINT coral_attribute_resource_class_ref_fkey FOREIGN KEY (ref)
        REFERENCES coral_resource_class (resource_class_id);

-- resource attribute

ALTER TABLE coral_attribute_resource 
        ADD CONSTRAINT coral_attribute_resource_ref_fkey FOREIGN KEY (ref)
        REFERENCES coral_resource (resource_id);

-- subject attribute 
        
ALTER TABLE coral_attribute_subject 
        ADD CONSTRAINT coral_attribute_subject_ref_fkey FOREIGN KEY (ref)
        REFERENCES coral_subject (subject_id);

-- role attribute
        
ALTER TABLE coral_attribute_role 
        ADD CONSTRAINT coral_attribute_role_ref_fkey FOREIGN KEY (ref)
        REFERENCES coral_role (role_id);

-- permission attribute        

ALTER TABLE coral_attribute_permission 
        ADD CONSTRAINT coral_attribute_permission_ref_fkey FOREIGN KEY (ref)
        REFERENCES coral_permission (permission_id);

-- resource list attribute

ALTER TABLE coral_attribute_resource_list
        ADD CONSTRAINT coral_attribute_resource_list_ref_fkey FOREIGN KEY (ref)
        REFERENCES coral_resource (resource_id);


