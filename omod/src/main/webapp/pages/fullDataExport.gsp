<%
    ui.decorateWith("appui", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "basicStatistics") }" },
        { label: "${ reportManager.name }", link: "${ ui.thisUrl() }" }
    ];
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
				<% } else if (parameter.name == "whichDataSets") { %>
					<label for="dataSetOptionField">$parameter.label</label>
					<ul id="dataSetOptionField">
						<% for (option in reportManager.dataSetOptions) { %>
							<li>
								<input id="dataSetOption${option}" type="checkbox" name="dataSets" value="${option}" checked="true" style="float:none;"/>
								${ ui.message("mirebalaisreports.fulldataexport."+option+".name") }
								<small>${ ui.message("mirebalaisreports.fulldataexport."+option+".description") }</small>
							</li>
						<% } %>
					</ul>
				<% } %>
			</p>
		<% } %>
	</fieldset>

    <button type="submit">${ ui.message("mirebalaisreports.general.runReport") }</button>
</form>
