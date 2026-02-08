describe campus;
describe batiment;

drop table exploite;
drop table salle;
drop table batiment;
drop table campus;
drop table composante;

create table campus (nomC varchar(16), constraint campus_pk primary key(nomC), ville varchar(20));

create table batiment (codeB varchar(16), constraint batiment_pk primary key(codeB), anneeC integer, campus varchar(16), constraint campus_fk foreign key (campus) references campus(nomC) on delete cascade);

create table salle (numS varchar(16), constraint salle_pk primary key(numS), capacite integer, typeS varchar(12), acces varchar(3), etage varchar(3), batiment varchar(16), constraint batiment_fk foreign key(batiment) references batiment(codeB) on delete cascade, constraint dom_typeS check (typeS in ('amphi','sc','td','tp','numerique')));

create table composante (acronyme varchar(8), constraint composante_pk primary key (acronyme), nom varchar(50), responsable varchar(30));

create table exploite (team varchar(8), constraint exploite_fk1 foreign key(team) references composante(acronyme) on delete cascade, building varchar(16), constraint exploite_fk2 foreign key(building) references batiment(codeB) on delete cascade, constraint exploite_pk primary key(team,building));

--tuples
insert into campus values ('Triolet','Montpellier');
insert into campus values ('St Priest','Montpellier');
insert into campus values ('Pharmacie','Montpellier');
insert into campus values ('Richter','Montpellier');
insert into campus values ('FDE Mende','Mende');
insert into campus values ('Medecine Nimes','Nimes');


insert into composante values ('FDS','Faculte des Sciences','JM. Marin');
insert into composante values ('IAE','Ecole Universitaire de Management','E Houze');
insert into composante values ('Polytech','Polytech Montpellier','L. Torres');

commit;



