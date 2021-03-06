package org.wso2.carbon.identity.sample.prometheus.event.handler;

import io.prometheus.client.Counter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;

import java.util.Map;

public class AuthenticationEventHandler extends AbstractEventHandler {

    private static final Log LOG = LogFactory.getLog(AuthenticationEventHandler.class);
    public static final String HANDLER_NAME = "prometheus-auth-handler";
    public static final String VALUE_NOT_AVAILABLE = "Not Available";

    private Counter loginSuccessCounter;
    private Counter loginFailureCounter;

    public void initCounter() {

        loginSuccessCounter = Counter.build()
                .name("login_success_total")
                .help("Successful logins from WSO2 Identity Server")
                .labelNames("username", "tenant", "application")
                .register();
        loginFailureCounter = Counter.build()
                .name("login_failure_total")
                .help("Failed logins from WSO2 Identity Server")
                .labelNames("username", "tenant", "application")
                .register();
    }

    @Override
    public String getName() {

        return HANDLER_NAME;
    }

    public void handleEvent(Event event) {

        if (IdentityEventConstants.EventName.AUTHENTICATION_STEP_FAILURE.name().equals(event.getEventName()) ||
                IdentityEventConstants.EventName.AUTHENTICATION_FAILURE.name().equals(event.getEventName())) {
            countAuthEvent(event, loginFailureCounter);
            return;
        }

        if (IdentityEventConstants.EventName.AUTHENTICATION_SUCCESS.name().equals(event.getEventName())) {
            countAuthEvent(event, loginSuccessCounter);
            return;
        }
        LOG.warn("Event " + event.getEventName() + " can't be handled by " + HANDLER_NAME + ". Hence ignoring..");
    }

    private void countAuthEvent(Event event, Counter counter) {

        Map<String, Object> properties = event.getEventProperties();
        AuthenticationContext context = (AuthenticationContext) properties.get(IdentityEventConstants.EventProperty.
                CONTEXT);
        Map<String, Object> params = (Map<String, Object>) properties.get(IdentityEventConstants.EventProperty.PARAMS);
        Object userObj = params.get(FrameworkConstants.AnalyticsAttributes.USER);
        String username = getUsername(userObj);
        String tenant = getTenant(userObj);
        String serviceProvider = context.getServiceProviderName();
        counter.labels(username, tenant, serviceProvider).inc();
    }

    private String getUsername(Object userObj) {

        String username = null;
        if (userObj instanceof User) {
            username = ((User) userObj).getUserName();
        }
        if (username == null && userObj instanceof AuthenticatedUser) {
            username = ((AuthenticatedUser) userObj).getAuthenticatedSubjectIdentifier();
        }
        return username != null ? username : VALUE_NOT_AVAILABLE;
    }

    private String getTenant(Object userObj) {

        String tenant = null;
        if (userObj instanceof User) {
            tenant = ((User) userObj).getTenantDomain();
        }
        return tenant != null ? tenant : VALUE_NOT_AVAILABLE;
    }
}
