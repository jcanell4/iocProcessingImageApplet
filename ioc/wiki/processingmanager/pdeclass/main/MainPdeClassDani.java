/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ioc.wiki.processingmanager.pdeclass.main;

import processing.core.PApplet;
import static processing.core.PApplet.concat;

/**
 *
 * @author Daniel Criado Casas <dani.criado.casas@gmail.com>
 */
public class MainPdeClassDani {

    /**
     * @param args the command line arguments
     */
    static public void main(String[] passedArgs) {
      String[] appletArgs = new String[] { "ioc.wiki.processingmanager.PdeClassDani1" };
      if (passedArgs != null) {
        PApplet.main(concat(appletArgs, passedArgs));
      } else {
        PApplet.main(appletArgs);
      }
    }  
    
}
