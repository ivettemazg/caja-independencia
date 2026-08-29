CREATE TABLE password_reset_tokens (
  prt_id INT AUTO_INCREMENT PRIMARY KEY,
  prt_usu_id INT NOT NULL,
  prt_token_hash VARCHAR(128) NOT NULL,
  prt_fecha_creacion DATETIME NOT NULL,
  prt_fecha_expira DATETIME NOT NULL,
  prt_fecha_uso DATETIME NULL,
  prt_estatus INT NOT NULL DEFAULT 1,
  prt_ip_solicitud VARCHAR(45) NULL,
  prt_user_agent VARCHAR(255) NULL,
  INDEX idx_prt_token_hash (prt_token_hash),
  INDEX idx_prt_usu_id (prt_usu_id),
  INDEX idx_prt_expira (prt_fecha_expira)
);
