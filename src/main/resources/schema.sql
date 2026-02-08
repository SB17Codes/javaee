DROP TABLE IF EXISTS exploite;
DROP TABLE IF EXISTS batiment;
DROP TABLE IF EXISTS composante;
DROP TABLE IF EXISTS app_user;
DROP TABLE IF EXISTS campus;
DROP TABLE IF EXISTS osm_building;
DROP TABLE IF EXISTS campus_boundary;

CREATE TABLE campus (
  nomC VARCHAR(16),
  ville VARCHAR(20),
  CONSTRAINT campus_pk PRIMARY KEY (nomC)
);

CREATE TABLE batiment (
  codeB VARCHAR(32),
  name TEXT,
  osm_id BIGINT,
  building_number INTEGER,
  anneeC INTEGER,
  latitude DOUBLE,
  longitude DOUBLE,
  campus VARCHAR(16),
  CONSTRAINT batiment_pk PRIMARY KEY (codeB),
  CONSTRAINT campus_fk FOREIGN KEY (campus) REFERENCES campus(nomC) ON DELETE CASCADE
);

CREATE TABLE salle (
  numS VARCHAR(16),
  capacite INTEGER,
  typeS VARCHAR(12),
  acces VARCHAR(3),
  etage VARCHAR(3),
  batiment VARCHAR(32),
  CONSTRAINT salle_pk PRIMARY KEY (numS),
  CONSTRAINT batiment_fk FOREIGN KEY (batiment) REFERENCES batiment(codeB) ON DELETE CASCADE,
  CONSTRAINT dom_typeS CHECK (typeS IN ('amphi','sc','td','tp','numerique'))
);

CREATE TABLE composante (
  acronyme VARCHAR(8),
  nom VARCHAR(50),
  responsable VARCHAR(30),
  CONSTRAINT composante_pk PRIMARY KEY (acronyme)
);

CREATE TABLE app_user (
  id BIGINT AUTO_INCREMENT,
  username VARCHAR(64) UNIQUE,
  password_hash VARCHAR(255),
  role VARCHAR(32),
  enabled BOOLEAN,
  created_at TIMESTAMP,
  CONSTRAINT app_user_pk PRIMARY KEY (id)
);

CREATE TABLE exploite (
  team VARCHAR(8),
  building VARCHAR(32),
  CONSTRAINT exploite_fk1 FOREIGN KEY (team) REFERENCES composante(acronyme) ON DELETE CASCADE,
  CONSTRAINT exploite_fk2 FOREIGN KEY (building) REFERENCES batiment(codeB) ON DELETE CASCADE,
  CONSTRAINT exploite_pk PRIMARY KEY (team, building)
);

CREATE TABLE osm_building (
  osm_id BIGINT,
  name TEXT,
  campus VARCHAR(32),
  latitude DOUBLE,
  longitude DOUBLE,
  tags TEXT,
  CONSTRAINT osm_building_pk PRIMARY KEY (osm_id)
);

CREATE TABLE campus_boundary (
  campus VARCHAR(32),
  source VARCHAR(64),
  geojson TEXT,
  CONSTRAINT campus_boundary_pk PRIMARY KEY (campus)
);
