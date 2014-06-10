/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ioc.wiki.processingmanager.applet;

import ioc.wiki.processingmanager.ImageGenerator;
import ioc.wiki.processingmanager.data.DataManager;
import static ioc.wiki.processingmanager.data.DataManager.ERROR_CAMP_BUIT;
import static ioc.wiki.processingmanager.data.DataManager.ERROR_DESAR_IMATGE;
import static ioc.wiki.processingmanager.data.DataManager.ERROR_GENERAR_IMATGE;
import static ioc.wiki.processingmanager.data.DataManager.ERROR_IMATGE_EXISTENT;
import static ioc.wiki.processingmanager.data.DataManager.IMAGE_NAME_LABEL;
import static ioc.wiki.processingmanager.data.DataManager.SAVE_IMAGE_LABEL;
import ioc.wiki.processingmanager.excepcions.ProcessingImageException;
import ioc.wiki.processingmanager.excepcions.ProcessingLoaderException;
import ioc.wiki.processingmanager.http.HttpCommandSender;
import ioc.wiki.processingmanager.http.HttpFileSender;
import ioc.wiki.processingmanager.loader.PdeLoaderManager;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Daniel Criado Casas<dani.criado.casas@gmail.com>
 */
public class ImageGeneratorApplet extends javax.swing.JApplet {

    private final static String URLS_PARAM = "urls";
    private final static String COOKIES_PARAM = "Cookie";
    private final static String SECTOK_PARAM = "sectok";
    private final static String FILE_SENDER_URL_PARAM = "fileSenderURL";
    private final static String IMAGE_NAME_PARAM = "imageName";
    private final static String NAME_SENDER_URL_PARAM = "nameSenderURL";
    private final static String PDE_CLASSES_URL_PARAM = "getPdeClassesURL";
    //JSON
    private final static String VALUE_PARAM = "value";
    private final static String CODE_PARAM = "code";
    private final static String INFO_PARAM = "info";
    private final static String N_ALGORISMES_PARAM = "n_algorismes";
    private final static String ALGORISMES_PARAM = "algorismes";
    private final static String ALGORISME_PARAM = "algorisme";

    private final static String COMMA = ",";
    private final static String SLASH = "/";
    private final static String IMAGE_EXTENSION = ".png";
    private final static int NAME_NOT_EXISTS_RESPONSE = 2;
    private final static String JAVA_IO_TMPDIR = "java.io.tmpdir";

    PdeLoaderManager pdeLoaderManager;
    ImageGenerator pdeApplet;
    HttpFileSender fileSender;

    /**
     * Initializes the applet ImageApplet
     */
    @Override
    public void init() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ImageGeneratorApplet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ImageGeneratorApplet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ImageGeneratorApplet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ImageGeneratorApplet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the applet */
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    initComponents();

                    getRootPane().setDefaultButton(jbGenerar);
                    //PDE LOAD MANAGER
                    pdeLoaderManager = new PdeLoaderManager();
                    //urls de les llibreries i classes a carregar
                    String urls = getParameter(URLS_PARAM);
                    String[] array_urls = urls.split(COMMA);
                    for (int i = 0; i < array_urls.length; ++i) {
                        pdeLoaderManager.addSourceUrl(array_urls[i]);
                    }

                    //GET PDE CLASSES
                    String cookies = getParameter(COOKIES_PARAM);
                    String sectok = getParameter(SECTOK_PARAM);
                    String url = getParameter(PDE_CLASSES_URL_PARAM);
                    getPdeClassList(cookies, sectok, url);//Genera el desplegable amb els algorismes
                    setDescripcio();

                    //FILE SENDER
                    fileSender = new HttpFileSender();
                    fileSender.setCookies(getParameter(COOKIES_PARAM));
                    fileSender.setParameter(SECTOK_PARAM, getParameter(SECTOK_PARAM));
                    fileSender.setUrl(getParameter(FILE_SENDER_URL_PARAM));

