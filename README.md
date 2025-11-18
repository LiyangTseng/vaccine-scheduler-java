# vaccine-scheduler-java

### Entity–relationship model
![Alt text](src/main/resources/er_diagram.png "ER diagram")
### Setup
Create new db file ```test.db``` and create the tables using SQL queries defined in [create.sql](src/main/resources/sqlite/create.sql)
```shell
sqlite3 test.db
.read src/main/resources/sqlite/create.sql
```

### compile
```shell
./compile.sh
```

### run
```shell
./run.sh
```