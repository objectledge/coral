-- resource class inheritance -----------------------------------------------

ALTER TABLE coral_resource_class_inheritance 
        ADD CONSTRAINT coral_resource_class_inheritance_parent_fkey FOREIGN KEY (parent)
        REFERENCES coral_resource_class (resource_class_id);

ALTER TABLE coral_resource_class_inheritance
        ADD CONSTRAINT coral_resource_class_inheritance_child_fkey FOREIGN KEY (child)
        REFERENCES coral_resource_class (resource_class_id);

-- attribute definition -----------------------------------------------------

ALTER TABLE coral_attribute_definition 
        ADD CONSTRAINT coral_attribute_definition_resource_class_id_fkey FOREIGN KEY (resource_class_id)
        REFERENCES coral_resource_class (resource_class_id);

ALTER TABLE coral_attribute_definition
        ADD CONSTRAINT coral_attribute_definition_attribute_class_id_fkey FOREIGN KEY (attribute_class_id)
        REFERENCES coral_attribute_class (attribute_class_id);

-- role implication ---------------------------------------------------------

ALTER TABLE coral_role_implication 
        ADD CONSTRAINT coral_role_implication_super_role_fkey FOREIGN KEY (super_role)
        REFERENCES coral_role (role_id);

ALTER TABLE coral_role_implication
        ADD CONSTRAINT coral_role_implication_sub_role_fkey FOREIGN KEY (sub_role)
        REFERENCES coral_role (role_id);

-- role assignment ----------------------------------------------------------

ALTER TABLE coral_role_assignment
        ADD CONSTRAINT coral_role_assignment_subject_id_fkey FOREIGN KEY (subject_id)
        REFERENCES coral_subject(subject_id);

ALTER TABLE coral_role_assignment
        ADD CONSTRAINT coral_role_assignment_role_id_fkey FOREIGN KEY (role_id)
        REFERENCES coral_role(role_id);

ALTER TABLE coral_role_assignment
        ADD CONSTRAINT coral_role_assignment_grantor_fkey FOREIGN KEY (grantor)
        REFERENCES coral_subject(subject_id);

-- resource -----------------------------------------------------------------

ALTER TABLE coral_resource
        ADD CONSTRAINT coral_resource_resource_class_id_fkey FOREIGN KEY (resource_class_id)
        REFERENCES coral_resource_class (resource_class_id);

ALTER TABLE coral_resource
        ADD CONSTRAINT coral_resource_parent_fkey FOREIGN KEY (parent)
        REFERENCES coral_resource (resource_id);

ALTER TABLE coral_resource
        ADD CONSTRAINT coral_resource_created_by_fkey FOREIGN KEY (created_by)
        REFERENCES coral_subject (subject_id);

ALTER TABLE coral_resource
        ADD CONSTRAINT coral_resource_owned_by_fkey FOREIGN KEY (owned_by)
        REFERENCES coral_subject (subject_id);

ALTER TABLE coral_resource
        ADD CONSTRAINT coral_resource_modified_by_fkey FOREIGN KEY (modified_by)
        REFERENCES coral_subject (subject_id);

-- relation -----------------------------------------------------------------

ALTER TABLE coral_relation_data
        ADD CONSTRAINT coral_relation_data_relation_id_fkey FOREIGN KEY (relation_id)
        REFERENCES coral_relation (relation_id);

ALTER TABLE coral_relation_data
        ADD CONSTRAINT coral_relation_data_resource1_fkey FOREIGN KEY (resource1)
        REFERENCES coral_resource (resource_id);

ALTER TABLE coral_relation_data
        ADD CONSTRAINT coral_relation_data_resource2_fkey FOREIGN KEY (resource2)
        REFERENCES coral_resource (resource_id);

