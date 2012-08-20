SELECT
  r.resource_id,
  %d attribute_definition_id,
  g.data_key
FROM 
  coral_resource r 
LEFT OUTER JOIN
  coral_generic_resource g USING (resource_id)
LEFT OUTER JOIN
  %s a ON (g.data_key = a.%s)
WHERE r.resource_class_id = %d
AND g.attribute_definition_id = %d 
AND a.%s IS NULL
