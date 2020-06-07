package org.wso2.carbon.identity.sample.prometheus.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.sample.prometheus.event.handler.AuthenticationEventHandler;
import org.wso2.carbon.identity.sample.prometheus.servlet.IdentityMetricsServlet;

import javax.servlet.Servlet;

@Component(
        name = "identity.sample.prometheus.agent",
        immediate = true
)
public class ServiceComponent {

    private static final Log LOG = LogFactory.getLog(ServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();

            //Initialize and register the event listener to capture login stats
            AuthenticationEventHandler eventHandler = new AuthenticationEventHandler();
            eventHandler.initCounter();
            bundleContext.registerService(AbstractEventHandler.class,
                    eventHandler, null);

            // Register the metrics endpoint as an OSGi Servlet
            HttpService httpService = DataHolder.getInstance().getHttpService();
            Servlet identityMetricsServlet = new ContextPathServletAdaptor(new IdentityMetricsServlet(),
                    IdentityMetricsServlet.CONTEXT);
            httpService.registerServlet(IdentityMetricsServlet.CONTEXT, identityMetricsServlet, null, null);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Sample prometheus agent bundle is activated");
            }
        } catch (Exception e) {
            LOG.error("Error while activating sample prometheus agent bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sample prometheus agent bundle is deactivated");
        }
    }

    @Reference(
            name = "osgi.httpservice",
            service = org.osgi.service.http.HttpService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHttpService")
    protected void setHttpService(HttpService httpService) {

        DataHolder.getInstance().setHttpService(httpService);
    }

    protected void unsetHttpService(HttpService httpService) {

        DataHolder.getInstance().setHttpService(null);
    }
}
