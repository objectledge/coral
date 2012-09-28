-- resource class inheritance -----------------------------------------------

ALTER TABLE coral_resource_class_inheritance 
        DROP CONSTRAINT coral_resource_class_inheritance_parent_fkey;

ALTER TABLE coral_resource_class_inheritance
        DROP CONSTRAINT coral_resource_class_inheritance_child_fkey;

-- attribute definition -----------------------------------------------------

ALTER TABLE coral_attribute_definition 
        DROP CONSTRAINT coral_attribute_definition_attribute_class_id_fkey;

ALTER TABLE coral_attribute_definition
        DROP CONSTRAINT coral_attribute_definition_resource_class_id_fkey;

-- role implication ---------------------------------------------------------

ALTER TABLE coral_role_implication 
        DROP CONSTRAINT coral_role_implication_sub_role_fkey;

ALTER TABLE coral_role_implication
        DROP CONSTRAINT coral_role_implication_super_role_fkey;

-- role assignment ----------------------------------------------------------

ALTER TABLE coral_role_assignment
        DROP CONSTRAINT coral_role_assignment_grantor_fkey;

ALTER TABLE coral_role_assignment
        DROP CONSTRAINT coral_role_assignment_role_id_fkey;

ALTER TABLE coral_role_assignment
        DROP CONSTRAINT coral_role_assignment_subject_id_fkey;

-- resource -----------------------------------------------------------------

ALTER TABLE coral_resource
        DROP CONSTRAINT coral_resource_parent_fkey;

ALTER TABLE coral_resource
        DROP CONSTRAINT coral_resource_created_by_fkey;

ALTER TABLE coral_resource
        DROP CONSTRAINT coral_resource_modified_by_fkey;

ALTER TABLE coral_resource
        DROP CONSTRAINT coral_resource_owned_by_fkey;

ALTER TABLE coral_resource
        DROP CONSTRAINT coral_resource_resource_class_id_fkey;

-- relation -----------------------------------------------------------------

ALTER TABLE coral_relation_data
        DROP CONSTRAINT coral_relation_data_relation_id_fkey;

ALTER TABLE coral_relation_data
        DROP CONSTRAINT coral_relation_data_resource1_fkey;

ALTER TABLE coral_relation_data
        DROP CONSTRAINT coral_relation_data_resource2_fkey;

