select
  r1.resource_id,
  r1.resource_class_id,
  g.attribute_definition_id,
  r2.resource_id,
  r2.resource_class_id
from 
  coral_resource r1 join
  coral_generic_resource g using (resource_id) join
  coral_attribute_resource a using (data_key) join
  coral_resource r2 on (a.ref = r2.resource_id)
where  
  r1.resource_class_id in %s and
  g.attribute_definition_id = %d and
  r2.resource_class_id not in %s
