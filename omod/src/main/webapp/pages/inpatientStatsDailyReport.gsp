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

    .wrap-word {
        word-wrap: break-word;
    }

    #view-cohort {
        margin-top: 2em;
    }
</style>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "home") }" },
        { label: "${ ui.message("mirebalaisreports.inpatientStatsDailyReport.title") }", link: "${ ui.pageLink("mirebalaisreports", "inpatientStatsDailyReport") }" }
    ];
</script>

<div ng-app="inpatientStatsDailyReport" ng-controller="InpatientStatsDailyReportController" ng-init="evaluate()">

    <div id="date-header">
        <button class="left" ng-click="previousDay()">
            Previous
        </button>

        <span id="current-date">
            {{ day.format('DD-MMM-YYYY') }}
        </span>

        <button class="right" ng-click="nextDay()" ng-disabled="!day.isBefore(maxDay)">
            Next
        </button>
    </div>

    <div ng-hide="isLoading(day) || hasResults(day)">
        <button ng-click="evaluate()">Evaluate</button>
    </div>

    <div ng-show="isLoading(day)">
        <img src="${ ui.resourceLink("uicommons", "images/spinner.gif") }"/>
    </div>

    <div ng-show="hasResults(day)">
        <table>
            <thead>
                <tr>
                    <th></th>
                    <th ng-repeat="location in locations" class="wrap-word">{{ location.name }}</th>
                </tr>
            </thead>
            <tbody>
                <tr ng-repeat="locationIndicator in locationIndicators">
                    <th class="wrap-word">{{ locationIndicator.label }}</th>
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
            <h3>{{ viewingCohort.indicator.label }} at {{ viewingCohort.location.name }}</h3>

            <table>
                <thead>
                    <th>ZL EMR ID</th>
                    <th>Dossier Number</th>
                </thead>
                <tbody ng-show="viewingCohort.members">
                    <tr ng-repeat="member in viewingCohort.members">
                        <a target="_blank" href="${ ui.pageLink("coreapps", "patientdashboard/patientDashboard") }?patientId={{ member.patientId }}">
                            <td>{{ member.zlEmrId }}</td>
                            <td>{{ member.dossierNumber }}</td>
                        </a>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

</div>