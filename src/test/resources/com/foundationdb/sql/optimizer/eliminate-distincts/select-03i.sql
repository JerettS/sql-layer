SELECT DISTINCT parent.name,child.name 
  FROM parent INNER JOIN child ON parent.id = child.pid
