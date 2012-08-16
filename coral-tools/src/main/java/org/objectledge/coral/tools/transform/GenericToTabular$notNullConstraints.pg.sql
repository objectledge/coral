select 
  rc.name, 
  ad.name,
  am.sql_type 
from 
  coral_resource_class rc, 
  coral_attribute_definition ad,
  coral_attribute_mapping am 
where 
  ad.resource_class_id = rc.resource_class_id 
  and am.attribute_class_id = ad.attribute_class_id
  and (ad.flags & 1) = 1
  and (rc.flags & 4) = 0
