CREATE TABLE IF NOT EXISTS campus (
  nomC VARCHAR(16),
  ville VARCHAR(20),
  CONSTRAINT campus_pk PRIMARY KEY (nomC)
);

CREATE TABLE IF NOT EXISTS batiment (
  codeB VARCHAR(32),
  name TEXT,
  osm_id BIGINT,
  building_number INTEGER,
  anneeC INTEGER,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  campus VARCHAR(16),
  CONSTRAINT batiment_pk PRIMARY KEY (codeB),
  CONSTRAINT campus_fk FOREIGN KEY (campus) REFERENCES campus(nomC) ON DELETE CASCADE
);

ALTER TABLE batiment ADD COLUMN IF NOT EXISTS name TEXT;
ALTER TABLE batiment ADD COLUMN IF NOT EXISTS osm_id BIGINT;
ALTER TABLE batiment ADD COLUMN IF NOT EXISTS building_number INTEGER;
ALTER TABLE batiment ALTER COLUMN codeB TYPE VARCHAR(32);

CREATE TABLE IF NOT EXISTS salle (
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

CREATE TABLE IF NOT EXISTS composante (
  acronyme VARCHAR(8),
  nom VARCHAR(50),
  responsable VARCHAR(30),
  CONSTRAINT composante_pk PRIMARY KEY (acronyme)
);

CREATE TABLE IF NOT EXISTS exploite (
  team VARCHAR(8),
  building VARCHAR(32),
  CONSTRAINT exploite_fk1 FOREIGN KEY (team) REFERENCES composante(acronyme) ON DELETE CASCADE,
  CONSTRAINT exploite_fk2 FOREIGN KEY (building) REFERENCES batiment(codeB) ON DELETE CASCADE,
  CONSTRAINT exploite_pk PRIMARY KEY (team, building)
);

CREATE TABLE IF NOT EXISTS osm_building (
  osm_id BIGINT,
  name TEXT,
  campus VARCHAR(32),
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  tags TEXT,
  CONSTRAINT osm_building_pk PRIMARY KEY (osm_id)
);

ALTER TABLE osm_building ALTER COLUMN name TYPE TEXT;
ALTER TABLE salle ALTER COLUMN batiment TYPE VARCHAR(32);
ALTER TABLE exploite ALTER COLUMN building TYPE VARCHAR(32);

CREATE TABLE IF NOT EXISTS campus_boundary (
  campus VARCHAR(32),
  source VARCHAR(64),
  geojson TEXT,
  CONSTRAINT campus_boundary_pk PRIMARY KEY (campus)
);
