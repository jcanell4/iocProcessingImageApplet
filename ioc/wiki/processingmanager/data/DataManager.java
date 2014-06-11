/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ioc.wiki.processingmanager.data;

import java.util.HashMap;

/**
 *
 * @author Daniel Criado Casas<dani.criado.casas@gmail.com>
 */
public class DataManager {
    private static final HashMap<String,String> DATA = insertData();
    public static final String ERROR_INESPERAT = "ERROR_INESPERAT";
    public static final String ERROR_DESAR_IMATGE = "ERROR_DESAR_IMATGE";
    public static final String ERROR_GENERAR_IMATGE = "ERROR_GENERAR_IMATGE";
    public static final String ERROR_IMATGE_EXISTENT = "ERROR_IMATGE_EXISTENT";
    public static final String ERROR_CAMP_BUIT = "ERROR_CAMP_BUIT";
    public static final String IMAGE_NAME_LABEL = "NOM_IMATGE_LABEL";
    public static final String SAVE_IMAGE_LABEL = "SAVE_IMAGE_LABEL";
    public static final String CAP_ALGORISME = "CAP_ALGORISME";
    public static final String SENSE_DESCRIPCIO = "SENSE_DESCRIPCIO";

    
    private static HashMap<String, String> insertData() {
        HashMap<String, String> result = new HashMap<>();
        result.put(ERROR_INESPERAT, "Error inesperat");
        result.put(ERROR_DESAR_IMATGE,"No s'ha pogut desar la imatge");
        result.put(ERROR_GENERAR_IMATGE,"No s'ha pogut generar la imatge");
        result.put(ERROR_IMATGE_EXISTENT,"Ja existeix aquest nom d'imatge");
        result.put(ERROR_CAMP_BUIT, "El camp no pot ser buit");
        result.put(IMAGE_NAME_LABEL, "Nom de la imatge:");
        result.put(SAVE_IMAGE_LABEL, "Desar imatge");
        result.put(CAP_ALGORISME, "No hi han algorismes");
        result.put(SENSE_DESCRIPCIO, "Sense descripció");
        
        return result;
    }

    public static String getData(String param) {
        String data;
        if (DATA.containsKey(param)){
            data = DATA.get(param);
        }else {
            data = DATA.get(ERROR_INESPERAT);
        }
        return DATA.get(param);
    }
}
