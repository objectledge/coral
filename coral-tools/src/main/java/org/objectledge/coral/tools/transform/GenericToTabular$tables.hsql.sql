select 
  rc.name 
from 
  coral_resource_class rc,
  coral_attribute_definition ad
where 
  rc.handler_class_name = 'org.objectledge.coral.datatypes.GenericResourceHandler' 
  and bitand(rc.flags, 4) = 0 -- ignore builtin classes
  and ad.resource_class_id = rc.resource_class_id
group by rc.name  
order by rc.name
