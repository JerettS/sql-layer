SELECT cid FROM customers WHERE name NOT IN (SELECT sku FROM items, parent WHERE items.oid = parent.id)