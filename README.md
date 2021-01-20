openmrs-module-mirebalaisreports
================================

This module provides **legacy** mechanisms for defining reports using the
[OpenMRS Reporting Module](https://github.com/openmrs/openmrs-module-reporting).

**Do not use this** for defining new reports. New reports should be created using
[config](https://github.com/PIH/openmrs-config-pihemr/#reports).

## Legacy reports

There are several ways these reports are defined.

1. **FullDataExport**: Allows defining, a CSV report. Requires some Java and some SQL.
2. **DataReportManager with SQL**: Allows defining a CSV report, or can fill in an XML template. Requires Java and SQL.
3. **DataReportManager with only Java**: Allows defining a CSV report, or can fill in an XML template. Requires Java, lots of it. 

### 1. Using FullDataExport

See 
[FullDataExportBuilder.java](https://github.com/PIH/openmrs-module-mirebalaisreports/blob/master/api/src/main/java/org/openmrs/module/mirebalaisreports/definitions/FullDataExportBuilder.java) 
and 
[ncd.sql](https://github.com/PIH/openmrs-module-mirebalaisreports/blob/master/api/src/main/resources/org/openmrs/module/mirebalaisreports/sql/fullDataExport/ncd.sql)
for reference.

* add a new sql file to `api/src/main/resources/org/openmrs/module/mirebalaisreports/sql/fullDataExport`
* enable the report by modifying `org/openmrs/module/mirebalaisreports/definitions/FullDataExportBuilder.java`
  * For country (ie. HAITI, LIBERIA, MEXICO, etc)
  * For site (ie. MIREBALAIS, CHIAPAS, etc)
  * For component (ie. visits, ncd, etc)
* for report definitions (variables used for reports), modify `org/openmrs/module/mirebalaisreports/definitions/BasePihReportManager.java` and/or `org/openmrs/module/mirebalaisreports/MirebalaisReportsProperties.java`
* for setting/translating labels, modify api/src/main/resources/messages.properties (English).  For adding other language translations (fr, ht, es)
  * push the file to Transifex (cd mirebalaisreport; tx push -s)
  * add translations to the mirebalaisreport module using [Transifex](https://www.transifex.com/pih/mirebalaisreport)
  * pull the translations back into the mirebalaisreport module from Transifex (`cd mirebalaisreport; tx pull; git [add/commit/push]`)

### 2. Using DataReportManager with SQL

See
[MorbidityRegisterReportManager.java](https://github.com/PIH/openmrs-module-mirebalaisreports/blob/master/api/src/main/java/org/openmrs/module/mirebalaisreports/definitions/MorbidityRegisterReportManager.java),
[morbidityRegister.sql](https://github.com/PIH/openmrs-module-mirebalaisreports/blob/master/api/src/main/resources/org/openmrs/module/mirebalaisreports/sql/morbidityRegister.sql),
and
[MorbidityRegisterTemplate.xls](https://github.com/PIH/openmrs-module-mirebalaisreports/blob/master/api/src/main/resources/org/openmrs/module/mirebalaisreports/reportTemplates/MorbidityRegisterTemplate.xls)
for reference.

* Make your own versions of the above three files. You can skip the XLS file if you just want to create a CSV.
* If not using a template, copy the `constructReportDesigns` function used in [NonCodedDiagnosesReportManager.java](https://github.com/PIH/openmrs-module-mirebalaisreports/blob/master/api/src/main/java/org/openmrs/module/mirebalaisreports/definitions/NonCodedDiagnosesReportManager.java)
* The report will be included based on
    * whether your implementation's country is in the `getCountries` list
    * whether your PIH site config has the correct component enabled for the category returned by `getCategory`. For example, if your report's category is `DATA_EXPORT`, then you must have the `dataExports` component enabled for it to show up.
    
### 3. Using DataReportManager with only Java

See
[DailyCheckInsReportManager.java](https://github.com/PIH/openmrs-module-mirebalaisreports/blob/master/api/src/main/java/org/openmrs/module/mirebalaisreports/definitions/DailyCheckInsReportManager.java)
and the associated files for an example of how to do this.

