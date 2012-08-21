select 
  ad.resource_class_id attribute_class_id,
  ar.name attribute_class_name,
  ad.attribute_definition_id,
  ad.name ad_name,
  ad.domain,
  dr.resource_class_id domain_class_id
from 
  coral_attribute_class ac 
join  
  coral_attribute_definition ad using (attribute_class_id)
join
  coral_resource_class ar using (resource_class_id)
left outer join
  coral_resource_class dr on (dr.name = ad.domain)  
where
  ac.name = 'resource' and
  ad.domain is not null
