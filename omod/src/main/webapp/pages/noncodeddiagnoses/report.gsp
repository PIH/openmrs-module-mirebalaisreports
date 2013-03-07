<%
    ui.decorateWith("emr", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "basicStatistics") }" },
        { label: "${ ui.message("mirebalaisreports.noncodeddiagnoses.title") }", link: "${ ui.thisUrl() }" }
    ];
</script>

<h1>
    ${ ui.message("mirebalaisreports.noncodeddiagnoses.title") }
</h1>

<h3>
    ${ ui.message("mirebalaisreports.noncodeddiagnoses.subtitle", ui.format(fromDate), ui.format(toDate)) }
</h3>

<table>
    <thead>
        <tr>
            <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.diagnosis") }</th>
            <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.enteredBy") }</th>
            <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.entryDate") }</th>
        </tr>
    </thead>
    <tbody>
    <% if (!list) { %>
        <tr>
            <td colspan="3">${ ui.message("emr.none") }</td>
        </tr>
    <% } %>
    <% list.each { %>
        <tr>
            <td>${ ui.escapeHtml(it.diagnosis) }</td>
            <td>${ ui.format(it.creator) }</td>
            <td>${ ui.format(it.dateCreated) }</td>
        </tr>
    <% } %>
    </tbody>
</table>