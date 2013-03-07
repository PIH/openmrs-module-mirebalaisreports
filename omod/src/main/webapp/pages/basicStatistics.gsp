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
    <p>${ ui.message("mirebalaisreports.basicStatistics.hospitalUtilization") }</p>
    <ul>
        <li>
            <span class="data">${ activeVisits.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.activeVisits") }</span>
        </li>
        <li>
            <span class="data">${ todayRegistrations.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.todayRegistrations") }</span>
        </li>
        <li>
            <span class="data">${ startedVisitOnDay.value }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.startedVisitOnDay") }</span>
        </li>
        <li>
            <span class="data">${ startedVisitDayBefore }</span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.startedVisitDayBefore") }</span>
        </li>
    </ul>
</div>

<div class="reportBox">
    <p>${ ui.message("mirebalaisreports.basicStatistics.dataQuality") }</p>
    <ul>
        <li>
            <span class="data">
                ${ outpatientsDayBefore.value }
            </span>
            <span class="label">${ ui.message("mirebalaisreports.basicStatistics.label.outpatientsDayBefore") }</span>
        </li>
        <li>
            <span class="data">
                <span>
                    ${ percentage(outpatientsDayBeforeWithClinical) }%
                </span>
                <span class="number">
                    (${ numerator(outpatientsDayBeforeWithClinical) }/${ denominator(outpatientsDayBeforeWithClinical) })
                </span>
            </span>
            <span class="label"><i class="icon-angle-right small"></i> with any clinical encounter</span>
        </li>
        <%
        def indicators = [
            [ label: ui.message("mirebalaisreports.basicStatistics.label.outpatientsDayBeforeWithVitals"), value: outpatientsDayBeforeWithVitals ],
            [ label: ui.message("mirebalaisreports.basicStatistics.label.outpatientsDayBeforeWithDiagnosis"), value: outpatientsDayBeforeWithDiagnosis ],
        ]
        indicators.each {
        %>
            <li>
                <span class="data">
                    <span>
                        ${ percentage(it.value) }%
                    </span>
                    <span class="number">
                        (${ numerator(it.value) }/${ denominator(it.value) })
                    </span>
                </span>
                <span class="label"><i class="icon-angle-right small"></i> ${ it.label }</span>
            </li>
        <% } %>
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