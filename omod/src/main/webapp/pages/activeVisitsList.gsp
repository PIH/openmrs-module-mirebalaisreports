<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("mirebalais", "inpatient.css")

%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.activeVisits.label")}"}
    ];


    jq(document).ready(function() {

    });

</script>

<h3>${ ui.message("coreapps.activeVisits.title") }</h3>

<table id="active-visits" width="100%" border="1" cellspacing="0" cellpadding="2">
    <thead>
    <tr>
        <th>${ ui.message("coreapps.patient.identifier") }</th>
        <th>${ ui.message("coreapps.person.name") }</th>
        <th>${ ui.message("coreapps.activeVisits.checkIn") }</th>
        <th>${ ui.message("coreapps.activeVisits.lastSeen") }</th>
    </tr>
    </thead>
    <tbody>
    <% if (activeVisitsList == null || (activeVisitsList !=null && activeVisitsList.size() == 0) ) { %>
    <tr>
        <td colspan="4">${ ui.message("coreapps.none") }</td>
    </tr>
    <% } %>
    <% activeVisitsList.each { v ->

    %>
    <tr id="visit-${ v.patientId }">
        <td>${ ui.encodeHtmlContent(ui.format(v.zlEmrId ?: '')) }</td>
        <td>
            <% if (sessionContext.currentUser.hasPrivilege(privilegePatientDashboard)) { %>
            <!-- only add link to patient dashboard if user has appropriate privilege -->
            <a href="${ ui.urlBind("/" + contextPath + dashboardUrl, [ patientId: v.patientId ]) }">
                <% } %>

                ${ ui.format((v.givenName ? v.givenName : '') + " " + (v.familyName ? v.familyName : '')) }

                <% if (sessionContext.currentUser.hasPrivilege(privilegePatientDashboard)) { %>
                <!-- only add link to patient dashboard if user has appropriate privilege -->
            </a>
            <% } %>

        </td>
        <td>
            <small>
                ${ ui.encodeHtmlContent(ui.format(v.firstCheckinLocation)) } @ ${ ui.format(v.checkinDateTime) }
            </small>
        </td>
        <td>
            ${ ui.encodeHtmlContent(ui.format(v.lastEncounterType)) }
            <br/>
            <small>
                ${ ui.encodeHtmlContent(ui.format(v.lastEncounterLocation)) } @ ${ ui.format(v.lastEncounterDateTime) }
            </small>

        </td>

    </tr>
    <% } %>
    </tbody>
</table>

<% if (activeVisitsList !=null && activeVisitsList.size() > 0) { %>
${ ui.includeFragment("uicommons", "widget/dataTable", [ object: "#active-visits",
                                                         options: [
                                                                 bFilter: true,
                                                                 bJQueryUI: true,
                                                                 bLengthChange: false,
                                                                 iDisplayLength: 10,
                                                                 sPaginationType: '\"full_numbers\"',
                                                                 bSort: false,
                                                                 sDom: '\'ft<\"fg-toolbar ui-toolbar ui-corner-bl ui-corner-br ui-helper-clearfix datatables-info-and-pg \"ip>\''
                                                         ]
]) }
<% } %>