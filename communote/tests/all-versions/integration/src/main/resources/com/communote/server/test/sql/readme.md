# About
The create.*.sql are responsible for creating the database. The name of the database has to be replaced
with placeholder @@DATABASE@@. If an owner exists it has to be substituted with @@USERNAME@@.

The drop.*.sql scripts are responsible for removing or clearing the database. It will be executed before
applying the create.*.sql script. If drop.*-2nd.sql script exists it will be run after the drop.*.sql script.

The data.*.sql scripts create the schema and provide data of a fresh Communote installation. They are
actually dumps which were taken after running the installer test. The dumps should be
updated from time to time to speed-up liquibase data-migration in the CommunoteIntegrationTests.

The post-data.*.sql scripts are used to replace some values with those of the current running test.

# Creating the data.*.sql dumps
## PostgrSQL
Create a dump with pg_dump from the database created by the InstallerTest:
    pg_dump -U communote -b -O -x --inserts -f <path-to-dump.sql> communote_installer_test
Remove the `CREATE EXTENSION` and `COMMENT ON EXTENSION` statements from the dump.

## MySQL
Create a dump with mysqldump from the database created by the InstallerTest:
    mysqldump -u communote -p --default-character-set=utf8 --skip-add-drop-table --skip-add-locks --skip-comments --skip-disable-keys --skip-set-charset --result-file=<path-to-dump.sql> communote_installer_test

## MSSQL
* Start SQL Server Management Studio and select the database created by the InstallerTest
* Right-click and select Tasks -> Generate Scripts...
* Select 'Script entire database and all database objects'
* Open Advanced settings and set
** 'ANSI Padding' to False
** 'Script USE DATABASE' to False
** 'Types of data to script' to 'Schema and data'
* Save to file
* Modify the generated SQL file
** Remove everything before the SQL statements that create the first table
** Run regular expression based replacement over the file, with search string (excluding quotes) '$\R^GO$' and replacement (excluding quotes) ';'