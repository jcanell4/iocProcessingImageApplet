/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ioc.wiki.processingmanager.http;

import ioc.wiki.processingmanager.excepcions.ProcessingRtURLException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Daniel Criado Casas<dani.criado.casas@gmail.com>
 */
public class HttpFileSender extends HttpCommandSender {

    private String destFilename;
    private byte[] image;

    //PROPERTIES
    private static final String PROPERTY_CONTENT_TYPE = "Content-Type";
    private static final String REQUEST_METHOD = "POST";

    //VALUES
    private static final String boundary = "******";
    private static final String twoHyphens = "--";
    private static final String quote = "\"";
    private static final String crlf = "\r\n";
    private static final String accept = "text/html,application/xhtml+xml,application/xml";
    private static final String language = "ca,es, en − US, en, en − GB";
    private static final String content_type = "multipart/form-data;boundary=";

    //FILE
    private static final String contentDispositionFile = "Content-Disposition: form-data; name=file; filename=";
    private static final String contentTypeFile = "Content-type: image/png";
    private static final String contentTransferEncodingFile = "Content-Transfer-Enconding: base64";

    /**
     * Constructor de la classe
     */
    public HttpFileSender() {

    }

    /**
     * Afegeix la imatge i el seu nom.
     * @param destFilename nom de la imatge amb extensio
     * @param image bytes de la imatge
     */
    public void setImageToSend(String destFilename, byte[] image) {
        Logger.getLogger("HttpFileSender").log(Level.FINE, "setImageToSend(" + destFilename + ", byte[] image)");
        this.destFilename = destFilename;
        this.image = image;
    }

    /**
     * Prepara la comanda a enviar al servidor, l'envia i rep la resposta en un string.
     * Conté informació sobre el éxit de guardar el fitxer al servidor.
     * @return Retorna un string amb la resposta del servidor. 
     */
    @Override
    public String sendCommand() {
        super.prepareCommand();
        try {

            ((HttpURLConnection) urlConnection).setRequestMethod(REQUEST_METHOD);
            urlConnection.setRequestProperty(PROPERTY_CONTENT_TYPE, content_type + boundary);
            DataOutputStream request = new DataOutputStream(urlConnection.getOutputStream());
            //Capçalera fitxer
            
            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes(contentDispositionFile + quote + this.destFilename + quote + crlf);
            request.writeBytes(contentTypeFile + crlf);
            request.writeBytes(contentTransferEncodingFile + crlf);
            request.writeBytes(crlf);

            //IMAGE
            String base64Image = Base64.encodeBase64String(this.image);
            request.writeBytes(base64Image);

            //Tancament fitxer
            request.writeBytes(crlf);
            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

            request.flush();
            request.close();
            
        } catch (IOException ex) {
            throw new ProcessingRtURLException(ex);
        }

        return super.receiveResponse();
    }

}
