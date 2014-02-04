/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.mirebalaisreports.test;

import ch.vorburger.mariadb4j.DB;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Properties;

public class BaseRealDatabaseTest extends BaseModuleContextSensitiveTest {

    public static final int PORT = 3308;

    private static DB db;

    @BeforeClass
    public static void setupDatabase() throws Exception {
        db = DB.newEmbeddedDB(PORT);
        db.start();
        db.source("org/openmrs/module/mirebalaisreports/database/base-database.sql");
    }

    @Before
    public void signInAsAdminUser() throws Exception {
        super.authenticate();
    }

    @AfterClass
    public static void teardownDatabase() throws Exception {
        db.stop();
    }

    @Override
    public Boolean useInMemoryDatabase() {
        return false;
    }

    @Override
    public Properties getRuntimeProperties() {
        Properties p = super.getRuntimeProperties();
        p.setProperty("junit.username", "admin");
        p.setProperty("junit.password", "Admin123");
        p.setProperty("connection.username", "root");
        p.setProperty("connection.password", "");
        p.setProperty("connection.url", "jdbc:mysql://localhost:" + PORT + "/test?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8");
        p.setProperty("hibernate.show_sql", "false");
        return p;
    }
}
