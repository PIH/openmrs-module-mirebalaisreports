<%
    ui.decorateWith("emr", "standardEmrPage")
    ui.includeCss("mirebalaisreports", "reports.css")

    def numerator = { indicator ->
        return indicator.cohortIndicatorAndDimensionCohort.size()
    }

    def denominator = { indicator ->
        return indicator.cohortIndicatorAndDimensionDenominator.size()
    }

    def percentage = { indicator ->
        def denom = denominator(indicator)
        if (denom) {
            return Math.round(100 * (numerator(indicator) / (double) denom))
        } else {
            return 0;
        }
    }
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.thisUrl() }" }
    ];
</script>

<h1>
    ${ ui.message("mirebalaisreports.basicStatistics.title") }
</h1>

<div class="reportBox">
    <p>${ ui.message("mirebalaisreports.basicStatistics.label.today") }</p>
    <ul>
        <li>
            <span class="data">${ startedVisitOnDay.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.totalVisits") }</span>
        </li>
        <li>
            <span class="data">${ todayRegistrations.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.newlyRegisteredPatients") }</span>
        </li>
        <li>
            <span class="data">${ returningPatientsOnDay.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.returningPatients") }</span>
        </li>    
    </ul>
    <ul>
        <li>
            <span class="data">${ outpatientOnDay.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.keVisits") }</span>
        </li>
        <li>
            <span class="data">
                <span>
                    ${ percentage(outpatientWithVitalsOnDay) }%
                </span>
                <span class="number">
                    (${ numerator(outpatientWithVitalsOnDay) }/${ denominator(outpatientWithVitalsOnDay) })
                </span>
            </span>
            <span class="label"><i class="icon-angle-right small"></i> ${ ui.message("mirebalaisreports.basicStatistics.label.outpatientsDayBeforeWithVitals") }</span>
        </li>
        <li>
            <span class="data">
                <span>
                    ${ percentage(outpatientWithDiagnosisOnDay) }%
                </span>
                <span class="number">
                    (${ numerator(outpatientWithDiagnosisOnDay) }/${ denominator(outpatientWithDiagnosisOnDay) })
                </span>
            </span>
            <span class="label"><i class="icon-angle-right small"></i> ${ ui.message("mirebalaisreports.basicStatistics.label.outpatientsDayBeforeWithDiagnosis") }</span>
        </li>
        <li>
            <span class="data">${ womenOnDay.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.sfVisits") }</span>
        </li>
        <li>
            <span class="data">
                <span>
                    ${ percentage(womenWithVitalsOnDay) }%
                </span>
                <span class="number">
                    (${ numerator(womenWithVitalsOnDay) }/${ denominator(womenWithVitalsOnDay) })
                </span>
            </span>
            <span class="label"><i class="icon-angle-right small"></i> ${ ui.message("mirebalaisreports.basicStatistics.label.outpatientsDayBeforeWithVitals") }</span>
        </li>
        <li>
            <span class="data">
                <span>
                    ${ percentage(womenWithDiagnosisOnDay) }%
                </span>
                <span class="number">
                    (${ numerator(womenWithDiagnosisOnDay) }/${ denominator(womenWithDiagnosisOnDay) })
                </span>
            </span>
            <span class="label"><i class="icon-angle-right small"></i> ${ ui.message("mirebalaisreports.basicStatistics.label.outpatientsDayBeforeWithDiagnosis") }</span>
        </li>
    </ul>
</div>

<div class="reportBox">
    <p>${ ui.message("mirebalaisreports.basicStatistics.label.yesterday") }</p>
    <ul>
        <li>
            <span class="data">${ startedVisitDayBefore }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.totalVisits") }</span>
        </li>
        <li>
            <span class="data">${ yesterdayRegistrations.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.newlyRegisteredPatients") }</span>
        </li>
        <li>
            <span class="data">${ returningPatientsOnDayBefore.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.returningPatients") }</span>
        </li>  
    </ul>
    <ul>
        <li>
            <span class="data">${ outpatientOnDayBefore.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.keVisits") }</span>
        </li>
        <li>
            <span class="data">
                <span>
                    ${ percentage(outpatientWithVitalsOnDayBefore) }%
                </span>
                <span class="number">
                    (${ numerator(outpatientWithVitalsOnDayBefore) }/${ denominator(outpatientWithVitalsOnDayBefore) })
                </span>
            </span>
            <span class="label"><i class="icon-angle-right small"></i> ${ ui.message("mirebalaisreports.basicStatistics.label.outpatientsDayBeforeWithVitals") }</span>
        </li>
        <li>
            <span class="data">
                <span>
                    ${ percentage(outpatientWithDiagnosisOnDayBefore) }%
                </span>
                <span class="number">
                    (${ numerator(outpatientWithDiagnosisOnDayBefore) }/${ denominator(outpatientWithDiagnosisOnDayBefore) })
                </span>
            </span>
            <span class="label"><i class="icon-angle-right small"></i> ${ ui.message("mirebalaisreports.basicStatistics.label.outpatientsDayBeforeWithDiagnosis") }</span>
        </li>
        <li>
            <span class="data">${ womenOnDayBefore.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.sfVisits") }</span>
        </li>
        <li>
            <span class="data">
                <span>
                    ${ percentage(womenWithVitalsOnDayBefore) }%
                </span>
                <span class="number">
                    (${ numerator(womenWithVitalsOnDayBefore) }/${ denominator(womenWithVitalsOnDayBefore) })
                </span>
            </span>
            <span class="label"><i class="icon-angle-right small"></i> ${ ui.message("mirebalaisreports.basicStatistics.label.outpatientsDayBeforeWithVitals") }</span>
        </li>
        <li>
            <span class="data">
                <span>
                    ${ percentage(womenWithDiagnosisOnDayBefore) }%
                </span>
                <span class="number">
                    (${ numerator(womenWithDiagnosisOnDayBefore) }/${ denominator(womenWithDiagnosisOnDayBefore) })
                </span>
            </span>
            <span class="label"><i class="icon-angle-right small"></i> ${ ui.message("mirebalaisreports.basicStatistics.label.outpatientsDayBeforeWithDiagnosis") }</span>
        </li>         
    </ul>
</div>

<div class="reportLinksBox">
</div>

<div class="reportLinksBox">
    <p>${ ui.message("mirebalaisreports.basicStatistics.dataQualityReports") }</p>
    <ul>
        <li><a href="${ ui.pageLink("mirebalaisreports", "noncodeddiagnoses/report") }">${ ui.message("mirebalaisreports.noncodeddiagnoses.title") }</a></li>
    </ul>
</div>