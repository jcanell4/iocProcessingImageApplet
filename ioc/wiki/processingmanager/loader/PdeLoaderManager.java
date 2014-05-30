package ioc.wiki.processingmanager.loader;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import ioc.wiki.processingmanager.ImageGenerator;
import ioc.wiki.processingmanager.excepcions.ProcessingLoaderException;
import ioc.wiki.processingmanager.excepcions.ProcessingRtURLException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;

/**
 *
 * @author Daniel Criado Casas<dani.criado.casas@gmail.com>
 */
public class PdeLoaderManager {
    /**
     * Variable que te les classes que ja s'han carregat.
     */
    private HashMap<String, Class<? extends ImageGenerator>> classesCarregades;
    private ArrayList<URL> urls;

    /**
     * Constructor de la classe.
     */
    public PdeLoaderManager() {
        this.classesCarregades = new HashMap<>();
        this.urls = new ArrayList<>();
    }

    /**
     * Agafa la classe del HashMap.
     *
     * @param className Nom de la classe.
     * @return Classe carregada,filla de ImageGenerator.
     */
    private Class<? extends ImageGenerator> getPdeClass(String className) throws ProcessingLoaderException {
        Logger.getLogger("PdeLoaderManager").log(Level.FINE, "getPdeClass("+className+")");
        Class<? extends ImageGenerator> cl = null;
        if (this.classesCarregades.containsKey(className)) {
            cl = this.classesCarregades.get(className);
        } else {
            cl = loadPdeClass(className);
            this.classesCarregades.put(className, cl);
        }
        return cl;
    }

    /**
     * Carrega la classe que esta al servidor.
     *
     * @param className Nom de la classe a carregar.
     * @return Classe carregada,filla de ImageGenerator.
     * @throws ProcessingLoaderException es llança quan no es pot carregar la classe.
     */
    private Class<? extends ImageGenerator> loadPdeClass(String className) throws ProcessingLoaderException{
        Logger.getLogger("PdeLoaderManager").log(Level.FINE, "loadPdeClass("+className+")");
        
        Class<? extends ImageGenerator> cl = null;
        URL[] array_urls = new URL[this.urls.size()];
        this.urls.toArray(array_urls);
        URLClassLoader urlClassLoader = new URLClassLoader(array_urls);
        try {
            cl = (Class<? extends ImageGenerator>) urlClassLoader.loadClass(className);
            //urlClassLoader.close();
        } catch (ClassNotFoundException /*| IOException*/ ex) {
            throw new ProcessingLoaderException(ex);
        }
        return cl;
    }

    /**
     * Afegeix una url a l'objecte.
     * @param url per afegir
     * @throws ProcessingRtURLException es llança quan la URL no està ben formada.
     */
    public void addSourceUrl(String url) {
        URL u = null;
        try {
            u = new URL(url);
            this.urls.add(u);
        } catch (MalformedURLException ex) {
            throw new ProcessingRtURLException(ex);
        }
        
    }

    /**
     * Aconsegueix una instancia de la classe demanada.
     * @param className Nom de la classe a instanciar.
     * @return una instancia de ImageGenerator
     * @throws ProcessingLoaderException es llança quan falla la instanciacio o hi ha un accés il·legal de la classe.
     */
    public ImageGenerator getNewInstance(String className) throws ProcessingLoaderException {
        ImageGenerator pdeClass = null;
        try {
            pdeClass = (ImageGenerator) getPdeClass(className).newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {//| PApplet.RendererChangeException
            throw new ProcessingLoaderException(ex);
        }
        return pdeClass;
    }
}
