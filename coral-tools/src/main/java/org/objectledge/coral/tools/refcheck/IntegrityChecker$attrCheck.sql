select
  g.data_key as g_data_key,
  a.data_key as a_data_key
from 
  (select 
     g.data_key
   from coral_attribute_definition ad,
     coral_generic_resource g
   where ad.attribute_class_id = %d
     and g.attribute_definition_id = ad.attribute_definition_id
  ) g full outer join
  (select 
     data_key
   from
     %s
  ) a on (g.data_key = a.data_key) 
where
  a.data_key is null
  or g.data_key is null
