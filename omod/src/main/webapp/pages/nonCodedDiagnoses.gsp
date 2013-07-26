<%
    ui.decorateWith("appui", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "home") }" },
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
    <% if (!data) { %>
        <tr>
            <td colspan="3">${ ui.message("mirebalaisreports.none") }</td>
        </tr>
    <% } %>
    <% data.each { %>
        <tr>
            <td>${ ui.escapeHtml(it.getColumnValue("diagnosis")) }</td>
            <td>${ ui.format(it.getColumnValue("creator")) }</td>
            <td>${ ui.format(it.getColumnValue("dateCreated")) }</td>
        </tr>
    <% } %>
    </tbody>
</table>