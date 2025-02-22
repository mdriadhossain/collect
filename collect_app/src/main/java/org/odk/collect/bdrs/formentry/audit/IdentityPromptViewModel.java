package org.odk.collect.bdrs.formentry.audit;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.odk.collect.bdrs.formentry.RequiresFormController;
import org.odk.collect.bdrs.javarosawrapper.FormController;

import static org.odk.collect.bdrs.utilities.StringUtils.isBlank;

public class IdentityPromptViewModel extends ViewModel implements RequiresFormController {

    private final MutableLiveData<Boolean> formEntryCancelled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> requiresIdentity = new MutableLiveData<>(false);

    @Nullable
    private AuditEventLogger auditEventLogger;

    private String identity = "";
    private String formName;

    public IdentityPromptViewModel() {
        updateRequiresIdentity();
    }

    @Override
    public void formLoaded(FormController formController) {
        this.formName = formController.getFormTitle();
        this.auditEventLogger = formController.getAuditEventLogger();
        updateRequiresIdentity();
    }

    public LiveData<Boolean> requiresIdentityToContinue() {
        return requiresIdentity;
    }

    public LiveData<Boolean> isFormEntryCancelled() {
        return formEntryCancelled;
    }

    public String getUser() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void done() {
        if (auditEventLogger != null) {
            auditEventLogger.setUser(identity);
        }
        
        updateRequiresIdentity();
    }

    public void promptDismissed() {
        formEntryCancelled.setValue(true);
    }

    private void updateRequiresIdentity() {
        this.requiresIdentity.setValue(
                auditEventLogger != null && auditEventLogger.isUserRequired() && !userIsValid(auditEventLogger.getUser())
        );
    }

    private static boolean userIsValid(String user) {
        return user != null && !user.isEmpty() && !isBlank(user);
    }

    public String getFormTitle() {
        return formName;
    }
}
