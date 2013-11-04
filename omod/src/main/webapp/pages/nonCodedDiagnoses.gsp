<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("coreapps", "fragments/datamanagement/codeDiagnosisDialog.js")
    ui.includeJavascript("uicommons", "datatables/jquery.dataTables.min.js")

    def providerOptions = []
    providers.each {
        providerOptions.push( [label: ui.format(it), value: it.id])
    }
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "home") }" },
        { label: "${ ui.message("mirebalaisreports.noncodeddiagnoses.title") }", link: "${ ui.thisUrl() }" }
    ];
</script>

<script type="text/javascript">

    var toggleSubmitButton = function() {
        if (jq('#startDateField-display').val() || jq('#endDateField-display').val() ||
                jq('#nonCodedField-display').val() ||
                jq('#providerField-field :selected').text()) {
            jq('#submit').prop('disabled', false).removeClass('disabled');
        }
        else {
            jq('#submit').prop('disabled', true).addClass('disabled');
        }
    };

    jq(function() {
                
        jq('#startDateField-display, #endDateField-display, #nonCodedField-display, #providerField-field').change(toggleSubmitButton);

        jq('#nonCodedForm').submit(function() {
            var nonCoded = jq('#nonCodedField-display').val();
            if (nonCoded.length > 0) {
                jq('#nonCodedField-field').val(nonCoded);
            }

            return true;
        })

        jq(".codeDiagnosis").click(function(event) {
            createCodeDiagnosisDialog();
            patientDashboardLink ='${ui.pageLink("coreapps", "patientdashboard/patientDashboard")}';
            instructionsTemplate ='${ ui.escapeJs(ui.message("coreapps.dataManagement.replaceNonCoded")) }';
            var patientId = jq(event.target).attr("data-patient-id");
            var visitId = jq(event.target).attr("data-visit-id");
            var patientIdentifier = jq(event.target).attr("data-patient-identifier");
            var nonCodedDiagnosis = jq(event.target).attr("data-nonCoded-Diagnosis");
            var personName = jq(event.target).attr("data-person-name");
            var obsId = jq(event.target).attr("data-obs-id");

            showCodeDiagnosisDialog(patientId, visitId, patientIdentifier, personName, obsId, nonCodedDiagnosis);
        });


    });

</script>

<h1>
    ${ ui.message("mirebalaisreports.noncodeddiagnoses.title") }
</h1>

<form id="nonCodedForm" method="post">
    <fieldset id="run-report">
        <legend>
            ${ ui.message("mirebalaisreports.general.runReport") }
        </legend>
        <% for (int i=0; i<reportManager.parameters.size(); i++) {
            def parameter = reportManager.parameters.get(i); %>
        <p id="parameter${i}Section">
            <% if (parameter.name == "fromDate") { %>
            ${ ui.includeFragment("uicommons", "field/datetimepicker", [ "id": "startDateField", "label": parameter.label, "formFieldName": "fromDate", "useTime": false ]) }
            <% } else if (parameter.name == "toDate") { %>
            ${ ui.includeFragment("uicommons", "field/datetimepicker", [ "id": "endDateField", "label": parameter.label, "formFieldName": "toDate", "useTime": false ]) }
            <% } else if (parameter.name == "nonCoded") { %>
                <p id="nonCodedField">
                    <label for="nonCodedField-display">
                        ${ ui.message("mirebalaisreports.noncodeddiagnoses.nonCodedDiagnosis") }
                    </label>
                    <span id="nonCodedField-wrapper">
                        <input type="text" id="nonCodedField-display" value=""  />
                    </span>
                    <input type="hidden" id="nonCodedField-field" name="nonCoded" />
                </p>

             <% } else if (parameter.name == "provider") { %>
                ${ ui.includeFragment("uicommons", "field/dropDown", [  "id": "providerField", label: ui.message("mirebalaisreports.noncodeddiagnoses.enteredBy"), formFieldName: "provider", initialValue: '', options: providerOptions ])}
             <% }  %>

        </p>
        <% } %>

        <p>
            <button id="submit" type="submit" class="disabled confirm" disabled>${ ui.message("mirebalaisreports.general.runReport") }</button>
        </p>
    </fieldset>

</form>

<h3>
    ${ ui.message("mirebalaisreports.noncodeddiagnoses.subtitle", ui.format(fromDate), ui.format(toDate)) }
</h3>

<table id="non-coded-diagnoses">
    <thead>
        <tr>
            <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.nonCodedDiagnosis") }</th>
            <th>${ ui.message("coreapps.patient.identifier") }</th>
            <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.enteredBy") }</th>
            <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.entryDate") }</th>
            <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.encounterDateTime") }</th>
            <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.createDiagnosis") }</th>
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
            <td class="non-coded-diagnoses-td">${ ui.escapeHtml(it.getColumnValue("diagnosis")) }</td>
            <td>
                <a href="${ ui.pageLink("coreapps", "patientdashboard/patientDashboard", [ patientId: it.getColumnValue("patientId") , visitId: it.getColumnValue("visitId") ]) }">
                    ${ ui.format(it.getColumnValue("patientIdentifier")) }
                </a>
            </td>
            <td>${ ui.format(it.getColumnValue("creator")) }</td>
            <td>${ ui.format(it.getColumnValue("dateCreated")) }</td>
            <td>${ ui.format(it.getColumnValue("encounterDateTime")) }</td>
            <td>
                <a class="codeDiagnosis"
                   data-patient-identifier="${ it.getColumnValue("patientIdentifier") }"
                   data-patient-id="${ it.getColumnValue("patientId") }"
                   data-visit-id="${ it.getColumnValue("visitId") }"
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

${ ui.includeFragment("uicommons", "widget/dataTable", [ object: "#non-coded-diagnoses",
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

${ ui.includeFragment("coreapps", "datamanagement/codeDiagnosisDialog") }