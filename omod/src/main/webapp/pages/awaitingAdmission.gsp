<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("mirebalais", "inpatient.css")
    def awaitingAdmissionNumber = 0;
    if (awaitingAdmissionList != null ){
        awaitingAdmissionNumber = awaitingAdmissionList.size();
    }
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.awaitingAdmission.label")}"}
    ];

    var WARD_COLUMN_INDEX = 5;
    var inpatientsTable = jq("#active-visits").dataTable();

    jq(document).ready(function() {
        var ward= "";
        jq.fn.dataTableExt.afnFiltering.push(
                function (oSettings, aData, iDataIndex) {
                    var currentWard = aData[WARD_COLUMN_INDEX];
                    currentWard = currentWard.replace(/'/g, "\\’");
                    if (ward.length < 1 ){
                        return true;
                    }else if (currentWard.match(new RegExp(ward)) != null ){
                        return true;
                    }
                    return false;
                }
        );
        jq("#inpatients-filterByLocation").change(function(event){
            var selectedItemId="";
            jq("select option:selected").each(function(){
                ward = jq(this).text();
                ward = ward.replace(/'/g, "\\’");
                selectedItemId =this.value;
                if (ward.length > 0) {
                    jq('#active-visits').dataTable({ "bRetrieve": true }).fnDraw();
                } else {
                    jq('#active-visits').dataTable({ "bRetrieve": true }).fnFilter('', WARD_COLUMN_INDEX);
                }

                jq("#listSize").text(jq('#active-visits').dataTable({ "bRetrieve": true }).fnSettings().fnRecordsDisplay());
            });
        });
    });

</script>

<h3 class="inpatient-count">${ ui.message("emr.inpatients.patientCount") }: <span id="listSize">${awaitingAdmissionNumber}</span></h3>
<div class="inpatient-filter">
    ${ ui.includeFragment("emr", "field/location", [
            "id": "inpatients-filterByLocation",
            "formFieldName": "filterByLocationId",
            "label": "mirebalais.awaitingAdmission.filterByAdmittedTo",
            "withTag": "Admission Location"
    ] ) }
</div>

<table id="active-visits" width="100%" border="1" cellspacing="0" cellpadding="2">
    <thead>
    <tr>
        <th>${ ui.message("emr.patient.identifier") }</th>
        <th>${ ui.message("ui.i18n.PatientIdentifierType.name.e66645eb-03a8-4991-b4ce-e87318e37566") }</th>
        <th>${ ui.message("emr.person.name") }</th>
        <th>${ ui.message("emr.inpatients.currentWard") }</th>
        <th>${ ui.message("emr.patientDashBoard.provider") }</th>
        <th>${ ui.message("disposition.emrapi.admitToHospital.admissionLocation") }</th>
        <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.diagnosis") }</th>

    </tr>
    </thead>
    <tbody>
    <% if ((awaitingAdmissionList == null) || (awaitingAdmissionList != null && awaitingAdmissionList.size() == 0)) { %>
    <tr>
        <td colspan="4">${ ui.message("emr.none") }</td>
    </tr>
    <% } %>
    <% awaitingAdmissionList.each { v ->
    %>
    <tr id="visit-${ v.patientId
    }">
        <td>${ v.primaryIdentifier ?: ''}</td>
        <td>${ v.dossierNumber ?: ''}</td>
        <td>
            <a href="${ ui.pageLink("coreapps", "patientdashboard/patientDashboard", [ patientId: v.patientId ]) }">
                ${ ui.format((v.patientFirstName ? v.patientFirstName : '') + " " + (v.patientLastName ? v.patientLastName : '')) }
            </a>
        </td>
        <td>
            ${ ui.message("ui.i18n.Location.name." + v.consultationLocationUuid) }
            <br/>
            <small>
                ${ ui.format(v.consultationDateTime)}
            </small>
        </td>
        <td>
            ${ ui.format((v.providerFirstName ? v.providerFirstName : '') + " " + (v.providerLastName ? v.providerLastName : '')) }
        </td>
        <td>${ ui.message("ui.i18n.Location.name." + v.admissionLocationUuid) }</td>
        <td>${ v.diagnosis ?: ''}</td>
    </tr>
    <% } %>
    </tbody>
</table>

<% if ( (awaitingAdmissionList != null) && (awaitingAdmissionList.size() > 0) ) { %>
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