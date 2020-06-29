package org.odk.collect.bdrs.instrumented.database;

import org.junit.Test;
import org.odk.collect.bdrs.dao.FormsDao;
import org.odk.collect.bdrs.dao.InstancesDao;
import org.odk.collect.bdrs.database.helpers.FormsDatabaseHelper;
import org.odk.collect.bdrs.database.helpers.InstancesDatabaseHelper;
import org.odk.collect.bdrs.instrumented.database.helpers.SqlLiteHelperTest;

import java.io.File;
import java.io.IOException;

import static org.odk.collect.bdrs.support.FileUtils.copyFileFromAssets;

public class ChangingDatabasesByForksTest extends SqlLiteHelperTest {

    @Test
    public void appShouldNotCrashAfterChangingFormsDbByItsFork() throws IOException {
        // getting forms cursor creates the newest version of form.db
        new FormsDao().getFormsCursor();

        // a fork changes our forms.db downgrading it
        copyFileFromAssets("database" + File.separator + "forms_v4.db", FormsDatabaseHelper.getDatabasePath());

        // the change should be detected and handled, app shouldn't crash
        new FormsDao().getFormsCursor();
    }

    @Test
    public void appShouldNotCrashAfterChangingInstancesDbByItsFork() throws IOException {
        // getting instances cursor creates the newest version of instances.db
        new InstancesDao().getSentInstancesCursor();

        // a fork changes our instances.db downgrading it
        copyFileFromAssets("database" + File.separator + "instances_v3.db", InstancesDatabaseHelper.getDatabasePath());

        // the change should be detected and handled, app shouldn't crash
        new InstancesDao().getSentInstancesCursor();
    }
}
