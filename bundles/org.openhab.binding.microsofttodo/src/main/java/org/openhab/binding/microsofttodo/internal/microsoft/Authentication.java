package org.openhab.binding.microsofttodo.internal.microsoft;

import com.microsoft.aad.msal4j.DeviceCode;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Authentication
 */
public class Authentication {

    private String applicationId;
    // Set authority to allow only organizational accounts
    // Device code flow only supports organizational accounts
    private final String authority = "https://login.microsoftonline.com/common/";
    public PublicClientApplication app;
    public ExecutorService pool = Executors.newFixedThreadPool(1);
    private final Logger logger = LoggerFactory.getLogger(Authentication.class);

    public Authentication(String applicationId) {
        this.applicationId = applicationId;
        try {
            app = PublicClientApplication.builder(applicationId)
                    .authority(authority)
                    .executorService(pool)
                    .setTokenCacheAccessAspect(new TokenPersistence())
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserAccessToken(String[] scopes) {
        if (applicationId == null) {
            logger.info("You must initialize Authentication before calling getUserAccessToken");
            return null;
        }

        Set<String> scopeSet = Set.of(scopes);
        Set<IAccount> accountsInCache = app.getAccounts().join();
        IAuthenticationResult result;
        if(accountsInCache.size()>0) {
            IAccount account = accountsInCache.iterator().next();
            try {
                SilentParameters silentParameters =
                        SilentParameters
                                .builder(scopeSet, account)
                                .build();

                // try to acquire token silently. This call will fail since the token cache
                // does not have any data for the user you are trying to acquire a token for
                result = app.acquireTokenSilently(silentParameters).join();
            } catch (Exception ex) {
                // Create consumer to receive the DeviceCode object
                // This method gets executed during the flow and provides
                // the URL the user logs into and the device code to enter
                result = interactiveToken(scopeSet);
            }

        }
        else
            result = interactiveToken(scopeSet);

        if (result != null) {
            return result.accessToken();
        }
        return null;
    }

    private IAuthenticationResult interactiveToken(Set<String> scopeSet) {
        IAuthenticationResult result;
        Consumer<DeviceCode> deviceCodeConsumer = (DeviceCode deviceCode) -> {
            // Print the login information to the console
            logger.info(deviceCode.message());
        };

        // Request a token, passing the requested permission scopes
        result = app.acquireToken(
                DeviceCodeFlowParameters
                        .builder(scopeSet, deviceCodeConsumer)
                        .build()
        ).exceptionally(ex2 -> {
            logger.info("Unable to authenticate - " + ex2.getMessage());
            return null;
        }).join();
        return result;
    }
}
