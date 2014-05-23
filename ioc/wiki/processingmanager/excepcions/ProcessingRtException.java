/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ioc.wiki.processingmanager.excepcions;

/**
 *
 * @author Daniel Criado Casas<dani.criado.casas@gmail.com>
 */
public class ProcessingRtException extends RuntimeException{

    public ProcessingRtException(Exception ex) {
        super(ex);
    }
    
    
}
