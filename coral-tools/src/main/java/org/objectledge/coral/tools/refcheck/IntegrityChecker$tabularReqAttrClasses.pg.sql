select 
  rc.resource_class_id,
  rc.db_table_name
from 
  coral_resource_class rc
  join coral_attribute_definition ad using (resource_class_id)
where 
  rc.db_table_name is not null
  and ad.flags & 1 = 1
group by  
  rc.resource_class_id,
  rc.db_table_name
