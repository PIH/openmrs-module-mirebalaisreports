package org.openmrs.module.mirebalaisreports.definitions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
import org.openmrs.module.pihcore.config.Config;
import org.openmrs.module.radiologyapp.RadiologyProperties;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Includes helpful methods for dealing with PIH Metadata (this class exists so that someday we might consider
 * moving BaseReportManager into a shared refapp module)
 */
public abstract class BasePihReportManager extends BaseReportManager {

    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
	protected MirebalaisReportsProperties mirebalaisReportsProperties;

    @Autowired
    protected DispositionService dispositionService;

    @Autowired
    protected EmrApiProperties emrApiProperties;

    @Autowired
    protected RadiologyProperties radiologyProperties;

    @Autowired
    protected Config config;

    public abstract String getUuid();

    public void setMirebalaisReportsProperties(MirebalaisReportsProperties mirebalaisReportsProperties) {
        this.mirebalaisReportsProperties = mirebalaisReportsProperties;
    }

    protected String replace(String sql, String oldValue, OpenmrsObject newValue) {
        if (newValue != null) {  // some replacemnets, like the radiology encounter types, aren't available on all systems, so just ignore in these cases
            String s = sql.replace(":" + oldValue, newValue.getId().toString());
            return s;
        }
        else {
            return sql;
        }
    }
}
