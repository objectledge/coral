SELECT
  g.resource_id
FROM 
  coral_generic_resource g
LEFT OUTER JOIN
  coral_resource r USING (resource_id)
WHERE
  r.resource_id IS NULL
