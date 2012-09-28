select 
  ac.db_table_name, 
  case 
    when am.is_fk OR am.is_custom then 'ref' 
    else 'data' 
  end col 
from 
  coral_attribute_class ac join
  coral_attribute_mapping am using(attribute_class_id)
where 
  not coalesce(am.is_custom, false) or 
  ac.name like '%list%'

