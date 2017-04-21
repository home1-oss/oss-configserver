
--
CREATE TABLE IF NOT EXISTS users (
  username VARCHAR(50)  NOT NULL,
  password VARCHAR(255) NOT NULL,
  enabled  BOOLEAN      NOT NULL,
  PRIMARY KEY (username)
)
  ENGINE = InnoDb;

--
CREATE TABLE IF NOT EXISTS authorities (
  username  VARCHAR(50) NOT NULL,
  authority VARCHAR(50) NOT NULL,
  FOREIGN KEY (username) REFERENCES users (username),
  UNIQUE INDEX authorities_idx_1 (username, authority)
)
  ENGINE = InnoDb;

--
CREATE TABLE IF NOT EXISTS groups (
  id BIGINT NOT NULL AUTO_INCREMENT,
  group_name VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
)
  ENGINE = InnoDb;

--
CREATE TABLE IF NOT EXISTS group_authorities (
  group_id BIGINT NOT NULL,
  authority VARCHAR(50) NOT NULL,
  FOREIGN KEY (group_id) REFERENCES groups (id)
)
  ENGINE = InnoDb;

--
CREATE TABLE IF NOT EXISTS group_members (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  group_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (group_id) REFERENCES groups (id)
)
  ENGINE = InnoDb;
