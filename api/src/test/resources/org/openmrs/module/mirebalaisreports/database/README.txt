This folder holds complete database dumps that can be loaded into a transient MariaDB database that mimics our real-life
MySQL database. We use MariaDB4j for this.

To create a dump, just set up the database the way you want it using the real application, and do something like:
    mysqldump mirebalais -u root -p > ~/inpatientStatsMonthly.sql