                    jcbAlgorismes.requestFocusInWindow();

                }
            });

        } catch (Exception ex) {
//            ex.printStackTrace();
            java.util.logging.Logger.getLogger("ImageGeneratorApplet")
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (this.pdeApplet != null & this.pdeApplet.isActive()) {
            this.pdeApplet.stop();
        }

    }

    /**
     * Especifica quina llavor ha d'utilitzar l'algorisme.
     *
     * @param seed llavor
     */
    public void setSeed(String seed) {
        this.pdeApplet.setSeed(seed);
    }

    /**
     * Assigna el nou ImageGenerator en el applet.
     *
     * @param generator nou ImageGenerator
     * @throws ProcessingLoaderException es llença quan no s'ha pogut afegir el
     * nou ImageGenerator.
     */
    private void setImageGenerator(ImageGenerator generator)
            throws ProcessingLoaderException {
        //Ja teniem una instancia
        if (this.pdeApplet != null) {
            if (this.pdeApplet.isActive()) {//Aturarla
                this.pdeApplet.stop();
            }
            //Temporal de la que ja estaba per si trobem algun problema.
            ImageGenerator tmp = this.pdeApplet;
            try {
//                this.jpApplet.remove(this.pdeApplet);
                this.jpApplet.remove(this.pdeApplet);
                this.pdeApplet = generator;
                this.jpApplet.add(this.pdeApplet);
//                this.jpApplet.add(this.pdeApplet);
            } catch (Exception ex) {
                this.pdeApplet = tmp;
                throw new ProcessingLoaderException(ex);
            }
            tmp.dispose();

        } else {//Encara no haviem generat cap imatge
            this.pdeApplet = generator;
            this.jpApplet.add(this.pdeApplet);
        }
    }

    /**
     * Inicialitza el PApplet per generar la imatge amb l'algorisme que tenim
     * seleccionat.
     */
    public void generateImage() {
        try {
            Algorisme algorisme = (Algorisme) jcbAlgorismes.getSelectedItem();
            String className = algorisme.getClasse();
            ImageGenerator imageGenerator = pdeLoaderManager.getNewInstance(className);
            setImageGenerator(imageGenerator);
            setSeed(this.jtfLlavor.getText());
            this.pdeApplet.init();
            this.jpApplet.setSize(pdeApplet.getHeight(), pdeApplet.getWidth());
        } catch (ProcessingLoaderException ex) {
            //MOSTRAR L'ERROR EN L'APPLET.
            JOptionPane.showMessageDialog(this, DataManager.getData(ERROR_GENERAR_IMATGE));
//            ex.printStackTrace();
            java.util.logging.Logger.getLogger(ImageGeneratorApplet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    /**
     * Guarda la imatge de l'applet en el servidor amb el nom indicat per
     * l'usuari amb extensió .png.
     */
    public void saveImage() {
        String error = "";
        String imageName = preguntarNomImatge(error);
        while (imageName != null & !existsImage(imageName)) {
            imageName = preguntarNomImatge(DataManager.getData(ERROR_IMATGE_EXISTENT));
        }
        if (imageName != null) {
            byte[] image = null;
            try {
                image = saveLocalImage(imageName);
            } catch (ProcessingImageException ex) {
                image = null;
                JOptionPane.showMessageDialog(this, DataManager.getData(ERROR_DESAR_IMATGE));
//                ex.printStackTrace();
                java.util.logging.Logger.getLogger(ImageGeneratorApplet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            if (image != null) {
                //Envia la imatge al servidor
                System.out.println(this.fileSender.toString());
                this.fileSender.setImageToSend(imageName, image);
                String response = this.fileSender.sendCommand();
                JsonObject json = (Json.createReader(new StringReader(response)))
                        .readArray().getJsonObject(0);
                JsonObject value = json.getJsonObject(VALUE_PARAM);
                String info = value.getString(INFO_PARAM);
                JOptionPane.showMessageDialog(this, info);
            }
        }
    }

    /**
     * Comprovar si ja existeix el nom de la imatge.
     *
     * @param imageName nom de la imatge
     * @return true si el nom de la imatge ja existeix en el servidor, altrament
     * false.
     */
    protected boolean existsImage(String imageName) {
        boolean exists = false;
        HttpCommandSender nameSender = new HttpCommandSender();
        nameSender.setCookies(getParameter(COOKIES_PARAM));
        nameSender.setParameter(SECTOK_PARAM, getParameter(SECTOK_PARAM));
        nameSender.setParameter(IMAGE_NAME_PARAM, imageName);
        nameSender.setUrl(getParameter(NAME_SENDER_URL_PARAM));
        String response = nameSender.sendCommand();
        //Tractar la resposta
        JsonObject json = (Json.createReader(new StringReader(response)))
                .readArray().getJsonObject(0);
        JsonObject value = json.getJsonObject(VALUE_PARAM);
        int code = value.getInt(CODE_PARAM);
        if (code == NAME_NOT_EXISTS_RESPONSE) {
            exists = true;
        }
        return exists;
    }

    /**
     * Petit formulari per preguntar el nom de la imatge que es vol guardar.
     *
     * @param error Missatge d'error en un string.
     * @return Retorna el nom de la imatge, amb extensió .png, introduït per
     * l'usuari.
     */
    protected String preguntarNomImatge(String error) {
        JPanel form = new JPanel(new GridLayout(0, 1));
        JLabel jlNom = new JLabel(IMAGE_NAME_LABEL);
        JTextField jtfNom = new JTextField(7);
        JLabel jlError = new JLabel(error);
        form.add(jlNom);
        form.add(jtfNom);
        form.add(jlError);
        jlError.setForeground(Color.red);
        int result = JOptionPane.showConfirmDialog(
                this, form, SAVE_IMAGE_LABEL,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        String nom = jtfNom.getText();

        //Surt del bucle quan:
        ///Han donat a cancel o a tancar
        ///Han introduït un nom NO buit.
        while (result == JOptionPane.OK_OPTION & nom.isEmpty()) {
            jlError.setText(DataManager.getData(ERROR_CAMP_BUIT));
            result = JOptionPane.showConfirmDialog(
                    this, form, SAVE_IMAGE_LABEL,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            nom = jtfNom.getText();
        }

        if (result == JOptionPane.CANCEL_OPTION | result == JOptionPane.CLOSED_OPTION) {
            nom = null;
        }
        if (nom != null) {
            if (!nom.endsWith(IMAGE_EXTENSION)) {
                nom += IMAGE_EXTENSION;
            }
        }
        return nom;
    }

    /**
     * Agafa els bytes de la imatge de l'applet.
     *
     * @param imageName Nom de la imatge amb extensió .png
     * @return Retorna els bytes de la imatge de l'applet.
     * @throws ProcessingImageException es llança quan no es pot agafar els
     * bytes de la imatge temporal que s'ha creat.
     */
    protected byte[] saveLocalImage(String imageName) throws ProcessingImageException {
        byte[] image = null;
        String path = System.getProperty(JAVA_IO_TMPDIR)+SLASH+imageName;
        try {
            this.pdeApplet.loadPixels();
            this.pdeApplet.save(path);
            image = this.pdeApplet.loadBytes(path);
        } catch (Exception ex) {
            throw new ProcessingImageException(ex);
        } finally {
            //PER SABER ON ES GUARDA LA IMATGE, NO LA ELIMINO.
            File file = new File(path);
            file.delete();
        }
        return image;
    }

    /**
     * Demana al servidor la llista d'algorismes i la posa en un desplegable.
     */
    public void getPdeClassList(String cookies, String sectok, String url) {
        HttpCommandSender getPdeClasses = new HttpCommandSender();
        getPdeClasses.setCookies(cookies);
        getPdeClasses.setParameter(SECTOK_PARAM, sectok);
        getPdeClasses.setUrl(url);

        String response = getPdeClasses.sendCommand();
        JsonObject json = (Json.createReader(new StringReader(response)))
                .readArray().getJsonObject(0);
        JsonObject jsonValue = json.getJsonObject(VALUE_PARAM);
        int n_algorismes = jsonValue.getInt(N_ALGORISMES_PARAM);

        this.jcbAlgorismes.removeAllItems();
        if (n_algorismes > 0) {
            ArrayList<Algorisme> algorismes = new ArrayList<>();
            JsonObject jsonAlgorismes = jsonValue.getJsonObject(ALGORISMES_PARAM);

            if (n_algorismes == 1) {
                JsonObject algorisme_json = jsonAlgorismes.getJsonObject(ALGORISME_PARAM);
                addItemFromJson(algorisme_json);
            } else {
                JsonArray jsonArrayAlgorismes = jsonAlgorismes.getJsonArray(ALGORISME_PARAM);
                for (int i = 0; i < jsonArrayAlgorismes.size(); i++) {
                    JsonObject algorisme_json = jsonArrayAlgorismes.getJsonObject(i);
                    addItemFromJson(algorisme_json);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, DataManager.getData(DataManager.CAP_ALGORISME));
        }
    }

    /**
     * Manipula el json per obtenir un Algorisme i afegirlo al desplegable.
     * @param algorisme_json un objecte json que conte els elements necessaris per construir un objecte Algorisme.
     */
    private void addItemFromJson(JsonObject algorisme_json) {
        String id = algorisme_json.getJsonObject("@attributes").getString(Algorisme.ID_PARAM);
        String nom = algorisme_json.getString(Algorisme.NOM_PARAM);
        String classe = algorisme_json.getString(Algorisme.CLASSE_PARAM);
        String descripcio = algorisme_json.getString(Algorisme.DESCRIPCIO_PARAM);
        Algorisme algorisme = new Algorisme(id, nom, classe, descripcio);
        jcbAlgorismes.addItem(algorisme);
    }

    /**
     * Buida el TextField on s'escriu la llavor.
     */
    private void buidarLlavor() {
        jtfLlavor.setText("");
    }

    /**
     * Defineix la descripció de l'algorisme seleccionat.
     */
    private void setDescripcio() {
        if (jcbAlgorismes.getSelectedItem() != null) {
            Algorisme algorisme = (Algorisme) jcbAlgorismes.getSelectedItem();
            jtaDescripcio.setText(algorisme.getDescripcio());
        }

    }

    /**
     * This method is called from within the init() method to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpButtons = new javax.swing.JPanel();
        jpAlgorismesButtons = new javax.swing.JPanel();
        jlAlgorismes = new javax.swing.JLabel();
        jcbAlgorismes = new javax.swing.JComboBox();
        jlDescripcio = new javax.swing.JLabel();
        jspDescripcio = new javax.swing.JScrollPane();
        jtaDescripcio = new javax.swing.JTextArea();
        jpLlavorButtons = new javax.swing.JPanel();
        jlLlavor = new javax.swing.JLabel();
        jtfLlavor = new javax.swing.JTextField();
        jbBuidarLlavor = new javax.swing.JButton();
        jpImageButtons = new javax.swing.JPanel();
        jlImatge = new javax.swing.JLabel();
        jbGenerar = new javax.swing.JButton();
        jbDesar = new javax.swing.JButton();
        jScrollImage = new javax.swing.JScrollPane();
        jpApplet = new javax.swing.JPanel();

        jpButtons.setPreferredSize(new java.awt.Dimension(290, 459));

        jpAlgorismesButtons.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jpAlgorismesButtons.setPreferredSize(new java.awt.Dimension(172, 70));

        jlAlgorismes.setDisplayedMnemonic('A');
        jlAlgorismes.setLabelFor(jcbAlgorismes);
        jlAlgorismes.setText("Algorismes");

        jcbAlgorismes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jcbAlgorismes.setToolTipText("Llista d'algoritmes a  seleccionar");
        jcbAlgorismes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbAlgorismesActionPerformed(evt);
            }
        });

        jlDescripcio.setText("Descripció");

        jtaDescripcio.setEditable(false);
        jtaDescripcio.setLineWrap(true);
        jtaDescripcio.setDisabledTextColor(new java.awt.Color(19, 15, 12));
        jtaDescripcio.setEnabled(false);
        jtaDescripcio.setFocusable(false);
        jspDescripcio.setViewportView(jtaDescripcio);

        javax.swing.GroupLayout jpAlgorismesButtonsLayout = new javax.swing.GroupLayout(jpAlgorismesButtons);
        jpAlgorismesButtons.setLayout(jpAlgorismesButtonsLayout);
        jpAlgorismesButtonsLayout.setHorizontalGroup(
            jpAlgorismesButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpAlgorismesButtonsLayout.createSequentialGroup()
                .addGap(93, 93, 93)
                .addComponent(jlDescripcio)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jpAlgorismesButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpAlgorismesButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jspDescripcio)
                    .addGroup(jpAlgorismesButtonsLayout.createSequentialGroup()
                        .addComponent(jlAlgorismes)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcbAlgorismes, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jpAlgorismesButtonsLayout.setVerticalGroup(
            jpAlgorismesButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpAlgorismesButtonsLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jpAlgorismesButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcbAlgorismes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlAlgorismes))
                .addGap(30, 30, 30)
                .addComponent(jlDescripcio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jspDescripcio, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
        );

        jpLlavorButtons.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jpLlavorButtons.setPreferredSize(new java.awt.Dimension(172, 70));

        jlLlavor.setText("Llavor");

        jtfLlavor.setColumns(7);
        jtfLlavor.setToolTipText("Introdueix una llavor");

        jbBuidarLlavor.setText("Buidar");
        jbBuidarLlavor.setToolTipText("Buida la llavor");
        jbBuidarLlavor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbBuidarLlavorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpLlavorButtonsLayout = new javax.swing.GroupLayout(jpLlavorButtons);
        jpLlavorButtons.setLayout(jpLlavorButtonsLayout);
        jpLlavorButtonsLayout.setHorizontalGroup(
            jpLlavorButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpLlavorButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jtfLlavor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbBuidarLlavor)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpLlavorButtonsLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jlLlavor, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(100, 100, 100))
        );
        jpLlavorButtonsLayout.setVerticalGroup(
            jpLlavorButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpLlavorButtonsLayout.createSequentialGroup()
                .addComponent(jlLlavor, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addGroup(jpLlavorButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtfLlavor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbBuidarLlavor))
                .addContainerGap())
        );

        jpImageButtons.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jpImageButtons.setPreferredSize(new java.awt.Dimension(172, 70));

        jlImatge.setText("Imatge");

        jbGenerar.setText("Generar");
        jbGenerar.setToolTipText("Genera la imatge");
        jbGenerar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbGenerarActionPerformed(evt);
            }
        });

        jbDesar.setText("Desar");
        jbDesar.setToolTipText("Desa la imatge");
        jbDesar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDesarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpImageButtonsLayout = new javax.swing.GroupLayout(jpImageButtons);
        jpImageButtons.setLayout(jpImageButtonsLayout);
        jpImageButtonsLayout.setHorizontalGroup(
            jpImageButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpImageButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jbGenerar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jbDesar, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jpImageButtonsLayout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(jlImatge, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpImageButtonsLayout.setVerticalGroup(
            jpImageButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpImageButtonsLayout.createSequentialGroup()
                .addComponent(jlImatge, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addGroup(jpImageButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jbGenerar)
                    .addComponent(jbDesar))
                .addContainerGap())
        );

        javax.swing.GroupLayout jpButtonsLayout = new javax.swing.GroupLayout(jpButtons);
        jpButtons.setLayout(jpButtonsLayout);
        jpButtonsLayout.setHorizontalGroup(
            jpButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpButtonsLayout.createSequentialGroup()
                .addGroup(jpButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jpAlgorismesButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addComponent(jpLlavorButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addComponent(jpImageButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE))
                .addContainerGap())
        );
        jpButtonsLayout.setVerticalGroup(
            jpButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jpButtonsLayout.createSequentialGroup()
                .addComponent(jpAlgorismesButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jpLlavorButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addComponent(jpImageButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jpApplet.setFocusable(false);

        javax.swing.GroupLayout jpAppletLayout = new javax.swing.GroupLayout(jpApplet);
        jpApplet.setLayout(jpAppletLayout);
        jpAppletLayout.setHorizontalGroup(
            jpAppletLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 530, Short.MAX_VALUE)
        );
        jpAppletLayout.setVerticalGroup(
            jpAppletLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 465, Short.MAX_VALUE)
        );

        jScrollImage.setViewportView(jpApplet);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollImage, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jpButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
            .addComponent(jScrollImage)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jbGenerarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbGenerarActionPerformed
        generateImage();
        jtfLlavor.requestFocusInWindow();
    }//GEN-LAST:event_jbGenerarActionPerformed

    private void jcbAlgorismesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbAlgorismesActionPerformed
        setDescripcio();
        jtfLlavor.requestFocusInWindow();
    }//GEN-LAST:event_jcbAlgorismesActionPerformed

    private void jbDesarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDesarActionPerformed
        saveImage();
        jtfLlavor.requestFocusInWindow();
    }//GEN-LAST:event_jbDesarActionPerformed

    private void jbBuidarLlavorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbBuidarLlavorActionPerformed
        buidarLlavor();
        jtfLlavor.requestFocusInWindow();
    }//GEN-LAST:event_jbBuidarLlavorActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollImage;
    private javax.swing.JButton jbBuidarLlavor;
    private javax.swing.JButton jbDesar;
    private javax.swing.JButton jbGenerar;
    private javax.swing.JComboBox jcbAlgorismes;
    private javax.swing.JLabel jlAlgorismes;
    private javax.swing.JLabel jlDescripcio;
    private javax.swing.JLabel jlImatge;
    private javax.swing.JLabel jlLlavor;
    private javax.swing.JPanel jpAlgorismesButtons;
    private javax.swing.JPanel jpApplet;
    private javax.swing.JPanel jpButtons;
    private javax.swing.JPanel jpImageButtons;
    private javax.swing.JPanel jpLlavorButtons;
    private javax.swing.JScrollPane jspDescripcio;
    private javax.swing.JTextArea jtaDescripcio;
    private javax.swing.JTextField jtfLlavor;
    // End of variables declaration//GEN-END:variables

    class Algorisme {

        private final static String ID_PARAM = "id";
        private final static String NOM_PARAM = "nom";
        private final static String CLASSE_PARAM = "classe";
        private final static String DESCRIPCIO_PARAM = "descripcio";

        private String id;
        private String nom;
        private String classe;
        private String descripcio;

        public Algorisme(String id, String nom, String classe, String descripcio) {
            this.id = id;
            this.nom = nom;
            this.classe = classe;
            this.descripcio = descripcio;
        }

        public Algorisme(String id, String nom, String classe) {
            this(id, nom, classe, "");
        }

        public String getId() {
            return this.id;
        }

        public String getNom() {
            return this.nom;
        }

        public String getClasse() {
            return this.classe;
        }

        public String getDescripcio() {
            return this.descripcio;
        }

        @Override
        public String toString() {
            return this.nom;
        }
    }
}
