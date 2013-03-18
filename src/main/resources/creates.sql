CREATE TABLE ClientContract
(
  cc_Id BIGINT PRIMARY KEY,
  company VARCHAR(150) NOT NULL,
  orderTotal FLOAT,
  contractAdministrator BIGINT REFERENCES ContractAdministrator(caId),
  client BIGINT REFERENCES ContractAdministrator(caId), 
  dateOfCreation DATE NOT NULL
);

CREATE TABLE ContractAdministrator
(
  ca_Id BIGINT PRIMARY KEY,
  administratorCompany VARCHAR(150) NOT NULL,
  department VARCHAR(150) NOT NULL,
  contactNumber INTEGER
);

CREATE TABLE Client
(
  cl_Id BIGINT REFERENCES Person(pid) PRIMARY KEY,
  numberOfContracts INTEGER,
  creditcardNumber INTEGER,
  bankAccountNumber INTEGER,
  bankNumber INTEGER,
  balance FLOAT
);

CREATE TABLE ProducedProduct
(
   pp_Id BIGINT PRIMARY KEY,
   name VARCHAR(150) NOT NULL,
   size INTEGER,
   cost FLOAT,
   amount INTEGER,
   clientContract BIGINT REFERENCES ClientContract(cc_Id),
   productStorage BIGINT REFERENCES ProductStorage(ps_Id)  
);

CREATE TABLE ProductionFacility
(
    pf_Id BIGINT PRIMARY KEY,
    facilityName VARCHAR(150) NOT NULL,
    managerName VARCHAR(200) NOT NULL
);

CREATE TABLE ProductStorage
(
    ps_Id BIGINT PRIMARY KEY,
    storageName VARCHAR(150) NOT NULL,
    storageAddress VARCHAR(300) NOT NULL
);

CREATE TABLE ProductionMachine
(
   pm_Id BIGINT PRIMARY KEY,
   machineSerialId INTEGER,
   averageOutput INTEGER,
   purchaseDate DATE NOT NULL
   productionFacility BIGINT REFERENCES ProductionFacility(pfId)  
);

CREATE TABLE ProductionChain
(
   pc_Id BIGINT PRIMARY KEY,
   chainName VARCHAR(150) NOT NULL,
   duration INTEGER,
   numberOfEXecutions INTEGER
);

CREATE TABLE ProductionPlan
(
   pp_Id BIGINT PRIMARY KEY,
   planName VARCHAR(150) NOT NULL,
   creationDate DATE NOT NULL
);

CREATE TABLE ScheduledProduct
(
   sp_Id BIGINT PRIMARY KEY,
   productLabel VARCHAR(150) NOT NULL,
   count INTEGER,
   productionDate DATE NOT NULL
   productionMachine BIGINT REFERENCES ProductionMachine(pmId)  
);

CREATE TABLE Person
(
  p_id BIGINT PRIMARY KEY,
  firstName VARCHAR(30) NOT NULL,
  lastName VARCHAR(30) NOT NULL,
  dateOfBirth DATE NOT NULL,
  address BIGINT REFERENCES Address(aid)
);

CREATE TABLE Address
(
  a_id BIGINT PRIMARY KEY,
  street VARCHAR(150) NOT NULL,
  number INTEGER,
  region VARCHAR(100) NOT NULL,
  postalCode INTEGER,
  country VARCHAR(100) NOT NULL,
  countryCode INTEGER
);
