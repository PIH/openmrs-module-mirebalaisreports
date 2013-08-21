<%
    ui.decorateWith("appui", "standardEmrPage")
    // ui.includeJavascript("mirebalaisreports", "nonCodedDiagnoses.js")
    ui.includeJavascript("coreapps", "fragments/datamanagement/codeDiagnosisDialog.js")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "home") }" },
        { label: "${ ui.message("mirebalaisreports.noncodeddiagnoses.title") }", link: "${ ui.thisUrl() }" }
    ];
</script>

<script type="text/javascript">

    jq(function() {

        jq(".codeDiagnosis").click(function(event) {
            createCodeDiagnosisDialog();
            var patientId = jq(event.target).attr("data-patient-id");
            var patientIdentifier = jq(event.target).attr("data-patient-identifier");
            var nonCodedDiagnosis = jq(event.target).attr("data-nonCoded-Diagnosis");
            var personName = jq(event.target).attr("data-person-name");
            var obsId = jq(event.target).attr("data-obs-id");

            showCodeDiagnosisDialog(patientId, patientIdentifier, personName, obsId, nonCodedDiagnosis);
        });


    });

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
            <th>${ ui.message("coreapps.patient.identifier") }</th>
            <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.enteredBy") }</th>
            <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.entryDate") }</th>
            <th>${ ui.message("coreapps.dataManagement.codeDiagnosis.title") }</th>
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
            <td>
                <a href="${ ui.pageLink("coreapps", "patientdashboard/patientDashboard", [ patientId: it.getColumnValue("patientId") ]) }">
                    ${ ui.format(it.getColumnValue("patientIdentifier")) }
                </a>
            </td>
            <td>${ ui.format(it.getColumnValue("creator")) }</td>
            <td>${ ui.format(it.getColumnValue("dateCreated")) }</td>
            <td>
                <a class="codeDiagnosis"
                   data-patient-identifier="${ it.getColumnValue("patientIdentifier") }"
                   data-patient-id="${ it.getColumnValue("patientId") }"
                   data-person-name="${ it.getColumnValue("personName") }"
                   data-nonCoded-Diagnosis="${ ui.escapeHtml(it.getColumnValue("diagnosis")) }"
                   data-obs-id ="${ it.getColumnValue("obsId") }"
                   href="#${ it.getColumnValue("patientId") }">
                    ${ ui.message("coreapps.dataManagement.codeDiagnosis.title") }
                </a>
            </td>
        </tr>
    <% } %>
    </tbody>
</table>

${ ui.includeFragment("coreapps", "datamanagement/codeDiagnosisDialog") }