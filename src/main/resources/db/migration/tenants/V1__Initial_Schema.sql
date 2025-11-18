CREATE TABLE tb_resource (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    unit_value DECIMAL(10,2) NOT NULL,
    category ENUM ('COMPONENT','ELECTRICITY','GAS','RAW_MATERIAL','RETAIL','SILICATE','WATER'),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE tb_employee_category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE tb_employee (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    employee_category_id BIGINT NOT NULL,
    cost_per_hour DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (employee_category_id) REFERENCES tb_employee_category (id)
) ENGINE=InnoDB;

CREATE TABLE tb_batch (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    batch_total_water_cost_at_time DECIMAL(10,2) NOT NULL,
    resource_total_cost_at_time DECIMAL(10,2) NOT NULL,
    machines_energy_consumption_cost_at_time DECIMAL(10,2) NOT NULL,
    employee_total_cost_at_time DECIMAL(10,2) NOT NULL,
    batch_final_cost_at_time DECIMAL(10,2) NOT NULL,
    weight DOUBLE NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE tb_machine (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    power DOUBLE NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE tb_batch_resource_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    initial_quantity DOUBLE NOT NULL,
    umidity DOUBLE NOT NULL,
    added_quantity DOUBLE NOT NULL,
    total_cost_at_time DECIMAL(10,2) NOT NULL,
    batch_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (batch_id) REFERENCES tb_batch (id),
    FOREIGN KEY (resource_id) REFERENCES tb_resource (id)
) ENGINE=InnoDB;

CREATE TABLE tb_batch_machine_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usage_time DOUBLE NOT NULL,
    batch_id BIGINT NOT NULL,
    machine_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (batch_id) REFERENCES tb_batch (id),
    FOREIGN KEY (machine_id) REFERENCES tb_machine (id)
) ENGINE=InnoDB;

CREATE TABLE tb_batch_employee_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usage_time DOUBLE NOT NULL,
    employee_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (employee_id) REFERENCES tb_employee (id),
    FOREIGN KEY (batch_id) REFERENCES tb_batch (id)
) ENGINE=InnoDB;

