<%
    ui.decorateWith("emr", "standardEmrPage")
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

<table>
    <thead>
    <tr>
        <th>Statistic</th>
        <th>${ ui.message("mirebalaisreports.label.yesterday") }</th>
        <th>${ ui.message("mirebalaisreports.label.today") }</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>${ ui.message("mirebalaisreports.label.startedVisits") }</td>
        <td>${ startedVisitDayBefore }</td>
        <td>${ startedVisitOnDay }</td>
    </tr>
    </tbody>
</table>


<br/>
<table>
    <thead>
    <tr>
        <th>Statistic</th>
        <th>${ ui.message("mirebalaisreports.label.today") }</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>${ ui.message("mirebalaisreports.label.activeVisits") }</td>
        <td>${ activeVisits }</td>
    </tr>
    <tr>
        <td>${ ui.message("mirebalaisreports.label.registrationsToday") }</td>
        <td>${ todayRegistrations }</td>
    </tr>

    </tbody>
</table>