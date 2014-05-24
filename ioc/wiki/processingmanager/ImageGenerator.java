/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ioc.wiki.processingmanager;

import processing.core.PApplet;

/**
 *
 * @author Daniel Criado Casas<dani.criado.casas@gmail.com>
 */
public class ImageGenerator extends PApplet {
    
    /**
     * És la variable llavor
     */
    private String seed;
     
    /**
     * Constructor de la classe
     */
    public ImageGenerator(){
        this.seed = null;
    }
    
    /**
     * Especifica quina es la llavor en el metode randomSeed() de PApplet.
     * @param seed Llavor que es pasa en forma de string.
     */
    public void setSeed(String seed) {
        this.seed = seed;
        super.randomSeed(seed.hashCode());
    }
    
    /**
     * Funcio que et dona la llavor.
     *
     * @return Retorna un string amb la llavor.
     */
    public String getSeed() {
        return this.seed;
    }
    
}
