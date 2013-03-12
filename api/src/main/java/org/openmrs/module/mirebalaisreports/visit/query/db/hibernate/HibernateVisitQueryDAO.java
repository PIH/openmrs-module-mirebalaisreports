package org.openmrs.module.mirebalaisreports.visit.query.db.hibernate;


import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.mirebalaisreports.visit.query.db.VisitQueryDAO;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HibernateVisitQueryDAO implements VisitQueryDAO{

    private SessionFactory sessionFactory;


    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public Map<Integer, Date> getMapOfVisitIdsAndDates(Concept concept, Date startDate, Date endDate) {

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class)
                            .createAlias("visit", "v")
                            .add(Restrictions.eq("voided", false))
                            .add(Restrictions.eq("concept", concept))
                            .add(Restrictions.between("v.date_started", startDate, endDate))
                            .setProjection(Projections.projectionList().add(Projections.property("encounter.visit.id")).add(Projections.property("encounter.visit.startDatetime")));


        List<Object[]> rows= criteria.list();

        Map<Integer, Date> visitIdsWithDates = new HashMap<Integer, Date>();

        for (Object[] row : rows) {
            visitIdsWithDates.put((Integer) row[0], (Date) row[1]);
        }

        return visitIdsWithDates;
    }


}
