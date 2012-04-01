package com.taskadapter.connector.jira;

import com.atlassian.jira_soapclient.SOAPSession;
import com.taskadapter.connector.definition.WebServerInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class JiraConnectionFactory {
    public static JiraConnection createConnection(WebServerInfo info) throws MalformedURLException, RemoteException {
        String baseUrl = info.getHost() + "/rpc/soap/jirasoapservice-v2";
        SOAPSession soapSession = new SOAPSession(new URL(baseUrl));
        soapSession.connect(info.getUserName(), info.getPassword());
        return new JiraConnection(soapSession.getJiraSoapService(), soapSession.getAuthenticationToken());
    }
}
