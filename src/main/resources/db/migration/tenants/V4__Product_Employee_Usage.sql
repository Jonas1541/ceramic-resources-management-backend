CREATE TABLE tb_product_employee_usage (
    id BIGINT AUTO_INCREMENT NOT NULL,
    usage_time DOUBLE NOT NULL,
    employee_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_employee_usage_employee FOREIGN KEY (employee_id) REFERENCES tb_employee(id),
    CONSTRAINT fk_product_employee_usage_product FOREIGN KEY (product_id) REFERENCES tb_product(id) ON DELETE CASCADE
);

-- Drop old table
-- WARNING: This will delete historical detailed usage data. Ensure this is intended.
DROP TABLE tb_product_transaction_employee_usage;
