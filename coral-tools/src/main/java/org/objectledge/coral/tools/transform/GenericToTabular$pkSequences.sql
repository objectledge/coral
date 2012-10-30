select
  tc.table_name, kc.column_name, s.sequence_name
from 
  information_schema.table_constraints tc,
  information_schema.key_column_usage kc,
  information_schema.sequences s
where 
  tc.constraint_type = 'PRIMARY KEY'
  and s.sequence_name = tc.table_name || '_seq'
  and kc.constraint_name = tc.constraint_name
  and kc.ordinal_position = 1

