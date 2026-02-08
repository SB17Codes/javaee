INSERT INTO campus (nomC, ville) VALUES ('Triolet','Montpellier') ON CONFLICT (nomC) DO NOTHING;
INSERT INTO campus (nomC, ville) VALUES ('St Priest','Montpellier') ON CONFLICT (nomC) DO NOTHING;
INSERT INTO campus (nomC, ville) VALUES ('Pharmacie','Montpellier') ON CONFLICT (nomC) DO NOTHING;
INSERT INTO campus (nomC, ville) VALUES ('Richter','Montpellier') ON CONFLICT (nomC) DO NOTHING;
INSERT INTO campus (nomC, ville) VALUES ('FDE Mende','Mende') ON CONFLICT (nomC) DO NOTHING;
INSERT INTO campus (nomC, ville) VALUES ('Medecine Nimes','Nimes') ON CONFLICT (nomC) DO NOTHING;

INSERT INTO composante (acronyme, nom, responsable) VALUES ('FDS','Faculte des Sciences','JM. Marin') ON CONFLICT (acronyme) DO NOTHING;
INSERT INTO composante (acronyme, nom, responsable) VALUES ('IAE','Ecole Universitaire de Management','E Houze') ON CONFLICT (acronyme) DO NOTHING;
INSERT INTO composante (acronyme, nom, responsable) VALUES ('Polytech','Polytech Montpellier','L. Torres') ON CONFLICT (acronyme) DO NOTHING;
