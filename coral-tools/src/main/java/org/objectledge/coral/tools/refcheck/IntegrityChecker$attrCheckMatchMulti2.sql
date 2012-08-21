select
  g.data_key,
  count(*) res_count
from 
  (select 
     g.resource_id, 
     g.data_key
   from coral_attribute_definition ad,
     coral_generic_resource g
   where ad.attribute_class_id = %d
     and g.attribute_definition_id = ad.attribute_definition_id
  ) g join
  (select 
     %s data_key
   from
     %s
   group by
     %2$s
  ) a on (g.data_key = a.data_key) 
group by 
  g.data_key
having 
  count(*) > 1
