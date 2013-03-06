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
    Diagnoses entered as free-text, from ${ ui.format(fromDate) } to ${ ui.format(toDate) }
</h3>

<table>
    <thead>
        <tr>
            <th>Diagnosis</th>
            <th>Entered By</th>
            <th>Entry Date</th>
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