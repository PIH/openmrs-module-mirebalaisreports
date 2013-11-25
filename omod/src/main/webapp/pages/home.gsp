<%
    ui.decorateWith("appui", "standardEmrPage")
	ui.includeCss("mirebalaisreports", "reports.css")

    def breadcrumb = """{ label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.escapeJs(ui.pageLink("mirebalaisreports", "home")) }" }"""

    def linkFor = { uuid ->
        ui.pageLink("reportingui", "runReport", [
            reportDefinition: uuid,
            breadcrumb: breadcrumb
        ])
    }

    def fullDataExportLink = ui.pageLink("reportingui", "runReport", [
            reportDefinition: properties.FULL_DATA_EXPORT_REPORT_DEFINITION_UUID,
            breadcrumb: breadcrumb
    ])

    def dailyReportLink = { reportManager ->
        ui.pageLink("mirebalaisreports", "dailyReport", [ reportDefinition: reportManager.uuid ])
    }
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "home") }" }
    ];
</script>

<div class="reportBox">
	<p>${ ui.message("mirebalaisreports.categories.overviewReports") }</p>
	<ul>
		<li><a id="mirebalaisreports-basicStatisticsReport-link" href="${ ui.pageLink("mirebalaisreports", "basicStatistics") }">${ basicStatisticsReport.name }</a></li>
        <li><a id="mirebalaisreports-dailyRegistrationsReport-link" href="${ dailyReportLink(dailyRegistrationsReport)} ">${ ui.message(dailyRegistrationsReport.name) }</a></li>
        <li><a id="mirebalaisreports-dailyCheckInsReport-link" href="${ dailyReportLink(dailyCheckInsReport)} ">${ ui.message(dailyCheckInsReport.name) }</a></li>
        <li><a id="mirebalaisreports-dailyClinicalEncountersReport-link" href="${ dailyReportLink(dailyClinicalEncountersReport)} ">${ ui.message(dailyClinicalEncountersReport.name) }</a></li>
        <li><a id="mirebalaisreports-inpatientDataExportReport-link" href="${ ui.pageLink("mirebalaisreports", "inpatientStatsDailyReport") }">${ ui.message("mirebalaisreports.inpatientStatsDailyReport.name") }</a></li>
	</ul>

    <p>${ ui.message("mirebalaisreports.categories.dataQualityReports") }</p>
    <ul>
        <li><a id="mirebalaisreports-nonCodedDiagnosesReport-link" href="${ ui.pageLink("mirebalaisreports", "nonCodedDiagnoses") }">${ nonCodedDiagnosesReport.name }</a></li>
    </ul>
</div>

<% if (context.hasPrivilege("App: mirebalaisreports.dataexports")) { %>
<div class="reportBox">
	<p>${ ui.message("mirebalaisreports.categories.dataExports") }</p>
	<ul>
		<li><a id="mirebalaisreports-fullDataExportReport-link" href="${ linkFor(properties.FULL_DATA_EXPORT_REPORT_DEFINITION_UUID) }">${ ui.message("mirebalaisreports.fulldataexport.name") }</a></li>
        <li><a id="mirebalaisreports-dashboardDataExportReport-link" href="${ linkFor(properties.DASHBOARD_DATA_EXPORT_REPORT_DEFINITION_UUID) }">${ ui.message("mirebalaisreports.dashboarddataexport.name") }</a></li>
        <li><a id="mirebalaisreports-radiologyDataExportReport-link" href="${ linkFor(properties.RADIOLOGY_DATA_EXPORT_REPORT_DEFINITION_UUID) }">${ ui.message("mirebalaisreports.radiologydataexport.name") }</a></li>
        <li><a id="mirebalaisreports-surgeryDataExportReport-link" href="${ linkFor(properties.SURGERY_DATA_EXPORT_REPORT_DEFINITION_UUID) }">${ ui.message("mirebalaisreports.surgerydataexport.name") }</a></li>
        <li><a id="mirebalaisreports-hospitalizationsDataExportReport-link" href="${ linkFor(properties.HOSPITALIZATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID) }">${ ui.message("mirebalaisreports.hospitalizationsdataexport.name") }</a></li>
        <li><a id="mirebalaisreports-consultationsDataExportReport-link" href="${ linkFor(properties.CONSULTATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID) }">${ ui.message("mirebalaisreports.consultationsdataexport.name") }</a></li>
        <li><a id="mirebalaisreports-patientsDataExportReport-link" href="${ linkFor(properties.PATIENTS_DATA_EXPORT_REPORT_DEFINITION_UUID) }">${ ui.message("mirebalaisreports.patientsdataexport.name") }</a></li>

        <li><a id="mirebalaisreports-lqasDiagnosesReport-link" href="${ ui.pageLink("mirebalaisreports", "lqasDiagnoses") }">${ lqasDiagnosesReport.name }</a></li>
        <li><a id="mirebalaisreports-allPatientsWithIdsReport-link" href="${ ui.pageLink("mirebalaisreports", "allPatientsWithIds") }">${ allPatientsWithIdsReportManager.name }</a></li>
	</ul>
</div>
<% } %>