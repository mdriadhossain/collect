package org.odk.collect.bdrs.injection.config;

import android.app.Application;
import android.telephony.SmsManager;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.bdrs.activities.FormDownloadListActivity;
import org.odk.collect.bdrs.activities.FormEntryActivity;
import org.odk.collect.bdrs.activities.FormHierarchyActivity;
import org.odk.collect.bdrs.activities.FormMapActivity;
import org.odk.collect.bdrs.activities.GeoPointMapActivity;
import org.odk.collect.bdrs.activities.GeoPolyActivity;
import org.odk.collect.bdrs.activities.GoogleDriveActivity;
import org.odk.collect.bdrs.activities.GoogleSheetsUploaderActivity;
import org.odk.collect.bdrs.activities.InstanceUploaderListActivity;
import org.odk.collect.bdrs.activities.MainMenuActivity;
import org.odk.collect.bdrs.activities.SplashScreenActivity;
import org.odk.collect.bdrs.adapters.InstanceUploaderAdapter;
import org.odk.collect.bdrs.analytics.Analytics;
import org.odk.collect.bdrs.application.Collect;
import org.odk.collect.bdrs.events.RxEventBus;
import org.odk.collect.bdrs.formentry.ODKView;
import org.odk.collect.bdrs.formentry.saving.SaveFormProgressDialogFragment;
import org.odk.collect.bdrs.fragments.DataManagerList;
import org.odk.collect.bdrs.geo.GoogleMapFragment;
import org.odk.collect.bdrs.geo.MapboxMapFragment;
import org.odk.collect.bdrs.geo.OsmDroidMapFragment;
import org.odk.collect.bdrs.fragments.ShowQRCodeFragment;
import org.odk.collect.bdrs.logic.PropertyManager;
import org.odk.collect.bdrs.openrosa.OpenRosaHttpInterface;
import org.odk.collect.bdrs.preferences.AdminPasswordDialogFragment;
import org.odk.collect.bdrs.preferences.AdminSharedPreferences;
import org.odk.collect.bdrs.preferences.FormManagementPreferences;
import org.odk.collect.bdrs.preferences.FormMetadataFragment;
import org.odk.collect.bdrs.preferences.GeneralSharedPreferences;
import org.odk.collect.bdrs.preferences.IdentityPreferences;
import org.odk.collect.bdrs.preferences.ServerPreferencesFragment;
import org.odk.collect.bdrs.storage.migration.StorageMigrationDialog;
import org.odk.collect.bdrs.storage.migration.StorageMigrationService;
import org.odk.collect.bdrs.tasks.InstanceServerUploaderTask;
import org.odk.collect.bdrs.tasks.ServerPollingJob;
import org.odk.collect.bdrs.tasks.sms.SmsNotificationReceiver;
import org.odk.collect.bdrs.tasks.sms.SmsSender;
import org.odk.collect.bdrs.tasks.sms.SmsSentBroadcastReceiver;
import org.odk.collect.bdrs.tasks.sms.SmsService;
import org.odk.collect.bdrs.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.bdrs.upload.AutoSendWorker;
import org.odk.collect.bdrs.utilities.AuthDialogUtility;
import org.odk.collect.bdrs.utilities.FormDownloader;
import org.odk.collect.bdrs.widgets.ExStringWidget;
import org.odk.collect.bdrs.widgets.QuestionWidget;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

/**
 * Dagger component for the application. Should include
 * application level Dagger Modules and be built with Application
 * object.
 *
 * Add an `inject(MyClass myClass)` method here for objects you want
 * to inject into so Dagger knows to wire it up.
 *
 * Annotated with @Singleton so modules can include @Singletons that will
 * be retained at an application level (as this an instance of this components
 * is owned by the Application object).
 *
 * If you need to call a provider directly from the component (in a test
 * for example) you can add a method with the type you are looking to fetch
 * (`MyType myType()`) to this interface.
 *
 * To read more about Dagger visit: https://google.github.io/dagger/users-guide
 **/

@Singleton
@Component(modules = {
        AppDependencyModule.class
})
public interface AppDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        Builder appDependencyModule(AppDependencyModule testDependencyModule);

        AppDependencyComponent build();
    }

    void inject(Collect collect);

    void inject(SmsService smsService);

    void inject(SmsSender smsSender);

    void inject(SmsSentBroadcastReceiver smsSentBroadcastReceiver);

    void inject(SmsNotificationReceiver smsNotificationReceiver);

    void inject(InstanceUploaderAdapter instanceUploaderAdapter);

    void inject(DataManagerList dataManagerList);

    void inject(PropertyManager propertyManager);

    void inject(FormEntryActivity formEntryActivity);

    void inject(InstanceServerUploaderTask uploader);

    void inject(ServerPreferencesFragment serverPreferencesFragment);

    void inject(FormDownloader formDownloader);

    void inject(ServerPollingJob serverPollingJob);

    void inject(AuthDialogUtility authDialogUtility);

    void inject(FormDownloadListActivity formDownloadListActivity);

    void inject(InstanceUploaderListActivity activity);

    void inject(GoogleDriveActivity googleDriveActivity);

    void inject(GoogleSheetsUploaderActivity googleSheetsUploaderActivity);

    void inject(QuestionWidget questionWidget);

    void inject(ExStringWidget exStringWidget);

    void inject(ODKView odkView);

    void inject(FormMetadataFragment formMetadataFragment);

    void inject(GeoPointMapActivity geoMapActivity);

    void inject(GeoPolyActivity geoPolyActivity);

    void inject(FormMapActivity formMapActivity);

    void inject(OsmDroidMapFragment mapFragment);

    void inject(GoogleMapFragment mapFragment);

    void inject(MapboxMapFragment mapFragment);

    void inject(MainMenuActivity mainMenuActivity);

    void inject(ShowQRCodeFragment showQRCodeFragment);

    void inject(StorageMigrationService storageMigrationService);

    void inject(AutoSendWorker autoSendWorker);

    void inject(StorageMigrationDialog storageMigrationDialog);

    void inject(AdminPasswordDialogFragment adminPasswordDialogFragment);

    void inject(SplashScreenActivity splashScreenActivity);

    void inject(FormHierarchyActivity formHierarchyActivity);

    void inject(FormManagementPreferences formManagementPreferences);

    void inject(IdentityPreferences identityPreferences);

    void inject(SaveFormProgressDialogFragment saveFormProgressDialogFragment);

    SmsManager smsManager();

    SmsSubmissionManagerContract smsSubmissionManagerContract();

    RxEventBus rxEventBus();

    OpenRosaHttpInterface openRosaHttpInterface();

    ReferenceManager referenceManager();

    Analytics analytics();

    GeneralSharedPreferences generalSharedPreferences();

    AdminSharedPreferences adminSharedPreferences();
}
