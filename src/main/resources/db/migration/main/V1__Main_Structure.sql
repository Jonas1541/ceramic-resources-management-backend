CREATE TABLE IF NOT EXISTS tb_company (
  id bigint NOT NULL AUTO_INCREMENT,
  cnpj varchar(255) DEFAULT NULL UNIQUE,
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  last_activity_at datetime DEFAULT NULL,
  database_port int DEFAULT NULL,
  database_name varchar(255) DEFAULT NULL,
  database_url varchar(255) DEFAULT NULL,
  email varchar(255) DEFAULT NULL UNIQUE,
  name varchar(255) DEFAULT NULL,
  password varchar(255) DEFAULT NULL,
  updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  marked_for_deletion BOOLEAN NOT NULL DEFAULT FALSE,
  deletion_scheduled_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tb_password_reset_token (
  id bigint NOT NULL AUTO_INCREMENT,
  token varchar(255) DEFAULT NULL,
  company_id bigint NOT NULL,
  expiry_date datetime DEFAULT NULL,
  created_at datetime DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (company_id) REFERENCES tb_company(id)
) ENGINE=InnoDB;