<%
    ui.decorateWith("appui", "standardEmrPage")
%>

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
        jq('#startDateField-display').change(toggleSubmitButton);
        jq('#endDateField-display').change(toggleSubmitButton);
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
    <fieldset style="min-width:25%; padding-top:0px;">
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
                <% } %>
            </p>
        <% } %>
    </fieldset>

    <button id="submit" type="submit" class="disabled" disabled>${ ui.message("mirebalaisreports.general.runReport") }</button>
</form>
