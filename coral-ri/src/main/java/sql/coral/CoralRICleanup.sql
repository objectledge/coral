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
-- Coral RI : cleanup
--
-- $Id$
--

-- cleanup ------------------------------------------------------------------

DELETE FROM coral_permission_assignment;
DELETE FROM coral_permission_association;
DELETE FROM coral_role_assignment;
DELETE FROM coral_role_implication;
DELETE FROM coral_attribute_definition;
DELETE FROM coral_resource_class_inheritance;

-- MySQL: instead of DROP/ADD CONSTRAINT use
-- SET FOREIGN_KEY_CHECKS=0/1

ALTER TABLE coral_resource DROP CONSTRAINT resource_parent_fk;

DELETE FROM coral_resource;

ALTER TABLE coral_resource ADD CONSTRAINT resource_parent_fk 
  FOREIGN KEY (parent) REFERENCES coral_resource (resource_id);

DELETE FROM coral_role;
DELETE FROM coral_subject;
DELETE FROM coral_permission;
DELETE FROM coral_attribute_class;
DELETE FROM coral_resource_class;

DELETE FROM ledge_id_table WHERE table_name = 'coral_role';
DELETE FROM ledge_id_table WHERE table_name = 'coral_subject';
DELETE FROM ledge_id_table WHERE table_name = 'coral_permission';
