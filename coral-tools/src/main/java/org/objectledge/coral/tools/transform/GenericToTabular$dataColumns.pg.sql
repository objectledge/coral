select 
  rc.name, 
  ad.name, 
  am.sql_type 
from 
  coral_resource_class rc, 
  coral_attribute_definition ad, 
  coral_attribute_mapping am 
where rc.flags & 4 = 0 -- ignore builtin classes
  and ad.resource_class_id = rc.resource_class_id
  and ad.attribute_class_id = am.attribute_class_id
order by 
  rc.name, 
  ad.name
