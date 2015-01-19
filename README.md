How to configure & build
==========================
- Install & Configure Java JDK 1.7 or higher

- Install & Configure Maven 3.0 or higher

- Download a release version or clone and build (`mvn clean install`) the [OpenEngSB](https://github.com/openengsb/openengsb)

- Clone and build the [SQLCode](https://github.com/openengsb-domcon/openengsb-domain-sqlcode.git) domain

- Clone this project and adapt the `${sql.version}` property in the `pom.xml` to the version of the used SQLCode domain if necessary.

- Build this project (`mvn clean install`)

- You find the program in the directory `target`, its `org.openengsb.xlink.xlinkjavaclient-[version]-jar-with-dependencies.jar`

- To run the program, go to `target/classes` and copy the files `application.properties`
and `log4j.properties` into the same directory as your .jar

- Configure the program arguments in `application.properties`
    1. Change Username, Password and Context if necessary.
    2. If the OpenEngSB Server is not running on your local machine, the `openengsb.hostIp` has to be changed to the Ip of the
       network interface that connects to the server (default is 127.0.0.1).
    3. If the OpenEngSB Server is not running on your local machine, the `xlink.baseUrl` has to be changed as well.
    4. Set the `working.dir` to a local directory.
    5. Copy the `creates.sql` to this directory.

- Unzip the OpenEngSB server and start it (`bin/openengsb`)

- Make sure that the JMS-Port bundle is installed (if not install it with `feature:install openengsb-ports-jms`).
Note that, if server and client are not running on the same machine, the xlink base-URL (e.g. contains also the server URL) must
be configured in `$OPENENGSB_HOME$/etc/org.openengsb.core.services.internal.connectormanager.cfg`

- Copy the jar file of the build SQLCode domain to `$OPENENGSB_HOME/deploy` to deploy it

- Verify that the domain was started correctly (type `list` in the console of the OpenEngSB)

- Start the Java-Client

Structure of accepted SQL files
==========================
The Program filters SQL CreateStatements in this abstract syntax

A Statement is of the structure

```
CREATE TABLE `TABLENAME'
(
 'fieldName' 'dataType' 'listOfConstraints',
);
```

Make sure that the opening and closing bracket have no leading or trailing spaces.
Every fieldDefintion must be written in it's own row.

Accepted Constraints are
- `PRIMARY KEY`
- `NOT NULL`
- `REFERENCES 'tableName'('fieldName')`


Implemented Functionality
==========================
- Client automatically connects to the OpenEngSB and registers for XLink
- .sql Files in the WorkingDirectory can be opened and CreateStatements are viewed in a List
- For each Statement, details can be displayed
- Via a PopUp-Menu (right-click on a Statement), a valid XLink-URL is copied into the clipboard
- Incoming potential Matches are searched in the WorkingDirectory and, if found, the most likely match is displayed.

Not implemented Functionality
==========================
- Incoming updates about other local tools that support XLink are processed.
- Creation of Statements
