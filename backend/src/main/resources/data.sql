-- Sample categories
INSERT INTO categories (id, name, description) VALUES (1, 'IT Equipment', 'Computers, laptops, and peripherals');
INSERT INTO categories (id, name, description) VALUES (2, 'Furniture', 'Office furniture and fixtures');
INSERT INTO categories (id, name, description) VALUES (3, 'Vehicles', 'Company vehicles');

-- Sample assets
INSERT INTO assets (id, name, code, status, purchase_date, purchase_value, current_value, category_id, useful_life_years)
VALUES (1, 'Dell Latitude Laptop', 'LAP-001', 'AVAILABLE', '2022-03-15', 1200.00, 1200.00, 1, 5);
INSERT INTO assets (id, name, code, status, purchase_date, purchase_value, current_value, category_id, useful_life_years)
VALUES (2, 'HP ProDesk Desktop', 'DSK-001', 'IN_USE', '2021-06-01', 800.00, 640.00, 1, 5);
INSERT INTO assets (id, name, code, status, purchase_date, purchase_value, current_value, category_id, useful_life_years)
VALUES (3, 'Ergonomic Office Chair', 'FRN-001', 'AVAILABLE', '2023-01-10', 250.00, 250.00, 2, 10);
INSERT INTO assets (id, name, code, status, purchase_date, purchase_value, current_value, category_id, useful_life_years)
VALUES (4, 'Toyota Corolla', 'VEH-001', 'IN_USE', '2020-09-20', 20000.00, 12000.00, 3, 8);
INSERT INTO assets (id, name, code, status, purchase_date, purchase_value, current_value, category_id, useful_life_years)
VALUES (5, 'MacBook Pro 16', 'LAP-002', 'IN_REPAIR', '2022-11-05', 2400.00, 2000.00, 1, 5);

-- Sample employees
INSERT INTO employees (id, name, email, department) VALUES (1, 'John Doe', 'john.doe@assetmanage.com', 'IT');
INSERT INTO employees (id, name, email, department) VALUES (2, 'Jane Smith', 'jane.smith@assetmanage.com', 'Finance');

-- Sample assignments
INSERT INTO assignments (id, asset_id, employee_id, assign_date, return_date)
VALUES (1, 2, 1, '2021-06-15', NULL);
INSERT INTO assignments (id, asset_id, employee_id, assign_date, return_date)
VALUES (2, 4, 2, '2020-10-01', NULL);

-- Reset identity counters so runtime inserts do not collide with seed ids
ALTER TABLE categories ALTER COLUMN id RESTART WITH 100;
ALTER TABLE assets ALTER COLUMN id RESTART WITH 100;
ALTER TABLE employees ALTER COLUMN id RESTART WITH 100;
ALTER TABLE assignments ALTER COLUMN id RESTART WITH 100;
