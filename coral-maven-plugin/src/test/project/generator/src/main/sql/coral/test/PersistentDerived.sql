-- SQL schema for coral.test.PersistentDerived --

CREATE TABLE persistent_derived (
    persistent_derived_id BIGINT PRIMARY KEY,
    resource_id BIGINT REFERENCES coral_resource(resource_id) NOT NULL,
    i1 INTEGER NOT NULL, 
    s1 VARCHAR(255) NOT NULL
);
