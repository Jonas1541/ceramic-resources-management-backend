-- Add new columns
ALTER TABLE tb_kiln
ADD COLUMN average_bisque_gas_consumption DOUBLE NOT NULL DEFAULT 0,
ADD COLUMN average_glaze_gas_consumption DOUBLE NOT NULL DEFAULT 0;

-- Migrate data
UPDATE tb_kiln
SET average_bisque_gas_consumption = gas_consumption_per_hour,
    average_glaze_gas_consumption = gas_consumption_per_hour;

-- Drop old column
ALTER TABLE tb_kiln
DROP COLUMN gas_consumption_per_hour;
