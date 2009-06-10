-- SQL schema for coral.test.Persistent --

CREATE TABLE persistent (
    persistent_id BIGINT PRIMARY KEY,
    resource_id BIGINT REFERENCES coral_resource(resource_id) NOT NULL,
    d1 TIMESTAMP, 
    p1 BIGINT REFERENCES ledge_parameters (parameters_id), 
    res1 BIGINT REFERENCES coral_resource (resource_id)
);
