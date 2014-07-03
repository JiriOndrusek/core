package org.jboss.as.console.client.shared.state;

import org.jboss.dmr.client.ModelNode;
import org.jboss.dmr.client.Property;

import java.util.List;
import java.util.Map;

/**
 * @author Heiko Braun
 * @date 1/17/12
 */
public class DomainResponseProcessor implements ResponseProcessor {

    private static final String RESPONSE_HEADERS = "response-headers";
    private static final String PROCESS_STATE = "process-state";

    private static final String RESTART_REQUIRED = "restart-required";
    private static final String RELOAD_REQUIRED = "reload-required";
    private static final String SERVER_GROUPS = "server-groups";
    private static final String RESPONSE = "response";

    @Override
    public boolean accepts(ModelNode response) {
        return response.hasDefined(SERVER_GROUPS);
    }

    @Override
    public void process(ModelNode response, Map<String, ServerState> serverStates) {

        parseServerState(response, serverStates);

    }

    private static boolean parseServerState(ModelNode response, Map<String, ServerState> serverStates) {
        boolean staleModel = false;

        if(response.hasDefined(SERVER_GROUPS))
        {
            List<Property> serverGroups = response.get(SERVER_GROUPS).asPropertyList();
            for(Property serverGroup : serverGroups)
            {
                ModelNode serverGroupValue = serverGroup.getValue();

                //  -- Server Group --
                if(serverGroupValue.hasDefined("host"))
                {
                    List<Property> hosts = serverGroupValue.get("host").asPropertyList();
                    for(Property host : hosts)
                    {
                        // -- Host  --

                        ModelNode hostValue = host.getValue();
                        parseHost(host.getName(), hostValue, serverStates);
                    }
                }
            }
        }

        return staleModel;
    }

    private static void parseHost(final String hostName, ModelNode hostValue, Map<String, ServerState> serverStates) {


        List<Property> servers = hostValue.asPropertyList();

        for(Property server : servers)
        {

            // -- Server  --

            ModelNode serverResponse = server.getValue().get(RESPONSE);

            if(serverResponse.hasDefined(RESPONSE_HEADERS))
            {
                List<Property> headers = serverResponse.get(RESPONSE_HEADERS).asPropertyList();
                for(Property header : headers)
                {
                    if(PROCESS_STATE.equals(header.getName()))
                    {
                        String headerValue = header.getValue().asString();
                        String name = "Host: "+hostName+", server: "+server.getName();

                        if(RESTART_REQUIRED.equals(headerValue))
                        {
                            ServerState state = new ServerState(name);
                            state.setRestartRequired(true);
                            serverStates.put(name, state);

                        }
                        else if(RELOAD_REQUIRED.equals(headerValue))
                        {
                            ServerState state = new ServerState(name);
                            state.setReloadRequired(true);
                            serverStates.put(name, state);
                        }
                    }
                }
            }
        }
    }
}
