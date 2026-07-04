-- Delete histories and stages for Smile Face, Hollow Square 20x20, and Hollow Square 30x30
DELETE FROM histories WHERE stage_id IN (SELECT id FROM stages WHERE name IN ('Smile Face', 'Hollow Square 20x20', 'Hollow Square 30x30'));
DELETE FROM stages WHERE name IN ('Smile Face', 'Hollow Square 20x20', 'Hollow Square 30x30');
