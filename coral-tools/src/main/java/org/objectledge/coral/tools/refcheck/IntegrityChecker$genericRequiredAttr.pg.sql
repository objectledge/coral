SELECT
  ad.resource_class_id,
  ad.attribute_definition_id,
  ad.name,
  ac.db_table_name,
  am.fk_key_column,
  COALESCE(am.is_custom AND NOT am.is_fk, false) is_multi
FROM 
  coral_attribute_definition ad,
  coral_attribute_class ac,
  coral_attribute_mapping am
WHERE 
  ad.flags & 1 = 1 
  AND ad.flags & 8 = 0
  AND ac.attribute_class_id = ad.attribute_class_id
  AND am.attribute_class_id = ad.attribute_class_id
