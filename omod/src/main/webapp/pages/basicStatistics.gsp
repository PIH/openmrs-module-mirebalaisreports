<%
    ui.decorateWith("emr", "standardEmrPage")
    ui.includeCss("mirebalaisreports", "reports.css")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "home") }" },
        { label: "${ ui.message("mirebalaisreports.basicStatistics.title") }", link: "${ ui.thisUrl() }" }
    ];
</script>

<h1>
    ${ ui.message("mirebalaisreports.basicStatistics.title") }
</h1>

<h3>
    ${ ui.message("mirebalaisreports.hospitalPopulation.title") }
</h3>

<div class=" reportBox">
    <p>${ ui.message("mirebalaisreports.label.today") }</p>
    <ul>
        <li>${ startedVisitOnDay } ${ ui.message("mirebalaisreports.label.startedVisits") }</li>
        <li>${ activeVisits } ${ ui.message("mirebalaisreports.label.activeVisits") }</li>
        <li>${ todayRegistrations } ${ ui.message("mirebalaisreports.label.registrationsToday") }</li>
    </ul>
</div>

<div class="reportBox">
    <p>${ ui.message("mirebalaisreports.label.yesterday") }</p>
    <ul>
        <li>${ startedVisitDayBefore } ${ ui.message("mirebalaisreports.label.totalVisits") }</li>
        <li>${ outpatientsDayBefore } ${ ui.message("mirebalaisreports.label.outpatientsDayBefore") }</li>
        <li>${ outpatientsDayBeforeWithVitals } ${ ui.message("mirebalaisreports.label.outpatientsDayBeforeWithVitals") }</li>
        <li>${ outpatientsDayBeforeWithDiagnosis } ${ ui.message("mirebalaisreports.label.outpatientsDayBeforeWithDiagnosis") }</li>
        <li>${ outpatientsDayBeforeWithVitalsAndDiagnosis } ${ ui.message("mirebalaisreports.label.outpatientsDayBeforeWithVitalsAndDiagnosis") }</li>
    </ul>
</div>
