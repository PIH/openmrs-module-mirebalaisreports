<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("uicommons", "moment.min.js")
    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("mirebalaisreports", "inpatientStatsDailyReport.js")
%>
<style type="text/css">
    #date-header {
        text-align: center;
    }

    #current-date {
        font-size: 2em;
    }

    #view-cohort {
        margin-top: 2em;
    }
</style>

${ ui.includeFragment("appui", "messages", [ codes: [
        "ui.i18n.Location.name.272bd989-a8ee-4a16-b5aa-55bad4e84f5c",
        "ui.i18n.Location.name.dcfefcb7-163b-47e5-84ae-f715cf3e0e92",
        "ui.i18n.Location.name.e5db0599-89e8-44fa-bfa2-07e47d63546f",
        "ui.i18n.Location.name.2c93919d-7fc6-406d-a057-c0b640104790",
        "ui.i18n.Location.name.62a9500e-a1a5-4235-844f-3a8cc0765d53",
        "ui.i18n.Location.name.c9ab4c5c-0a8a-4375-b986-f23c163b2f69",
        "ui.i18n.Location.name.950852f3-8a96-4d82-a5f8-a68a92043164",
        "ui.i18n.Location.name.7d6cc39d-a600-496f-a320-fd4985f07f0b",
        "mirebalaisreports.inpatientStatsDailyReport.censusAtStart",
        "mirebalaisreports.inpatientStatsDailyReport.admissions",
        "mirebalaisreports.inpatientStatsDailyReport.transfersIn",
        "mirebalaisreports.inpatientStatsDailyReport.transfersOut",
        "mirebalaisreports.inpatientStatsDailyReport.discharged",
        "mirebalaisreports.inpatientStatsDailyReport.deaths",
        "mirebalaisreports.inpatientStatsDailyReport.transfersOutOfHUM",
        "mirebalaisreports.inpatientStatsDailyReport.leftWithoutCompletingTx",
        "mirebalaisreports.inpatientStatsDailyReport.leftWithoutSeeingClinician",
        "mirebalaisreports.inpatientStatsDailyReport.censusAtEnd"
    ]
])}

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "home") }" },
        { label: "${ ui.message("mirebalaisreports.inpatientStatsDailyReport.name") }", link: "${ ui.pageLink("mirebalaisreports", "inpatientStatsDailyReport") }" }
    ];
</script>

<div ng-app="inpatientStatsDailyReport" ng-controller="InpatientStatsDailyReportController" ng-init="evaluate()">

    <div id="date-header">
        <button class="left" ng-click="previousDay()">
            ${ ui.message("uicommons.previous") }
        </button>

        <span id="current-date">
            ${ ui.message("mirebalaisreports.inpatientStatsDailyReport.name") },
            {{ day.format('DD-MMM-YYYY') }}
        </span>

        <button class="right" ng-click="nextDay()" ng-disabled="!day.isBefore(maxDay)">
            ${ ui.message("uicommons.next") }
        </button>
    </div>

    <div ng-hide="isLoading(day) || hasResults(day)">
        <button ng-click="evaluate()">
            ${ ui.message("mirebalaisreports.evaluate") }
        </button>
    </div>

    <div ng-show="isLoading(day)">
        <img src="${ ui.resourceLink("uicommons", "images/spinner.gif") }"/>
    </div>

    <div ng-show="hasResults(day)">
        <table>
            <thead>
                <tr>
                    <th></th>
                    <th ng-repeat="location in locations">{{ location | translate:"ui.i18n.Location.name." }}</th>
                </tr>
            </thead>
            <tbody>
                <tr ng-repeat="locationIndicator in locationIndicators">
                    <th>{{ locationIndicator.name | translate:"mirebalaisreports.inpatientStatsDailyReport." }}</th>
                    <td ng-repeat="location in locations">
                        <a ng-click="viewCohort(day, location, locationIndicator)">
                            {{ dataFor(day).cohorts[locationIndicator.name + ":" + location.uuid].size }}
                        </a>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <div id="view-cohort" ng-show="viewingCohort">

        <img ng-show="viewingCohort.loading" src="${ ui.resourceLink("uicommons", "images/spinner.gif") }"/>

        <div ng-show="viewingCohort.members">
            <h3>{{ viewingCohort.location | translate:"ui.i18n.Location.name." }} {{ viewingCohort.indicator.label }}</h3>

            <table>
                <thead>
                    <th>${ ui.message("ui.i18n.PatientIdentifierType.name.a541af1e-105c-40bf-b345-ba1fd6a59b85") }</th>
                    <th>${ ui.message("ui.i18n.PatientIdentifierType.name.e66645eb-03a8-4991-b4ce-e87318e37566") }</th>
                </thead>
                <tbody ng-show="viewingCohort.members">
                    <tr ng-repeat="member in viewingCohort.members">
                        <td>
                            <a target="_blank" href="${ ui.pageLink("coreapps", "patientdashboard/patientDashboard") }?patientId={{ member.patientId }}">
                                {{ member.zlEmrId }}
                            </a>
                        </td>
                        <td>
                            {{ member.dossierNumber }}
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

</div>