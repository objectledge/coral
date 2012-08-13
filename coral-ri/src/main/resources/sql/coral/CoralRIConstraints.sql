-- resource class inheritance -----------------------------------------------

ALTER TABLE coral_resource_class_inheritance 
        ADD FOREIGN KEY (parent)
        REFERENCES coral_resource_class (resource_class_id);

ALTER TABLE coral_resource_class_inheritance
        ADD FOREIGN KEY (child)
        REFERENCES coral_resource_class (resource_class_id);

-- attribute definition -----------------------------------------------------

ALTER TABLE coral_attribute_definition 
        ADD FOREIGN KEY (resource_class_id)
        REFERENCES coral_resource_class (resource_class_id);

ALTER TABLE coral_attribute_definition
        ADD FOREIGN KEY (attribute_class_id)
        REFERENCES coral_attribute_class (attribute_class_id);

-- role implication ---------------------------------------------------------

ALTER TABLE coral_role_implication 
        ADD FOREIGN KEY (super_role)
        REFERENCES coral_role (role_id);

ALTER TABLE coral_role_implication
        ADD FOREIGN KEY (sub_role)
        REFERENCES coral_role (role_id);

-- role assignment ----------------------------------------------------------

ALTER TABLE coral_role_assignment
        ADD FOREIGN KEY (subject_id)
        REFERENCES coral_subject(subject_id);

ALTER TABLE coral_role_assignment
        ADD FOREIGN KEY (role_id)
        REFERENCES coral_role(role_id);

ALTER TABLE coral_role_assignment
        ADD FOREIGN KEY (grantor)
        REFERENCES coral_subject(subject_id);

-- resource -----------------------------------------------------------------

ALTER TABLE coral_resource
        ADD FOREIGN KEY (resource_class_id)
        REFERENCES coral_resource_class (resource_class_id);

ALTER TABLE coral_resource
        ADD FOREIGN KEY (parent)
        REFERENCES coral_resource (resource_id);

ALTER TABLE coral_resource
        ADD FOREIGN KEY (created_by)
        REFERENCES coral_subject (subject_id);

ALTER TABLE coral_resource
        ADD FOREIGN KEY (owned_by)
        REFERENCES coral_subject (subject_id);

ALTER TABLE coral_resource
        ADD FOREIGN KEY (modified_by)
        REFERENCES coral_subject (subject_id);

-- relation -----------------------------------------------------------------

ALTER TABLE coral_relation_data
        ADD FOREIGN KEY (relation_id)
        REFERENCES coral_relation (relation_id);

ALTER TABLE coral_relation_data
        ADD FOREIGN KEY (resource1)
        REFERENCES coral_resource (resource_id);

ALTER TABLE coral_relation_data
        ADD FOREIGN KEY (resource2)
        REFERENCES coral_resource (resource_id);