CREATE TABLE tb_glaze (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    color VARCHAR(255) NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE tb_glaze_resource_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    quantity DOUBLE NOT NULL,
    glaze_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_GLAZE_RES_USAGE_GLZ 
        FOREIGN KEY (glaze_id) REFERENCES tb_glaze (id) ON DELETE CASCADE,
    CONSTRAINT FK_GLAZE_RES_USAGE_RESOURCE 
        FOREIGN KEY (resource_id) REFERENCES tb_resource (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE tb_glaze_machine_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usage_time DOUBLE NOT NULL,
    glaze_id BIGINT NOT NULL,
    machine_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_GLAZE_MACH_USAGE_GLZ
        FOREIGN KEY (glaze_id) REFERENCES tb_glaze (id) ON DELETE CASCADE,
    CONSTRAINT FK_GLAZE_MACH_USAGE_MACH
        FOREIGN KEY (machine_id) REFERENCES tb_machine (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE tb_glaze_employee_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usage_time DOUBLE NOT NULL,
    employee_id BIGINT NOT NULL,
    glaze_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (employee_id) REFERENCES tb_employee (id) ON DELETE RESTRICT,
    FOREIGN KEY (glaze_id) REFERENCES tb_glaze (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE tb_glaze_transaction (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    quantity DOUBLE NOT NULL,
    TYPE ENUM('INCOMING','OUTGOING') NOT NULL,
    glaze_id BIGINT NOT NULL,
    resource_total_cost_at_time DECIMAL(10,2) NOT NULL,
    machine_energy_consumption_cost_at_time DECIMAL(10,2) NOT NULL,
    employee_total_cost_at_time DECIMAL(10,2) NOT NULL,
    glaze_final_cost_at_time DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_GLAZE_TRANSACTION_GLZ
        FOREIGN KEY (glaze_id) REFERENCES tb_glaze (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE tb_resource_transaction (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    quantity DOUBLE NOT NULL,
    TYPE ENUM ('INCOMING','OUTGOING'),
    resource_id BIGINT NOT NULL,
    batch_id BIGINT NULL,
    glaze_transaction_id BIGINT NULL,
    cost_at_time DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (resource_id) REFERENCES tb_resource (id),
    FOREIGN KEY (batch_id) REFERENCES tb_batch (id) ON DELETE CASCADE,
    FOREIGN KEY (glaze_transaction_id) REFERENCES tb_glaze_transaction (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE tb_product_line (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE tb_product_type (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE tb_product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    height DOUBLE NOT NULL,
    length DOUBLE NOT NULL,
    width DOUBLE NOT NULL,
    glaze_quantity_per_unit DOUBLE NOT NULL,
    weight DOUBLE NOT NULL,
    product_type_id BIGINT NOT NULL,
    product_line_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_type_id) REFERENCES tb_product_type (id),
    FOREIGN KEY (product_line_id) REFERENCES tb_product_line (id)
) ENGINE=InnoDB;

CREATE TABLE tb_kiln (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    name VARCHAR(255) NOT NULL,
    gas_consumption_per_hour DOUBLE NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE tb_bisque_firing (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    temperature DOUBLE NOT NULL,
    burn_time DOUBLE NOT NULL,
    cooling_time DOUBLE NOT NULL,
    kiln_id BIGINT NOT NULL,
    cost_at_time DECIMAL(10,2),
    PRIMARY KEY (id),
    FOREIGN KEY (kiln_id) REFERENCES tb_kiln (id)
) ENGINE=InnoDB;

CREATE TABLE tb_bisque_firing_employee_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usage_time DOUBLE NOT NULL,
    employee_id BIGINT NOT NULL,
    bisque_firing_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (employee_id) REFERENCES tb_employee (id),
    FOREIGN KEY (bisque_firing_id) REFERENCES tb_bisque_firing (id)
) ENGINE=InnoDB;

CREATE TABLE tb_glaze_firing (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    temperature DOUBLE NOT NULL,
    burn_time DOUBLE NOT NULL,
    cooling_time DOUBLE NOT NULL,
    kiln_id BIGINT NOT NULL,
    cost_at_time DECIMAL(10,2),
    PRIMARY KEY (id),
    FOREIGN KEY (kiln_id) REFERENCES tb_kiln (id)
) ENGINE=InnoDB;

CREATE TABLE tb_glaze_firing_employee_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usage_time DOUBLE NOT NULL,
    employee_id BIGINT NOT NULL,
    glaze_firing_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (employee_id) REFERENCES tb_employee (id),
    FOREIGN KEY (glaze_firing_id) REFERENCES tb_glaze_firing (id)
) ENGINE=InnoDB;

CREATE TABLE tb_product_transaction (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    outgoing_at DATETIME,
    state ENUM ('GREENWARE', 'BISCUIT', 'GLAZED') NOT NULL,
    outgoing_reason ENUM ('SOLD', 'DEFECT_DISPOSAL'),
    product_id BIGINT NOT NULL,
    glaze_transaction_id BIGINT,
    bisque_firing_id BIGINT,
    glaze_firing_id BIGINT,
    cost DECIMAL(10,2),
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES tb_product (id),
    FOREIGN KEY (glaze_transaction_id) REFERENCES tb_glaze_transaction (id),
    FOREIGN KEY (bisque_firing_id) REFERENCES tb_bisque_firing (id),
    FOREIGN KEY (glaze_firing_id) REFERENCES tb_glaze_firing (id)
) ENGINE=InnoDB;

CREATE TABLE tb_product_transaction_employee_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usage_time DOUBLE NOT NULL,
    employee_id BIGINT NOT NULL,
    product_transaction_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (employee_id) REFERENCES tb_employee (id),
    FOREIGN KEY (product_transaction_id) REFERENCES tb_product_transaction (id)
) ENGINE=InnoDB;

CREATE TABLE tb_drying_room (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    gas_consumption_per_hour DOUBLE NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE tb_kiln_machine (
    kiln_id BIGINT NOT NULL,
    machine_id BIGINT NOT NULL,
    PRIMARY KEY (kiln_id, machine_id),
    FOREIGN KEY (kiln_id) REFERENCES tb_kiln(id) ON DELETE CASCADE,
    FOREIGN KEY (machine_id) REFERENCES tb_machine(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE tb_drying_room_machine (
    drying_room_id BIGINT NOT NULL,
    machine_id BIGINT NOT NULL,
    PRIMARY KEY (drying_room_id, machine_id),
    FOREIGN KEY (drying_room_id) REFERENCES tb_drying_room(id) ON DELETE CASCADE,
    FOREIGN KEY (machine_id) REFERENCES tb_machine(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE tb_drying_session (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    hours DOUBLE NOT NULL,
    cost_at_time DECIMAL(10,2) NOT NULL,
    drying_room_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (drying_room_id) REFERENCES tb_drying_room(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE tb_drying_session_employee_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usage_time DOUBLE NOT NULL,
    employee_id BIGINT NOT NULL,
    drying_session_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (employee_id) REFERENCES tb_employee (id),
    FOREIGN KEY (drying_session_id) REFERENCES tb_drying_session (id)
) ENGINE=InnoDB;