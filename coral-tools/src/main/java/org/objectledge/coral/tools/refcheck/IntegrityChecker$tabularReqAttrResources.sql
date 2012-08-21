select
  r.resource_id
from  
  coral_resource r
  left outer join %s t using(resource_id)
where 
  r.resource_class_id = %d
  and t.resource_id is null 
