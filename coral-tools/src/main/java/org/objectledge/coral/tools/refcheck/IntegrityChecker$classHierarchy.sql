SELECT
  rc.resource_class_id,
  rc.resource_class_id descendant_id
FROM 
  coral_resource_class rc
UNION ALL SELECT
  ri.parent,
  ri.child
FROM
  coral_resource_class_inheritance ri
ORDER BY 
  1, 2

