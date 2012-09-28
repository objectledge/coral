select rc.resource_class_id, 
  rc.name, 
  ad.attribute_definition_id, 
  ad.name, 
  ac.db_table_name, 
  am.is_fk, 
  coalesce(am.is_custom, false) as is_custom
from coral_resource_class rc,
  coral_attribute_definition ad,
  coral_attribute_class ac,
  coral_attribute_mapping am
where rc.resource_class_id = ad.resource_class_id 
  and ac.attribute_class_id = ad.attribute_class_id
  and am.attribute_class_id = ad.attribute_class_id
  and bitand(rc.flags, 4) = 0
  and bitand(ad.flags, 8) = 0
order by 
  rc.name, 
  ad.name;
