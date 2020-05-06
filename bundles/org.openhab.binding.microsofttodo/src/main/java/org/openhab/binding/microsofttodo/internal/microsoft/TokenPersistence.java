package org.openhab.binding.microsofttodo.internal.microsoft;

import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;

import java.io.*;

public class TokenPersistence implements ITokenCacheAccessAspect {
    String data;
    File tempFile;

    public TokenPersistence() throws IOException {
        File tempFile = new File("/volume1/@appstore/openHAB/conf/tmp", "permission.tmp");
        if(tempFile.exists()){
            StringBuilder contentBuilder = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(tempFile));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                contentBuilder.append(sCurrentLine).append("\n");
            }
            data = contentBuilder.toString();
        }
        this.tempFile = tempFile;
    }

    @Override
    public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
         iTokenCacheAccessContext.tokenCache().deserialize(data);
    }

    @Override
    public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
        data = iTokenCacheAccessContext.tokenCache().serialize();
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(tempFile));
            bw.write(data);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
