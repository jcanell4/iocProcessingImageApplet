/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ioc.wiki.processingmanager.http;

import ioc.wiki.processingmanager.excepcions.ProcessingRtURLException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniel Criado Casas<dani.criado.casas@gmail.com>
 */
public class HttpCommandSender {

    protected String url;
    protected String cookies;
    protected URLConnection urlConnection;
    private HashMap<String,String> urlParameters;
    
    //PROPERTIES
    private static final String PROPERTY_ACCEPT = "Accept";
    private static final String PROPERTY_LANGUAGE = "Accept-Language";
    private static final String PROPERTY_COOKIE = "Cookie";
    private static final String PROPERTY_CHARSET = "Charset";
    
    //PROPERTIES VALUES
    private static final String accept = "text/html,application/xhtml+xml,application/xml";
    private static final String language = "ca,es, en − US, en, en − GB";
    private static final String charset = "UTF-8";
    
    

    /**
     * Constructor de la classe
     */
    public HttpCommandSender() {
        urlParameters = new HashMap<>();
    }

    /**
     * Afegeix un parametre de la url.
     * @param name nom del parametre.
     * @param value valor del parametre.
     */
    public void setParameter(String name, String value) {
        this.urlParameters.put(name, value);
    }
    
    /**
     * Retorna el parametre demanat de la url.
     * @param name nom del parametre.
     * @return retorna el valor del parametre.
     */
    private String getParameter(String name) {
        return this.urlParameters.get(name);
    }
    
    /**
     * Prepara la comanda a enviar afegint tots els parametres i propietats necessaries.
     */
    protected void prepareCommand() {
        Logger.getLogger("HttpCommandSender").log(Level.FINE, "prepareCommand()");
        URL u;
        try {
            String parameters = "";
            Set<Entry<String, String> > setParameters;
            setParameters = this.urlParameters.entrySet();
            for (Map.Entry<String, String> parameter: setParameters) {
                parameters += "&"+parameter.getKey()+"="+parameter.getValue();
            }
            u = new URL(this.url + parameters);
            urlConnection = (URLConnection) u.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty(PROPERTY_ACCEPT, accept);
            urlConnection.setRequestProperty(PROPERTY_LANGUAGE, language);
            urlConnection.setRequestProperty(PROPERTY_COOKIE, this.cookies);
            urlConnection.setRequestProperty(PROPERTY_CHARSET, charset);
        } catch (IOException ex) {
            throw new ProcessingRtURLException(ex);
        }
    }
    
    /**
     * Llegeix la resposta del servidor per retornal en un string.
     * @return Retorna un string amb la resposta http
     */
    protected String receiveResponse() {
        Logger.getLogger("HttpCommandSender").log(Level.FINE, "String receiveResponse()");
        String response = null;
        try {
            //asegura que el missatge s'envia sencer tant si és get com post
            OutputStream request = urlConnection.getOutputStream();
            request.flush();
            request.close();

            Reader reader = new InputStreamReader(urlConnection.getInputStream());
            response = "";
            char[] cbuf = new char[1024];
            int charsRead = 0;
            while (-1 != (charsRead = reader.read(cbuf))) {
                response += String.valueOf(cbuf, 0, charsRead);
            }

            reader.close();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            throw new ProcessingRtURLException(ex);
        }
        return response;
    }

    /**
     * Envia la commanda ja preparada i rep la resposta del servidor.
     * @return Retorna un string amb la resposta http
     */
    public String sendCommand() {
        this.prepareCommand();
        return this.receiveResponse();
    }

    /**
     * Afegeix la url.
     * @param url string de la url.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Afegeix les cookies.
     * @param cook string amb les cookies necessaries.
     */
    public void setCookies(String cook) {
        this.cookies = cook;
    }
}
