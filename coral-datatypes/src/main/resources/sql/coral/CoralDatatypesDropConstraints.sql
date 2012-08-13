-- generic resource ---------------------------------------------------------

ALTER TABLE coral_generic_resource 
        DROP CONSTRAINT coral_generic_resource_resource_id_fkey;

ALTER TABLE coral_generic_resource
        DROP CONSTRAINT coral_generic_resource_attribute_definition_id_fkey;

-- resource class attribute

ALTER TABLE coral_attribute_resource_class 
        DROP CONSTRAINT coral_attribute_resource_class_ref_fkey;

-- resource attribute

ALTER TABLE coral_attribute_resource 
        DROP CONSTRAINT coral_attribute_resource_ref_fkey;

-- subject attribute 
        
ALTER TABLE coral_attribute_subject 
        DROP CONSTRAINT coral_attribute_subject_ref_fkey;

-- role attribute
        
ALTER TABLE coral_attribute_role 
        DROP CONSTRAINT coral_attribute_role_ref_fkey;

-- permission attribute        

ALTER TABLE coral_attribute_permission 
        DROP CONSTRAINT coral_attribute_permission_ref_fkey;

-- resource list attribute

ALTER TABLE coral_attribute_resource_list
        DROP CONSTRAINT coral_attribute_resource_list_ref_fkey;


