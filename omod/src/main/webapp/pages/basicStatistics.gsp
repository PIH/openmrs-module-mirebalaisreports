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
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "home") }" },
        { label: "${ ui.message("mirebalaisreports.basicStatistics.title") }", link: "${ ui.thisUrl() }" }
    ];

    function indicatorColor(pct) {
        var red = Math.round(180 * (1-pct));
        var green = Math.round(180 * pct);
        var blue = 0;
        return "rgb(" + red + "," + green + "," + blue + ")";
    }

    jq(function() {
        jq('.percentage-bar').each(function(index) {
            var n = Number(jq(this).attr('data-numerator'));
            var d = Number(jq(this).attr('data-denominator'));
            if (d && d > 0) {
                jq(this).progressbar({
                    value: n,
                    max: d
                }).find(".ui-progressbar-value").css({ "background-color": indicatorColor(n/d) });
            }
        });
    });
</script>

<h1>
    ${ ui.message("mirebalaisreports.basicStatistics.title") }
</h1>
<p class="small">
    Generated ${ ui.format(new Date()) }
</p>

<div class="reportBox">
    <p>Hospital Usage</p>
    <ul>
        <li>
            <span class="label">Registrations Today</span>
            <span class="data">${ todayRegistrations.value }</span>
        </li>
        <li>
            <span class="label">Visits Today</span>
            <span class="data">${ startedVisitOnDay.value }</span>
        </li>
        <li class="subtle">
            <span class="label">Visits Yesterday</span>
            <span class="data">${ startedVisitDayBefore }</span>
        </li>
        <li>
            <span class="label">Open Visits (Now)</span>
            <span class="data">${ activeVisits.value }</span>
        </li>
    </ul>
</div>

<div class="reportBox">
    <p>Data Quality</p>
    <ul>
        <li>
            <span class="label">Outpatients seen yesterday</span>
            <span class="data">
                ${ outpatientsDayBefore }
            </span>
        </li>
        <%
        def indicators = [
            [ label: "with vitals captured", value: outpatientsDayBeforeWithVitals ],
            [ label: "with diagnosis captured", value: outpatientsDayBeforeWithDiagnosis ],
            [ label: "with both vitals & diagnosis captured", value: outpatientsDayBeforeWithVitalsAndDiagnosis ]
        ]
        indicators.each {
        %>
            <li class="indented">
                <span class="label">${ it.label }</span>
                <span class="data">
                    ${ percentage(it.value) }%
                    (${ numerator(it.value) })
                </span>
                <div class="percentage-bar" data-numerator="${ numerator(it.value) }" data-denominator="${ denominator(it.value) }"></div>
            </li>
        <% } %>
    </ul>
</div>
