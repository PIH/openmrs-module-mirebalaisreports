<%
    ui.decorateWith("appui", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "home") }" }
    ];
</script>

<h1>
    ${ ui.message("mirebalaisreports.home.title") }
</h1>

<ul>
    <li><a href="${ ui.pageLink("mirebalaisreports", "basicStatistics") }">${ ui.message("mirebalaisreports.basicStatistics.title") }</a></li>
    <li><a href="${ ui.pageLink("mirebalaisreports", "noncodeddiagnoses/report") }">${ ui.message("mirebalaisreports.noncodeddiagnoses.title") }</a></li>
</ul>