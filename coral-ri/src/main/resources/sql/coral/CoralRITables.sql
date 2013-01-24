-- 
-- Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
-- All rights reserved. 
-- 
-- Redistribution and use in source and binary forms, with or without modification,  
-- are permitted provided that the following conditions are met: 
-- 
-- * Redistributions of source code must retain the above copyright notice,  
--   this list of conditions and the following disclaimer. 
-- * Redistributions in binary form must reproduce the above copyright notice,  
--   this list of conditions and the following disclaimer in the documentation  
--   and/or other materials provided with the distribution. 
-- * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
--   nor the names of its contributors may be used to endorse or promote products  
--   derived from this software without specific prior written permission. 
-- 
-- THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
-- AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
-- WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
-- IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
-- INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
-- BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
-- OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
-- WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
-- ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
-- POSSIBILITY OF SUCH DAMAGE. 
-- 

--
-- Coral RI database schema
--
-- $Id$
--

-- CoralSchema --------------------------------------------------------------

CREATE TABLE coral_attribute_class (
        attribute_class_id BIGINT NOT NULL,
        name VARCHAR(255) NOT NULL,
        java_class_name VARCHAR(255) NOT NULL,
        handler_class_name VARCHAR(255) NOT NULL,
        db_table_name VARCHAR(255),
        PRIMARY KEY(attribute_class_id)
);

CREATE SEQUENCE coral_attribute_class_seq;

CREATE TABLE coral_resource_class (
        resource_class_id BIGINT NOT NULL,
        name VARCHAR(255) NOT NULL,
        java_class_name VARCHAR(255) NOT NULL,
        handler_class_name VARCHAR(255),
        db_table_name VARCHAR(255),
        flags INTEGER DEFAULT 0 NOT NULL,
        PRIMARY KEY (resource_class_id)
);

CREATE SEQUENCE coral_resource_class_seq;

CREATE TABLE coral_resource_class_inheritance (
        parent BIGINT NOT NULL,
        child BIGINT NOT NULL,
        PRIMARY KEY (parent, child)
);

CREATE TABLE coral_attribute_definition (
        attribute_definition_id BIGINT NOT NULL,
        resource_class_id BIGINT NOT NULL,
        attribute_class_id BIGINT NOT NULL,
        db_column VARCHAR(255),
        domain VARCHAR(255),
        name VARCHAR(32) NOT NULL,
        flags INTEGER DEFAULT 0 NOT NULL,
        PRIMARY KEY (attribute_definition_id)
);

CREATE SEQUENCE coral_attribute_definition_seq;

-- CoralSecrity -------------------------------------------------------------
        
CREATE TABLE coral_subject (
        subject_id BIGINT NOT NULL,
        name VARCHAR(255),
        PRIMARY KEY (subject_id)
);

CREATE UNIQUE INDEX coral_subject_name ON coral_subject (name);

CREATE SEQUENCE coral_subject_seq;

CREATE TABLE coral_role (
        role_id BIGINT NOT NULL,
        name VARCHAR(255),
        PRIMARY KEY (role_id)
);

CREATE SEQUENCE coral_role_seq;

CREATE TABLE coral_role_implication (
        super_role BIGINT NOT NULL,
        sub_role BIGINT NOT NULL,
        PRIMARY KEY (super_role, sub_role)
);

CREATE TABLE coral_role_assignment (
        subject_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL,
        grantor BIGINT NOT NULL,
        grant_time TIMESTAMP NOT NULL,
        granting_allowed BOOLEAN,
        PRIMARY KEY (subject_id, role_id)
);

CREATE TABLE coral_permission (
        permission_id BIGINT NOT NULL,
        name VARCHAR(255),
        PRIMARY KEY (permission_id)
);

CREATE SEQUENCE coral_permission_seq;

-- CoralStore ---------------------------------------------------------------

CREATE TABLE coral_resource (
        resource_id BIGINT NOT NULL,
        resource_class_id BIGINT NOT NULL,
        parent BIGINT,
        name VARCHAR(255) NOT NULL,
        created_by BIGINT NOT NULL,
        creation_time TIMESTAMP NOT NULL,
        owned_by BIGINT NOT NULL,
        modified_by BIGINT,
        modification_time TIMESTAMP,
        PRIMARY KEY (resource_id)
);

CREATE INDEX coral_resource_parent ON coral_resource (parent);

CREATE INDEX coral_resource_name ON coral_resource (name);

CREATE INDEX coral_resource_parent_name ON coral_resource (parent, name);

CREATE INDEX coral_resource_created_by ON coral_resource (created_by);

CREATE SEQUENCE coral_resource_seq;

CREATE TABLE coral_permission_assignment (
        resource_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL,
        permission_id BIGINT NOT NULL,
        is_inherited BOOLEAN NOT NULL,
        grantor BIGINT NOT NULL,
        grant_time TIMESTAMP NOT NULL,
        PRIMARY KEY (resource_id, role_id, permission_id)
);

CREATE TABLE coral_permission_association (
        resource_class_id BIGINT NOT NULL,
        permission_id BIGINT NOT NULL,
        PRIMARY KEY (resource_class_id, permission_id)
);

-- CoralRelation ---------------------------------------------------------------

CREATE TABLE coral_relation (
        relation_id BIGINT NOT NULL,
        name VARCHAR(255),
        PRIMARY KEY (relation_id)
);

CREATE SEQUENCE coral_relation_seq;

CREATE TABLE coral_relation_data (
        relation_id BIGINT NOT NULL,
        resource1 BIGINT,
        resource2 BIGINT,
        PRIMARY KEY (relation_id, resource1, resource2)
);


