var app = angular.module('inpatientStatsDailyReport', ['ui.bootstrap']).

filter('translate', function() {
    return function(input, prefix) {
        if (input && input.uuid) {
            input = input.uuid;
        }
        var code = prefix ? prefix + input : input;
        return emr.message(code, input);
    }
}).

controller('InpatientStatsDailyReportController', ['$scope', '$http', '$timeout', function($scope, $http, $timeout) {

    $scope.indicators = [
        { name: "edcheckin", class: "indicator-in" },
        { name: "orvolume", class: "indicator-total" },
        { name: "possiblereadmission", class: "indicator-in" }
    ];

    $scope.locationIndicators = [
        { name: "censusAtStart", label: "Census at start", class: "indicator-total" },
        { name: "admissions", label: "Admissions", class: "indicator-in" },
        { name: "transfersIn", label: "Transfers In", class: "indicator-in" },
        { name: "transfersOut", label: "Transfers Out", class: "indicator-out" },
        { name: "discharged", label: "Discharged", class: "indicator-out" },
        { name: "deaths", label: "Died", class: "indicator-out" },
        { name: "transfersOutOfHUM", label: "Transferred out of HUM", class: "indicator-out" },
        { name: "leftWithoutCompletingTx", label: "Left without completing treatment", class: "indicator-out" },
        { name: "leftWithoutSeeingClinician", label: "Left without seeing a clinician", class: "indicator-out" },
        { name: "censusAtEnd", label: "Census at end", class: "indicator-total" }
    ];

    $scope.locations = [
        { uuid: "272bd989-a8ee-4a16-b5aa-55bad4e84f5c", name: "Antepartum Ward" },
        { uuid: "dcfefcb7-163b-47e5-84ae-f715cf3e0e92", name: "Labor and Delivery" },
        { uuid: "950852f3-8a96-4d82-a5f8-a68a92043164", name: "Postpartum Ward" },
        { uuid: "62a9500e-a1a5-4235-844f-3a8cc0765d53", name: "NICU" },
        { uuid: "c9ab4c5c-0a8a-4375-b986-f23c163b2f69", name: "Pediatrics" },
        { uuid: "2c93919d-7fc6-406d-a057-c0b640104790", name: "Women's Internal Medicine" },
        { uuid: "e5db0599-89e8-44fa-bfa2-07e47d63546f", name: "Men’s Internal Medicine" },
        { uuid: "7d6cc39d-a600-496f-a320-fd4985f07f0b", name: "Surgical Ward" }
    ];

    $scope.data = { };

    $scope.viewingCohort = null;

    $scope.$watch('day', function() {
        $scope.viewingCohort = null;
    });

    $scope.maxDay = moment().startOf('day').toDate();

    $scope.day = moment().subtract('day', 1).startOf('day').toDate();

    $scope.dataFor = function(day) {
        if($scope.data[day] == null) {
            evaluate(day);
        }

        return $scope.data[day];
    };

    $scope.openDatePicker = function() {
      $timeout(function() {
        $scope.isDatePickerOpen = true;
      });
    };

    $scope.isLoading = function(day) {
        return $scope.dataFor(day) && $scope.dataFor(day).loading;
    };

    $scope.hasResults = function(day) {
        return $scope.dataFor(day) && $scope.dataFor(day).evaluationContext;
    };

    function evaluate(forDay) {
        forDay = new Date(forDay.getTime());
        if ($scope.data[forDay]) {
            return;
        }

        $scope.data[forDay] = { loading: true };

        $http.get(emr.fragmentActionLink('mirebalaisreports', 'inpatientStatsDailyReport', 'evaluate',
                {
                    day: moment(forDay).format('YYYY-MM-DD')
                })).
            success(function(data, status, headers, config) {
                $scope.data[forDay] = data;
            });
    }

    $scope.viewCohort = function(day, indicator, location) {
        day = new Date(day.getTime());

        var indicatorName = indicator.name;
        if (location) {
            indicatorName += ":" + location.uuid;
        }

        var ptIds = $scope.dataFor(day).cohorts[indicatorName].patientIds;
        if (ptIds == null || ptIds.length == 0) {
            $scope.viewingCohort = null;
            return;
        }

        $scope.viewingCohort = { loading: true };

        $http.get(emr.fragmentActionLink('mirebalaisreports', 'cohort', 'getCohort',
                {
                    memberIds: ptIds
                })).
            success(function(data, status, headers, config) {
                $scope.viewingCohort = {
                    location: location,
                    indicator: indicator,
                    members: data.members
                };
            });
    }
}]);
