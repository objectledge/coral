select
  tr.resource_id,
  rc.resource_class_id,
  %d attribute_definition_id,
  ar.resource_id,
  ar.resource_class_id 
from
  %s tr 
  join coral_resource r using (resource_id)
  join coral_resource_class rc using (resource_class_id)
  join coral_resource ar on (tr.%s = ar.resource_id)
where
  ar.resource_class_id not in %s
