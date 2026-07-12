-- Delete history records first to avoid foreign key violations
DELETE FROM histories WHERE stage_id IN (
    SELECT id FROM stages WHERE solution_grid IS NULL OR solution_grid = '' OR solution_grid = '[]'
);

-- Delete stages with null or empty solution grids
DELETE FROM stages WHERE solution_grid IS NULL OR solution_grid = '' OR solution_grid = '[]';
