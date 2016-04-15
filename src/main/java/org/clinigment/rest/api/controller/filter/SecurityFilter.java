
package org.clinigment.rest.api.controller.filter;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.clinigment.rest.api.model.UnauthorizedEntity;
import org.glassfish.jersey.internal.util.Base64;

/**
 *
 * @author csaba
 */
@Provider
public class SecurityFilter implements ContainerRequestFilter {
    
    //Static final Strings for header auth values
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_PREFIX = "Basic ";
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        //Get the encoded auth string from header
        List<String> authHeader = requestContext.getHeaders().get(AUTHORIZATION_HEADER);
        
        if(authHeader != null && authHeader.size() > 0) {
            String authToken = authHeader.get(0);
            //Replace "Basic " with ""
            authToken = authToken.replaceFirst(AUTHORIZATION_HEADER_PREFIX, "");
            //Decod username + password
            String decodedString = Base64.decodeAsString(authToken);
            
            //Tokenize username and password out of the string
            StringTokenizer tokenizer = new StringTokenizer(decodedString, ":");
            
            String username = tokenizer.nextToken();
            String password = tokenizer.nextToken();
            
            if(username.equals("admin") && password.equals("admin")) {
                return;
            }
        }
        
        UnauthorizedEntity unauthorizedEntity = new UnauthorizedEntity();
        
        //Create unauthorized response
        Response unauthorizedStatus = Response
                                        .status(Response.Status.UNAUTHORIZED)
                                        .entity(unauthorizedEntity)
                                        .build();
        //Break the request with "abortWith" method
        requestContext.abortWith(unauthorizedStatus);
    }
    
}