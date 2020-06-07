package org.wso2.carbon.identity.sample.prometheus.internal;

import org.osgi.service.http.HttpService;

public class DataHolder {

    private static DataHolder instance = new DataHolder();

    private HttpService httpService;

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        return instance;
    }

    public HttpService getHttpService() {
        return httpService;
    }

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }
}
