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
-- Coral Datatypes - initial data
--
-- $Id$
--

-- Attribute Class definitions ----------------------------------------------

-- -- primitives ------------------------------------------------------------

INSERT INTO coral_attribute_class VALUES(
		1, 
		'string',
		'java.lang.String', 
		'org.objectledge.coral.datatypes.StringAttributeHandler',
		'coral_attribute_string');
		
INSERT INTO coral_attribute_class VALUES(
		2, 
		'text',
		'java.lang.String', 
		'org.objectledge.coral.datatypes.TextAttributeHandler',
		'coral_attribute_text');
		
INSERT INTO coral_attribute_class VALUES(
		3, 
		'boolean',
		'java.lang.Boolean', 
		'org.objectledge.coral.datatypes.BooleanAttributeHandler',
		'coral_attribute_boolean');

INSERT INTO coral_attribute_class VALUES(
		4, 
		'integer',
		'java.lang.Integer', 
		'org.objectledge.coral.datatypes.IntegerAttributeHandler',
		'coral_attribute_integer');
		
INSERT INTO coral_attribute_class VALUES(
		5, 
		'long',
		'java.lang.Long', 
		'org.objectledge.coral.datatypes.LongAttributeHandler',
		'coral_attribute_long');

INSERT INTO coral_attribute_class VALUES(
		6, 
		'long',
		'java.lang.Number', 
		'org.objectledge.coral.datatypes.NumberAttributeHandler',
		'coral_attribute_number');

INSERT INTO coral_attribute_class VALUES(
		7, 
		'date',
		'java.util.Date', 
		'org.objectledge.coral.datatypes.DateAttributeHandler',
		'coral_attribute_date');
		
-- -- coral entities --------------------------------------------------------

INSERT INTO coral_attribute_class VALUES(
		8, 
		'resource_class',
		'org.objectledge.coral.schema.ResourceClass', 
		'org.objectledge.coral.datatypes.ResourceClassAttributeHandler',
		'coral_attribute_resource_class');

INSERT INTO coral_attribute_class VALUES(
		9, 
		'resource',
		'org.objectledge.coral.store.Resource', 
		'org.objectledge.coral.datatypes.ResourceAttributeHandler',
		'coral_attribute_resource');

INSERT INTO coral_attribute_class VALUES(
		10, 
		'subject',
		'org.objectledge.coral.security.Subject', 
		'org.objectledge.coral.datatypes.SubjectAttributeHandler',
		'coral_attribute_subject');

INSERT INTO coral_attribute_class VALUES(
		11, 
		'role',
		'org.objectledge.coral.security.Role', 
		'org.objectledge.coral.datatypes.RoleAttributeHandler',
		'coral_attribute_role');

INSERT INTO coral_attribute_class VALUES(
		12, 
		'permission',
		'org.objectledge.coral.security.Permission', 
		'org.objectledge.coral.datatypes.PermissionAttributeHandler',
		'coral_attribute_permission');

-- -- custom objects --------------------------------------------------------

INSERT INTO coral_attribute_class VALUES(
		13, 
		'date_range',
		'org.objectledge.coral.datatypes.DateRange', 
		'org.objectledge.coral.datatypes.DateRangeAttributeHandler',
		'coral_attribute_date_range');

INSERT INTO coral_attribute_class VALUES(
		14, 
		'parameters',
		'org.objectledge.parameters.Parameters', 
		'org.objectledge.coral.datatypes.ParametersAttributeHandler',
		'coral_attribute_parameters');

INSERT INTO coral_attribute_class VALUES(
		15, 
		'resource_list',
		'org.objectledge.coral.datatypes.ResourceList', 
		'org.objectledge.coral.datatypes.ResourceListAttributeHandler',
		'coral_attribute_resource_list');

INSERT INTO coral_attribute_class VALUES(
		16, 
		'weak_resource_list',
		'org.objectledge.coral.datatypes.WeakResourceList', 
		'org.objectledge.coral.datatypes.WeakResourceListAttributeHandler',
		'coral_attribute_weak_resource_list');
		
INSERT INTO ledge_id_table VALUES(17, 'coral_attribute_class');

-- resource classes ----------------------------------------------------------

INSERT INTO coral_resource_class VALUES(
		1, 
		'node', 
        'org.objectledge.coral.datatypes.NodeResourceImpl', 
        'org.objectledge.coral.datatypes.GenericResourceHandler',
        '',
        4);
        
INSERT INTO ledge_id_table VALUES (2, 'coral_resource_class');

-- -- builtin attributes -----------------------------------------------------

INSERT INTO coral_attribute_definition VALUES(
		1,
		1,
		5,
		NULL,
		'id',
		1+8);

INSERT INTO coral_attribute_definition VALUES(
		2,
		1,
		1,
		NULL,
		'name',
		1+8);

INSERT INTO coral_attribute_definition VALUES(
		3,
		1,
		1,
		NULL,
		'path',
		1+8+128);

INSERT INTO coral_attribute_definition VALUES(
		4,
		1,
		9,
		NULL,
		'parent',
		8);

INSERT INTO coral_attribute_definition VALUES(
		5,
		1,
		8,
		NULL,
		'resource_class',
		1+8);

INSERT INTO coral_attribute_definition VALUES(
		6,
		1,
		10,
		NULL,
		'owner',
		1+8);

INSERT INTO coral_attribute_definition VALUES(
		7,
		1,
		10,
		NULL,
		'created_by',
		1+8);

INSERT INTO coral_attribute_definition VALUES(
		8,
		1,
		10,
		NULL,
		'modified_by',
		8);

INSERT INTO coral_attribute_definition VALUES(
		9,
		1,
		7,
		NULL,
		'creation_date',
		1+8);

INSERT INTO coral_attribute_definition VALUES(
		10,
		1,
		7,
		NULL,
		'modification_date',
		8);

-- -- description ------------------------------------------------------------

INSERT INTO coral_attribute_definition VALUES(
		11,
		1,
		1,
		NULL,
		'description',
		4);

INSERT INTO ledge_id_table VALUES (12, 'coral_attribute_definition');

-- root resource -------------------------------------------------------------

INSERT INTO coral_resource values(1, 1, NULL, 'root' ,1 ,NOW(), 1, 1, NOW());
INSERT INTO ledge_id_table VALUES (2, 'coral_resource');

