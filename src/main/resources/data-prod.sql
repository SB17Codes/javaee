INSERT INTO campus (nomC, ville) VALUES ('Triolet','Montpellier') ON CONFLICT (nomC) DO NOTHING;
INSERT INTO campus (nomC, ville) VALUES ('St Priest','Montpellier') ON CONFLICT (nomC) DO NOTHING;
INSERT INTO campus (nomC, ville) VALUES ('Pharmacie','Montpellier') ON CONFLICT (nomC) DO NOTHING;
INSERT INTO campus (nomC, ville) VALUES ('Richter','Montpellier') ON CONFLICT (nomC) DO NOTHING;
INSERT INTO campus (nomC, ville) VALUES ('FDE Mende','Mende') ON CONFLICT (nomC) DO NOTHING;
INSERT INTO campus (nomC, ville) VALUES ('Medecine Nimes','Nimes') ON CONFLICT (nomC) DO NOTHING;

INSERT INTO composante (acronyme, nom, responsable) VALUES ('FDS','Faculte des Sciences','JM. Marin') ON CONFLICT (acronyme) DO NOTHING;
INSERT INTO composante (acronyme, nom, responsable) VALUES ('IAE','Ecole Universitaire de Management','E Houze') ON CONFLICT (acronyme) DO NOTHING;
INSERT INTO composante (acronyme, nom, responsable) VALUES ('Polytech','Polytech Montpellier','L. Torres') ON CONFLICT (acronyme) DO NOTHING;

INSERT INTO app_user (username, password_hash, role, enabled, created_at)
VALUES ('admin', '$2a$10$K0wx2TYTEEPirM2tAKI8PuviT9uhwDhqSumVMPc4yVEieyBirR86C', 'ADMIN', true, NOW())
ON CONFLICT (username) DO NOTHING;
INSERT INTO app_user (username, password_hash, role, enabled, created_at)
VALUES ('manager', '$2a$10$CqSQks9F5Fl.yTDaDYRyAOjm3vTaddWJX17Izk5/Q4jgv8l1J6wjq', 'MANAGER', true, NOW())
ON CONFLICT (username) DO NOTHING;
INSERT INTO app_user (username, password_hash, role, enabled, created_at)
VALUES ('teacher', '$2a$10$enWTRX.uyYeJxXo/TOF.ee3/rkG1Kp0ECkjorJcUnZ9mk3KuwbKwW', 'TEACHER', true, NOW())
ON CONFLICT (username) DO NOTHING;
INSERT INTO app_user (username, password_hash, role, enabled, created_at)
VALUES ('student', '$2a$10$xS6Zqx.SIbKrxclr5ExoeO6vmZJDqSQQxQiOqXpyG12Q5XZBmt3qa', 'STUDENT', true, NOW())
ON CONFLICT (username) DO NOTHING;
