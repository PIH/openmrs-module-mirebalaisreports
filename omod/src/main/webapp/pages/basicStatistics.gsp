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

<div class=" reportBox">
    <p>${ ui.message("mirebalaisreports.label.today") }</p>
    <ul>
        <li><span class="data">${ startedVisitOnDay }</span> ${ ui.message("mirebalaisreports.label.startedVisits") }</li>
        <li><span class="data">${ activeVisits }</span> ${ ui.message("mirebalaisreports.label.activeVisits") }</li>
        <li><span class="data">${ todayRegistrations }</span> ${ ui.message("mirebalaisreports.label.registrationsToday") }</li>
    </ul>
</div>

<div class="reportBox">
    <p>${ ui.message("mirebalaisreports.label.yesterday") }</p>
    <ul>
        <li><span class="data">${ startedVisitDayBefore }</span> ${ ui.message("mirebalaisreports.label.totalVisits") }</li>
        <li><span class="data">${ outpatientsDayBefore }</span> ${ ui.message("mirebalaisreports.label.outpatientsDayBefore") }</li>
        <li><span class="data">${ outpatientsDayBeforeWithVitals }</span> ${ ui.message("mirebalaisreports.label.outpatientsDayBeforeWithVitals") }</li>
        <li><span class="data">${ outpatientsDayBeforeWithDiagnosis }</span> ${ ui.message("mirebalaisreports.label.outpatientsDayBeforeWithDiagnosis") }</li>
        <li><span class="data">${ outpatientsDayBeforeWithVitalsAndDiagnosis }</span> ${ ui.message("mirebalaisreports.label.outpatientsDayBeforeWithVitalsAndDiagnosis") }</li>
    </ul>
</div>
