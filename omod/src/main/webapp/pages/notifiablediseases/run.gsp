<%
    ui.decorateWith("emr", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.home.title") }", link: "${ ui.pageLink("mirebalaisreports", "basicStatistics") }" },
        { label: "${ ui.message("mirebalaisreports.notifiablediseases.title") }", link: "${ ui.thisUrl() }" }
    ];
</script>

<h1>
    ${ ui.message("mirebalaisreports.notifiablediseases.title") }
</h1>

<h3>
    ${ ui.message("mirebalaisreports.notifiablediseases.subtitle") }
</h3>

<form method="post">
    <input type="hidden" name="format" value="excel"/>

    <p>
        <label for="start-of-week">${ ui.message("mirebalaisreports.notifiablediseases.startOfWeek.label") }</label>
        <input id="start-of-week" type="date" name="startOfWeek"/>
    </p>

    <button type="submit">${ ui.message("mirebalaisreports.general.runReport") }</button>
</form>
