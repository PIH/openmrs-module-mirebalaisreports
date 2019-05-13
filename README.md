openmrs-module-mirebalaisreports
================================
Add a new sql report
-----------------------
* add a new sql file to api/src/main/resources/org/openmrs/module/mirebalaisreports/sql/fullDataExport
* enable the report by modifying org/openmrs/module/mirebalaisreports/definitions/FullDataExportBuilder.java
  * For country (ie. HAITI, LIBERIA, MEXICO, etc)
  * For site (ie. MIREBALAIS, CHIAPAS, etc)
  * For component (ie. visits, ncd, etc)
* for report definitions (variables used for reports), modify org/openmrs/module/mirebalaisreports/definitions/BasePihReportManager.java and/or org/openmrs/module/mirebalaisreports/MirebalaisReportsProperties.java
* for setting/translating labels, modify api/src/main/resources/messages.properties (English).  For adding other language translations (fr, ht, es)
  * push the file to Transifex (cd mirebalaisreport; tx push -s)
  * add translations to the mirebalaisreport module using Transifex https://www.transifex.com/pih/mirebalaisreport
  * pull the translations back into the mirebalaisreport module from Transifex (cd mirebalaisreport; tx pull; git add/commit/push)
