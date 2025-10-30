ATM Project - SQL & Connector

Files in this folder:

- create_atm_db.sql
  - SQL script to create the `atm_db` database, `ATM_details` and `Cash_availability` tables, and insert 5 sample rows.

- mysql-connector-j-9.5.0/
  - Local copy of the MySQL Connector/J jar used by the project.

Running the GUI locally

1) Compile Java sources (from project root):

```powershell
javac -d out\classes src\main\java\*.java
```

2) Run the GUI, making sure the connector jar is on the classpath:

```powershell
& java -cp "out\classes;D:\Java Project\sql\mysql-connector-j-9.5.0\mysql-connector-j-9.5.0.jar" AtmAppGui
```

Packaging (Maven)

A Maven `pom.xml` is provided which can create a distributable zip containing the application jar, the connector jar (copied from this folder), and the SQL script.

To build the distribution (requires Maven installed):

```powershell
mvn clean package
```

The resulting zip will be in `target/` named `${project.artifactId}-${project.version}-dist.zip` and will contain:

- `${artifactId}-${version}.jar` (the application jar)
- `lib/mysql-connector-j-9.5.0.jar`
- `sql/create_atm_db.sql`

Security note

Do not commit real passwords to source. Use environment variables (MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD) to override defaults when running the app.
