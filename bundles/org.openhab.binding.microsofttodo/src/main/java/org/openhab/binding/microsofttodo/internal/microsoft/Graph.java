package org.openhab.binding.microsofttodo.internal.microsoft;

import com.google.gson.Gson;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;

public class Graph {

    private IGraphServiceClient graphClient = null;
    private SimpleAuthProvider authProvider = null;
    private Gson gson;
    private Authentication authentication;

    public Graph() {
        authentication = new Authentication("96075e99-9302-4979-8775-8f29dfed3895");
    }

    private void ensureGraphClient(String accessToken) {
        if (graphClient == null) {
            // Create the auth provider
            authProvider = new SimpleAuthProvider(accessToken);


            // Build a Graph client
            graphClient = GraphServiceClient.builder()
                    .authenticationProvider(authProvider)
                    .buildClient();
        }
    }

    public String getUser() {
        if(gson== null){
            gson = new Gson();
        }
        String[] scopes = {"Tasks.ReadWrite.Shared", "User.Read" };
        String userAccessToken = authentication.getUserAccessToken(scopes);
        ensureGraphClient(userAccessToken);

        // GET /me to get authenticated user
        graphClient.setServiceRoot("https://graph.microsoft.com/beta" );
//        graphClient.users().buildRequest().get();
        TaskCollection jsonObject = graphClient.customRequest("/me/outlook/tasks", TaskCollection.class ).buildRequest().get();
        String s = gson.toJson(jsonObject.value);

        return s;
    }
}
