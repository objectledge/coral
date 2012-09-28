select
  ad.name,
  rc.db_table_name,
  ad.attribute_definition_id
from 
  coral_attribute_class ac
  join coral_attribute_definition ad using (attribute_class_id)
  join coral_resource_class rc using (resource_class_id)
where ac.attribute_class_id = %d
and rc.db_table_name is not null
