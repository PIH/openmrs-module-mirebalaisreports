<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("mirebalais", "inpatient.css")
    def inpatientsNumber = 0;
    if (inpatientsList != null ){
        inpatientsNumber = inpatientsList.size();
    }
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("emr.app.inpatients.label")}"}
    ];

    var WARD_COLUMN_INDEX = 4;

    jq(function() {
        jq("#inpatients-filterByLocation").change(function(event){
            var ward= "";
            var selectedItemId="";
            var inpatientsTable = jq("#active-visits").dataTable();
            jq("select option:selected").each(function(){
                ward = jq(this).text();
                //ward = "Men"; //jq(this).text();
                ward = ward.replace(/'/g, "\\’");
                selectedItemId =this.value;
                if (ward.length > 0) {
                    console.log("ward=" + ward);
                    inpatientsTable.fnFilter(ward, WARD_COLUMN_INDEX);
                }else {
                    inpatientsTable.fnFilter('', WARD_COLUMN_INDEX);
                }
                jq("#listSize").text(inpatientsTable.fnSettings().fnRecordsDisplay());
            });
        });
    });

</script>

<h3 class="inpatient-count">${ ui.message("emr.inpatients.patientCount") }: <span id="listSize">${inpatientsNumber}</span></h3>
<div class="inpatient-filter">
    ${ ui.includeFragment("emr", "field/location", [
            "id": "inpatients-filterByLocation",
            "formFieldName": "filterByLocationId",
            "label": "emr.inpatients.filterByCurrentWard",
            "withTag": "Admission Location;Transfer Location"
    ] ) }
</div>

<table id="active-visits" width="100%" border="1" cellspacing="0" cellpadding="2">
    <thead>
    <tr>
        <th>${ ui.message("emr.patient.identifier") }</th>
        <th>${ ui.message("ui.i18n.PatientIdentifierType.name.e66645eb-03a8-4991-b4ce-e87318e37566") }</th>
        <th>${ ui.message("emr.person.name") }</th>
        <th>${ ui.message("emr.inpatients.firstAdmitted") }</th>
        <th>${ ui.message("emr.inpatients.currentWard") }</th>
    </tr>
    </thead>
    <tbody>
    <% if ((inpatientsList == null) || (inpatientsList != null && inpatientsList.size() == 0)) { %>
    <tr>
        <td colspan="4">${ ui.message("emr.none") }</td>
    </tr>
    <% } %>
    <% inpatientsList.each { v ->
    %>
    <tr id="visit-${ v.patientId
    }">
        <td>${ v.zlEmrId ?: ''}</td>
        <td>${ v.dossierNumber ?: ''}</td>
        <td>
            <a href="${ ui.pageLink("coreapps", "patientdashboard/patientDashboard", [ patientId: v.patientId ]) }">
                ${ ui.format((v.givenName ? v.givenName : '') + " " + (v.familyName ? v.familyName : '')) }
            </a>
        </td>
        <td>
            ${ ui.format( v.firstAdmittedLocation) }
            <br/>
            <small>
                ${ ui.format(v.admissionDateTime)}
            </small>
        </td>
        <td>
            ${ ui.format( v.inpatientLocation) }
            <br/>
            <small>
                ${ ui.format(v.inpatientDateTime) }
            </small>
        </td>
    </tr>
    <% } %>
    </tbody>
</table>

<% if ( (inpatientsList != null) && (inpatientsList.size() > 0) ) { %>
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