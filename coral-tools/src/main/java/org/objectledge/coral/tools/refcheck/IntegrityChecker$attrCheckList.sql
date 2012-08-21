select 
  ac.attribute_class_id,
  ac.db_table_name, 
  case 
    when am.is_custom then am.fk_key_column
    else 'data_key'
  end key_column
from 
  coral_attribute_class ac join
  coral_attribute_mapping am using(attribute_class_id)
