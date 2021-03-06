
package org.clinigment.rest.api.controller.filter;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.clinigment.rest.api.controller.LoginController;
import org.clinigment.rest.api.controller.exceptions.LoginException;
import org.clinigment.rest.api.model.Employee;
import org.clinigment.rest.api.model.LoginForm;
import org.clinigment.rest.api.model.UnauthorizedEntity;
import org.glassfish.jersey.internal.util.Base64;

/**
 *
 * @author csaba
 */
@Provider
public class SecurityFilter implements ContainerRequestFilter {
    
    private EntityManagerFactory getEntityManagerFactory() throws NamingException {
        return (EntityManagerFactory) new InitialContext().lookup("java:comp/env/persistence-factory");
    }
    
    private LoginController getController() {
        try {
            return new LoginController(getEntityManagerFactory());
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    //Static final Strings for header auth values, and login url
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_PREFIX = "Basic ";
    private static final String LOGIN_URL_PREFIX =  "login";
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        //Apart from login resource, all other resources will require basic authentication
        if(!requestContext.getUriInfo().getPath().contains(LOGIN_URL_PREFIX)) {
            //Get the encoded auth string from header
            List<String> authHeader = requestContext.getHeaders().get(AUTHORIZATION_HEADER);

            if(authHeader != null && authHeader.size() > 0) {
                
                String authToken = authHeader.get(0);
                //Replace "Basic " with ""
                authToken = authToken.replaceFirst(AUTHORIZATION_HEADER_PREFIX, "");
                //Decode username + password
                String decodedString = Base64.decodeAsString(authToken);

                //Tokenize username and password out of the string
                StringTokenizer tokenizer = new StringTokenizer(decodedString, ":");

                String username = tokenizer.nextToken();
                String password = "";
                try {
                    System.out.println("I'm here");
                    password = tokenizer.nextToken();
                } catch (NoSuchElementException nee) {
                    UnauthorizedEntity unauthorizedEntity = new UnauthorizedEntity();

                    //Create unauthorized response
                    Response unauthorizedStatus = Response
                                                    .status(Response.Status.UNAUTHORIZED)
                                                    .build();
                    //Break the request with "abortWith" method
                    requestContext.abortWith(unauthorizedStatus);
                }
                
                LoginForm form = new LoginForm();
                form.setUsername(username);
                form.setPassword(password);
                
                try {
                    Employee emp = getController().login(form);
                    System.out.println("Employee " + emp);
                    if(emp == null) {
                        throw new LoginException();
                    }
                } catch (LoginException ex) {
                    UnauthorizedEntity unauthorizedEntity = new UnauthorizedEntity();

                    //Create unauthorized response
                    Response unauthorizedStatus = Response
                                                    .status(Response.Status.UNAUTHORIZED)
                                                    .build();
                    //Break the request with "abortWith" method
                    requestContext.abortWith(unauthorizedStatus);
                }
            } else {
                UnauthorizedEntity unauthorizedEntity = new UnauthorizedEntity();

                    //Create unauthorized response
                    Response unauthorizedStatus = Response
                                                    .status(Response.Status.UNAUTHORIZED)
                                                    .build();
                    //Break the request with "abortWith" method
                    requestContext.abortWith(unauthorizedStatus);
            }
        }
        
        
    } 
}
