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
    <p>Hospital Utilization</p>
    <ul>
        <li>
            <span class="data">${ activeVisits.value }</span>
            <span class="label">Open Visits (Now)</span>
        </li>
        <li>
            <span class="data">${ todayRegistrations.value }</span>
            <span class="label">Registration(s) Today</span>
        </li>
        <li>
            <span class="data">${ startedVisitOnDay.value }</span>
            <span class="label">Visits Today</span>
            
        </li>
        <li>
            <span class="data">${ startedVisitDayBefore }</span>
            <span class="label">Visits Yesterday</span>
        </li>
    </ul>
</div>

<div class="reportBox">
    <p>Outpatient Clinic data collection</p>
    <ul>
        <li>
            <span class="data">
                ${ outpatientsDayBefore }
            </span>
            <span class="label">Outpatient(s) seen yesterday</span>
        </li>
        <li>
            <span class="data">
                <span>
                    ${ percentage(outpatientsDayBeforeWithClinical) }%
                </span>
                <span class="number">
                    (${ numerator(outpatientsDayBeforeWithClinical) })
                </span>
            </span>
            <span class="label"><i class="icon-angle-right small"></i> with any clinical encounter</span>
        </li>
        <%
        def indicators = [
            [ label: "with vitals captured", value: outpatientsDayBeforeWithVitals ],
            [ label: "with diagnosis captured", value: outpatientsDayBeforeWithDiagnosis ],
        ]
        indicators.each {
        %>
            <li>
                <span class="data">
                    <span>
                        ${ percentage(it.value) }%
                    </span>
                    <span class="number">
                        (${ numerator(it.value) })
                    </span>
                </span>
                <span class="label"><i class="icon-angle-right small"></i> ${ it.label }</span>
            </li>
        <% } %>
    </ul>
</div>

<div class="reportLinksBox">
    <p>Data Quality Reports</p>
    <ul>
        <li><a href="${ ui.pageLink("mirebalaisreports", "noncodeddiagnoses/report") }">${ ui.message("mirebalaisreports.noncodeddiagnoses.title") }</a></li>
    </ul>
</div>