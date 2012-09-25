select 
  rc.name, 
  ad.name, 
  am.fk_table
from 
  coral_resource_class rc, 
  coral_attribute_definition ad, 
  coral_attribute_mapping am 
where 
  -- builtint classes don't have custom non fk attributes
  ad.resource_class_id = rc.resource_class_id
  and ad.attribute_class_id = am.attribute_class_id
  and am.is_custom and not am.is_fk
order by 
  rc.name, 
  ad.name
