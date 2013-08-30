<%
    ui.decorateWith("appui", "standardEmrPage")
%>
<style type="text/css">
    #run-report {
        padding-top: 0px;
    }

    #run-report button {
        margin-top: 1.5em;
    }
</style>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "home") }" },
        { label: "${ reportManager.name }", link: "${ ui.thisUrl() }" }
    ];

    var toggleSubmitButton = function() {
        if (jq('#startDateField-display').val() && jq('#endDateField-display').val()) {
            jq('#submit').prop('disabled', false).removeClass('disabled');
        }
        else {
            jq('#submit').prop('disabled', true).addClass('disabled');
        }
    };

    jq(function() {
        jq('#startDateField-display, #endDateField-display').change(toggleSubmitButton);
    })

</script>

<div id="reportNameSection" class="title">
    ${ reportManager.name }
</div>

<div id="reportDescriptionSection">
    <small>${ reportManager.description }</small>
</div>

<br/>

<form method="post">
    <fieldset id="run-report">
        <legend>
            ${ ui.message("mirebalaisreports.general.runReport") }
        </legend>
        <% for (int i=0; i<reportManager.parameters.size(); i++) {
            def parameter = reportManager.parameters.get(i); %>
            <p id="parameter${i}Section">
                <% if (parameter.name == "startDate") { %>
                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [ "id": "startDateField", "label": parameter.label, "formFieldName": "startDate", "useTime": false ]) }
                <% } else if (parameter.name == "endDate") { %>
                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [ "id": "endDateField", "label": parameter.label, "formFieldName": "endDate", "useTime": false ]) }
                <% } else if (parameter.name == "location") { %>
                    ${ ui.includeFragment("emr", "field/location", [ "id": "locationField", "label": parameter.label, "formFieldName": "location" ]) }
                <% } %>
            </p>
        <% } %>

        <p>
            <button id="submit" type="submit" class="disabled" disabled>${ ui.message("mirebalaisreports.general.runReport") }</button>
        </p>
    </fieldset>

</form>
