CREATE TABLE Person
(
 pid INTEGER PRIMARY KEY,
 vname VARCHAR(30) NOT NULL,
 nname VARCHAR(30) NOT NULL,
 gebdat DATE NOT NULL,
 adresse VARCHAR(60) NOT NULL
);

CREATE TABLE Kunde
(
 kundenID INTEGER REFERENCES Person(pid),
 PRIMARY KEY(kundenID)
);

CREATE TABLE Mitarbeiter
(
 maID INTEGER REFERENCES Person(pid) PRIMARY KEY,
 eindat DATE NOT NULL,
 gehalt SMALLINT NOT NULL,
 svnr BIGINT NOT NULL,
 kontonr INTEGER NOT NULL,
 blz INTEGER NOT NULL,
 zweigstelle VARCHAR(30) NOT NULL
);

CREATE TABLE Zweigstelle
(
 name VARCHAR(30) PRIMARY KEY,
 email VARCHAR(30) NOT NULL,
 anschrift VARCHAR(60) NOT NULL,
 telefon INTEGER NOT NULL,
 leiter INTEGER REFERENCES Mitarbeiter(maID)
);

CREATE TABLE Entlehnkarte
(
 nr INTEGER PRIMARY KEY,
 inhaber INTEGER REFERENCES Person(pid),
 gueltig DATE NOT NULL,
 gebuehr SMALLINT NOT NULL
);

CREATE TABLE Medium
(
 mediennr INTEGER PRIMARY KEY,
 jahr SMALLINT NOT NULL,
 titel VARCHAR(30) NOT NULL,
 freigabe SMALLINT NOT NULL
);

CREATE TABLE Exemplar
(
 enr INTEGER PRIMARY KEY,
 kaufdat DATE NOT NULL,
 gehoert VARCHAR(30) REFERENCES Zweigstelle(name) NOT NULL,
 medium INTEGER REFERENCES Medium(mediennr) NOT NULL
);

CREATE TABLE Kuenstler
(
 kid INTEGER PRIMARY KEY,
 vname VARCHAR(30) NOT NULL,
 nname VARCHAR(30) NOT NULL
);

CREATE TABLE Buch
(
 buchNr INTEGER PRIMARY KEY REFERENCES Medium(mediennr),
 umfang SMALLINT NOT NULL
);

CREATE TABLE Film
(
 filmNr PRIMARY KEY INTEGER REFERENCES Medium(mediennr),
 format VARCHAR(3) NOT NULL,
 regisseur INTEGER REFERENCES Kuenstler(kid) NOT NULL
);

CREATE TABLE Themengebiet
(
 themenid INTEGER PRIMARY KEY,
 name VARCHAR(50) NOT NULL
);

CREATE TABLE Entlehnung
(
 eid INTEGER PRIMARY KEY,
 karte INTEGER REFERENCES Entlehnkarte(nr),
 adat DATE NOT NULL,
 fdat DATE NOT NULL
);

CREATE TABLE zugeordnet
(
 medienNr INTEGER PRIMARY KEY REFERENCES Medium(mediennr),
 themenGebietID INTEGER REFERENCES Themengebiet(themenid)
);

CREATE TABLE vorgaenger
(
 vorgaenger INTEGER REFERENCES Themengebiet(themenid) NOT NULL,
 nachfolger INTEGER PRIMARY KEY REFERENCES Themengebiet(themenid)
);

CREATE TABLE verfasst
(
 autor INTEGER PRIMARY KEY REFERENCES Kuenstler(kid),
 buchNr INTEGER REFERENCES Buch(buchNr)
);

CREATE TABLE wird_entlehnt
(
 entlehnungsID INTEGER PRIMARY KEY REFERENCES Entlehnung(eid),
 exemplarNr INTEGER REFERENCES Exemplar(enr)
);

CREATE TABLE rueckgabe
(
 entlehnungsID INTEGER PRIMARY KEY REFERENCES Entlehnung(eid),
 exemplarNr INTEGER REFERENCES Exemplar(enr),
 zweigstellenID VARCHAR(30) REFERENCES Zweigstelle(name) NOT NULL,
 rdat DATE NOT NULL
);