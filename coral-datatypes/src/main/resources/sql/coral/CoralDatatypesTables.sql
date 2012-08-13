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
-- Coral Datatypes database schema
--
-- $Id: arl_tables.sql,v 1.24 2003/11/10 22:56:32 pablo Exp $
--

-- GenericResource -----------------------------------------------------------

CREATE TABLE coral_generic_resource (
        resource_id BIGINT NOT NULL,
        attribute_definition_id BIGINT NOT NULL,
        data_key BIGINT NOT NULL,
        PRIMARY KEY (resource_id, attribute_definition_id)
);

-- Basic attributes ----------------------------------------------------------

CREATE TABLE coral_attribute_boolean (
        data_key BIGINT NOT NULL,
        data INTEGER,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_integer (
        data_key BIGINT NOT NULL,
        data INTEGER,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_long (
        data_key BIGINT NOT NULL,
        data BIGINT,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_number (
        data_key BIGINT NOT NULL,
        data DECIMAL,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_string (
        data_key BIGINT NOT NULL,
        data VARCHAR(255),
        PRIMARY KEY (data_key)
);

-- PostgreSQL, HSQLDB accept VARCHAR
-- PostgreSQL, MySQL accept TEXT

CREATE TABLE coral_attribute_text (
        data_key BIGINT NOT NULL,
        data VARCHAR,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_date (
        data_key BIGINT NOT NULL,
        data TIMESTAMP,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_date_range (
        data_key BIGINT NOT NULL,
        start_date TIMESTAMP,
        end_date TIMESTAMP,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_resource_class (
        data_key BIGINT NOT NULL,
        ref BIGINT,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_resource (
        data_key BIGINT NOT NULL,
        ref BIGINT,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_subject (
        data_key BIGINT NOT NULL,
        ref BIGINT,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_role (
        data_key BIGINT NOT NULL,
        ref BIGINT,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_permission (
        data_key BIGINT NOT NULL,
        ref BIGINT,
        PRIMARY KEY (data_key)
);

CREATE TABLE coral_attribute_resource_list (
        data_key BIGINT NOT NULL,
        pos INTEGER NOT NULL,
        ref BIGINT,
        PRIMARY KEY (data_key, pos)
);
        
CREATE TABLE coral_attribute_weak_resource_list (
        data_key BIGINT NOT NULL,
        pos INTEGER NOT NULL,
        ref BIGINT,
        PRIMARY KEY (data_key, pos)
);

-- attribute to SQL mapping for tabular model -------------------------------

CREATE TABLE coral_attribute_mapping (
  attribute_class_id BIGINT NOT NULL,
  is_fk boolean NOT NULL,
  sql_type VARCHAR(32),
  fk_table VARCHAR(64),
  fk_key_column VARCHAR(64)
);

