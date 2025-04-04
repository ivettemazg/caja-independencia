/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.com.evoti.bo.util;


import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Ivette Manzano
 */
public class Biblioteca {
    
    private List<SUSTITUIBLES> sustituibles = new ArrayList<>();
    private List<SUSTITUIBLES_COL> sustituiblesColonia = new ArrayList<>();
    private List<SUSTITUIBLES_MUN> sustituiblesMun = new ArrayList<>();
   
    private List<CARDINALES> cardinales = new ArrayList<>();
    private List<MESES> meses = new ArrayList<>();
    private List<TIPOS_CALLE> tiposCalle = new ArrayList<>();
    
      public String[] OMITIBLES = {
//                           "arquitecto",
//                           "capitan", "cd",  "cerro",  "coronel",
//                           "dguez", "doctor", "dr", "fray", "fte", 
//                           "general", "gral", "gobernador", "glz", "glez", "gonzalez", "gpe", "gober", "gob",
//                           "hdez", "hernandez", "ing", "inge", "ingeniero", "int",
//                           "km", "licenciado", "laguna", "lago","mar", "montes", "monte",
//                           "maestro", "mtro", "ntra", "num", "o", 
//                           "pdte", "presidente" ,"plaza",   "prof", "profesor", "profesora", "profr", "pza", "pla",
//                           "rep", "republica", "rio", "rosa","san", "sargento", "seccion", "sn", "sta", "santa",
//                           "teniente", "tnte", "sierra", "fuente", "rio", "paseo", "torre", "jardin", "cultura", "lomas",
//                           "villa", "golondrina", "gob", "rinconada", "estado", "rancho", "pto", "puerto", "flor", "club", "educacion",
//                           "cabo", "volcan", "desierto", "canal", "rosas", "loma", "bosque", "bosques", "molino", "molinos", "paso",
//                           "plan", "llano", "ejido", "playa", "puente", "crater", "golfo", "campo", "edo", "rey", "islas", "isla",
//                           "mision", "pradera", "obispo", "arco", "ley", "palma", "palmas", "santo", "agua", "manantiales", "cuesta", "presa", 
//                           "cruz", "toma", "senda", "prados", "pozo", "junta", "real", "interior", "convento", "cumbre", "cumbres",
//                           "parque", "ruta", "mina", "infanta", "embajada", "manantial", "bahia", "colina", "real", "hogar", "cordillera",
//                           "punta", "prado", "huerto", "vivero", "ingenio", "fuentes",
//                           "jose", "juan", "manuel", "luis", "angel", "daniel", "francisco", "fco", "jesus", "maria", "guadalupe", "antonio",
//                           "pablo", "carlos", "ignacio", "manuela", "lopez", "alfonso", "benjamin", "nicolas", "santiago"
                            };
      
      public String[] PALABRAS_OMITIBLES = {
                            "de", "del",  "el", "edif", "edificio", "edf", "ed",
                            "la", "las", "lic",  "los",  "no", "y",  "blv", "blvd", "blvrd", "boulevard",
                             "secc", "numero", "hda", "hacienda","frac", "fracc","fraccionamiento",
                             "col", "csa", "loc", "barranca", "barr",
                             "carretera", "carr", "carrt","calzada", "calz", "czda","autopista", "aut",
                             "boulevard", "blvd", "ampliacion", "amp", "avenida", "ave", "av", "diagonal", "diag",
                             "prolongacion", "prol", "eje", "circuito", "cto", "corredor", "corr", 
                             "retorno", "rtn", "rtno", "ret", "andador", "and", "anden", "callejon", "cjon", "cjn",
                             "cerrada", "cda", "cerr", "pasaje", "pje", "peatonal", "cerrada", "cda", "cerr",
                             "privada", "priv", "camino", "cmno", "calle", "call", "clle",
                             "adn", "ampl", "ampliacion", "bis"
              
                            };
      
      public String[] SIMBOLOS_OMITIBLES = {"!","¡","?","¿","#", "$", "%", "&", "/", "(", ")", ".", ",", ":", ";" ,"-", "_", "°", "'", "´"};
      
      public String[] NUMEROS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
      
      public String[] LETRAS = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
                                "k", "l", "m", "n", "ñ", "o", "p", "q", "r", "s",
                                 "t", "u", "v", "w", "x", "y", "z"};
      
      public String[] MANZANA = {"manzana", "mz", "mza", "mna", "man", "mnz", "mzna", "m", "ma", "mazn" };
      public String[] LOTE = {"lot", "lote", "lt", "lte", "l"};
      
      public Biblioteca(){
          agregaSustituibles();
          agregaSustituiblesColonia();
          agregaSustituiblesMunicipio();
          agregaMeses();
          agregaCardinales();
          agregaTiposCalle();
      }
      
      private void agregaSustituibles(){
        getSustituibles().add(SUSTITUIBLES.PRIMERA);
        getSustituibles().add(SUSTITUIBLES.SEGUNDA);
        getSustituibles().add(SUSTITUIBLES.TERCERA);
        getSustituibles().add(SUSTITUIBLES.CUARTA);
        getSustituibles().add(SUSTITUIBLES.QUINTA);
        getSustituibles().add(SUSTITUIBLES.SEXTA);
        getSustituibles().add(SUSTITUIBLES.SEPTIMA);
        getSustituibles().add(SUSTITUIBLES.OCTAVA);
        getSustituibles().add(SUSTITUIBLES.NOVENA);
        getSustituibles().add(SUSTITUIBLES.DECIMA);
        getSustituibles().add(SUSTITUIBLES.NORTE);
        getSustituibles().add(SUSTITUIBLES.SUR);
        getSustituibles().add(SUSTITUIBLES.PONIENTE);
        getSustituibles().add(SUSTITUIBLES.ORIENTE);
        getSustituibles().add(SUSTITUIBLES.ENERO);
        getSustituibles().add(SUSTITUIBLES.FEBRERO);
        getSustituibles().add(SUSTITUIBLES.MARZO);
        getSustituibles().add(SUSTITUIBLES.MAYO);
        getSustituibles().add(SUSTITUIBLES.JUNIO);
        getSustituibles().add(SUSTITUIBLES.JULIO);
        getSustituibles().add(SUSTITUIBLES.AGOSTO);
        getSustituibles().add(SUSTITUIBLES.SEPTIEMBRE);
        getSustituibles().add(SUSTITUIBLES.OCTUBRE);
        getSustituibles().add(SUSTITUIBLES.NOVIEMBRE);
        getSustituibles().add(SUSTITUIBLES.DICIEMBRE);
        getSustituibles().add(SUSTITUIBLES.CIUDAD);
        getSustituibles().add(SUSTITUIBLES.DOCTOR);
        getSustituibles().add(SUSTITUIBLES.GOBERNADOR);
        getSustituibles().add(SUSTITUIBLES.INGENIERO);
    }
   
      private void agregaSustituiblesMunicipio(){
        getSustituiblesMun().add(SUSTITUIBLES_MUN.AGUASCALIENTES);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.NORTE);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.SUR);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.PONIENTE);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.ORIENTE);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.ENERO);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.FEBRERO);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.MARZO);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.MAYO);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.JUNIO);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.JULIO);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.AGOSTO);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.SEPTIEMBRE);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.OCTUBRE);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.NOVIEMBRE);
        getSustituiblesMun().add(SUSTITUIBLES_MUN.DICIEMBRE);
      }
      private void agregaSustituiblesColonia(){
        getSustituiblesColonia().add(SUSTITUIBLES_COL.AGUASCALIENTES);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.NORTE);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.SUR);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.PONIENTE);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.ORIENTE);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.ENERO);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.FEBRERO);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.MARZO);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.MAYO);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.JUNIO);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.JULIO);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.AGOSTO);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.SEPTIEMBRE);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.OCTUBRE);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.NOVIEMBRE);
        getSustituiblesColonia().add(SUSTITUIBLES_COL.DICIEMBRE);
      }
      
      private void agregaCardinales(){
        getCardinales().add(CARDINALES.NORTE);
        getCardinales().add(CARDINALES.SUR);
        getCardinales().add(CARDINALES.PONIENTE);
        getCardinales().add(CARDINALES.ORIENTE);
    }
   
      private void agregaMeses(){
          getMeses().add(MESES.ENERO);
          getMeses().add(MESES.FEBRERO);
          getMeses().add(MESES.MARZO);
          getMeses().add(MESES.ABRIL);
          getMeses().add(MESES.MAYO);
          getMeses().add(MESES.JUNIO);
          getMeses().add(MESES.JULIO);
          getMeses().add(MESES.AGOSTO);
          getMeses().add(MESES.SEPTIEMBRE);
          getMeses().add(MESES.OCTUBRE);
          getMeses().add(MESES.NOVIEMBRE);
          getMeses().add(MESES.DICIEMBRE);
      }

      
       private void agregaTiposCalle(){
          getTiposCalle().add(TIPOS_CALLE.CARRETERA);
          getTiposCalle().add(TIPOS_CALLE.CALZADA);
          getTiposCalle().add(TIPOS_CALLE.AUTOPISTA);
          getTiposCalle().add(TIPOS_CALLE.BOULEVARD);
          getTiposCalle().add(TIPOS_CALLE.AMPLIACION);
          getTiposCalle().add(TIPOS_CALLE.AVENIDA);
          getTiposCalle().add(TIPOS_CALLE.DIAGONAL);
          getTiposCalle().add(TIPOS_CALLE.PROLONGACION);
          getTiposCalle().add(TIPOS_CALLE.EJEVIAL);
          getTiposCalle().add(TIPOS_CALLE.CIRCUITO);
          getTiposCalle().add(TIPOS_CALLE.CORREDOR);
          getTiposCalle().add(TIPOS_CALLE.RETORNO);
          getTiposCalle().add(TIPOS_CALLE.ANDADOR);
          getTiposCalle().add(TIPOS_CALLE.CALLEJON);
          getTiposCalle().add(TIPOS_CALLE.CERRADA);
          getTiposCalle().add(TIPOS_CALLE.PASAJE);
          getTiposCalle().add(TIPOS_CALLE.PEATONAL);
          getTiposCalle().add(TIPOS_CALLE.PLAZA);
          getTiposCalle().add(TIPOS_CALLE.PRIVADA);
          getTiposCalle().add(TIPOS_CALLE.CAMINO);
          
      }

    /**
     * @return the sustituiblesMun
     */
    public List<SUSTITUIBLES_MUN> getSustituiblesMun() {
        return sustituiblesMun;
    }

    /**
     * @param sustituiblesMun the sustituiblesMun to set
     */
    public void setSustituiblesMun(List<SUSTITUIBLES_MUN> sustituiblesMun) {
        this.sustituiblesMun = sustituiblesMun;
    }

    /**
     * @return the sustituiblesColonia
     */
    public List<SUSTITUIBLES_COL> getSustituiblesColonia() {
        return sustituiblesColonia;
    }

    /**
     * @param sustituiblesColonia the sustituiblesColonia to set
     */
    public void setSustituiblesColonia(List<SUSTITUIBLES_COL> sustituiblesColonia) {
        this.sustituiblesColonia = sustituiblesColonia;
    }
   
     
      public enum MESES {
          ENERO (new String[] {"enero", "ene"}),
          FEBRERO (new String[] {"febrero", "feb"}),
          MARZO (new String[] {"marzo", "mzo"}),
          ABRIL (new String[] {"abril"}),
          MAYO (new String[] {"mayo", "may"}),
          JUNIO (new String[] {"junio"}),
          JULIO (new String[] {"julio", "jul"}),
          AGOSTO (new String[] {"agosto"}),
          SEPTIEMBRE (new String[] {"septiembre", "sep"}),
          OCTUBRE (new String[] {"octubre", "oct"}),
          NOVIEMBRE (new String[] {"noviembre", "nov"}),
          DICIEMBRE (new String[] {"diciembre", "dic"});
          
          String[] mesNombre;

          MESES(String[] nombre){
              mesNombre = nombre;
          }
          
          public void setMesNombre(String[] nombre){
              mesNombre = nombre;
          }
          
          public String[] getMesNombre(){
              return mesNombre;
          }
      }
      
      public enum CARDINALES {
          NORTE ("norte", new String[] {"nte"}),
          SUR ("sur", new String[] {"sur"}),
          ORIENTE ("oriente", new String[] {"ote"}),
          PONIENTE ("poniente", new String[] {"pte"});
          
          String nombrePto;
          String[] sustituibles;

          CARDINALES(String nombre, String[] sustituiblesN){
              nombrePto = nombre;
              sustituibles = sustituiblesN;
          }
          
          public void setNombrePto(String nombre){
              nombrePto = nombre;
          }
          
          public String getNombrePto(){
              return nombrePto;
          }
          
          public void setSustituibles(String[] nombre){
              sustituibles = nombre;
          }
          
          public String[] getSustituibles(){
              return sustituibles;
          }
      }
      
      
      /**
       * Probablemente se podrían meter todos los términos que tienen abreviaciones y unificarlos a la hora
       * de buscar y comparar en la base de datos
       */
    public enum SUSTITUIBLES {
        
            PRIMERA(1, new String[] {"1a", "1ra", "1era","1er", "primer", "primera"}, "primera"),
            SEGUNDA(2, new String[] {"2a", "2da", "2o", "2do", "segundo", "segunda"}, "segunda"),
            TERCERA(3, new String[] {"3a", "3ra", "3era","3er", "tercer", "tercera"}, "tercera"),
            CUARTA(4, new String[] {"4a", "4ta","4to","4o", "cuarto", "cuarta"}, "cuarta"),
            QUINTA(5, new String[] {"5a", "5ta","5o","5to", "quinto", "quinta"}, "quinta"),
            SEXTA(6, new String[] {"6ta", "6a","6o","6to", "sexto", "sexta"}, "sexta"),
            SEPTIMA(7, new String[] {"7a", "7ma","7mo","7o", "septimo", "septima"},"septima"),
            OCTAVA(8, new String[] {"8va", "8a","8vo", "8o", "octavo", "octava"}, "octava"),
            NOVENA(9, new String[] {"9a", "9na", "9no","9o","noveno", "novena"}, "novena"),
            DECIMA(10, new String[] {"10a", "10ma","10mo","10o", "decimo", "decima"}, "decima"),
            NORTE (11, new String[] {"nte", "norte"}, "norte"),
            SUR (12, new String[] {"sur"}, "sur"),
            ORIENTE (13, new String[] {"ote", "oriente"}, "oriente"),
            PONIENTE (14, new String[] {"pte", "poniente"}, "poniente"),
            ENERO (15, new String[] {"ene", "enero"}, "enero"),
            FEBRERO (16, new String[] {"feb", "febrero"}, "febrero"),
            MARZO (17, new String[] {"mzo", "marzo"}, "marzo"),
            MAYO (18, new String[] {"may", "mayo"}, "mayo"),
            JUNIO (19, new String[] {"jun", "junio"}, "junio"),
            JULIO (20, new String[] {"jul", "julio"}, "julio"),
            AGOSTO (21, new String[] {"ago", "agosto"}, "agosto"),
            SEPTIEMBRE (22, new String[] {"sep", "septiembre"}, "septiembre"),
            OCTUBRE (23, new String[] {"oct", "octubre"}, "octubre"),
            NOVIEMBRE (24, new String[] {"nov", "noviembre"}, "noviembre"),
            DICIEMBRE (25, new String[] {"dic", "diciembre"}, "diciembre"),
            CIUDAD(26, new String[] {"cd"}, "ciudad"),
            DOCTOR(27, new String[] {"dr", "doc"}, "doctor"),
            GOBERNADOR(28, new String[] {"gob", "gober"}, "gobernador"),
            INGENIERO(29, new String[] {"ing", "inge"}, "ingeniero"),
            ;
            
            private int id;
            private String arrNum[];
            private String txt;
            
            
            SUSTITUIBLES(int idSust, String arrNum[], String txt){
                this.arrNum = arrNum;
                this.txt = txt;
                this.id = idSust;
            }

        /**
         * @return the arrNum
         */
        public String[] getArrNum() {
            return arrNum;
        }

        /**
         * @param arrNum the arrNum to set
         */
        public void setArrNum(String[] arrNum) {
            this.arrNum = arrNum;
        }

        /**
         * @return the txt
         */
        public String getTxt() {
            return txt;
        }

        /**
         * @param txt the txt to set
         */
        public void setTxt(String txt) {
            this.txt = txt;
        }

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(int id) {
            this.id = id;
        }
          
    }
    
     public enum SUSTITUIBLES_MUN {
        
            AGUASCALIENTES(1, new String[] {"ag", "ags", "aguascalientes"}, "aguascalientes"),
            NORTE (11, new String[] {"nte", "norte"}, "norte"),
            SUR (12, new String[] {"sur"}, "sur"),
            ORIENTE (13, new String[] {"ote", "oriente"}, "oriente"),
            PONIENTE (14, new String[] {"pte", "poniente"}, "poniente"),
            ENERO (15, new String[] {"ene", "enero"}, "enero"),
            FEBRERO (16, new String[] {"feb", "febrero"}, "febrero"),
            MARZO (17, new String[] {"mzo", "marzo"}, "marzo"),
            MAYO (18, new String[] {"may", "mayo"}, "mayo"),
            JUNIO (19, new String[] {"jun", "junio"}, "junio"),
            JULIO (20, new String[] {"jul", "julio"}, "julio"),
            AGOSTO (21, new String[] {"ago", "agosto"}, "agosto"),
            SEPTIEMBRE (22, new String[] {"sep", "septiembre"}, "septiembre"),
            OCTUBRE (23, new String[] {"oct", "octubre"}, "octubre"),
            NOVIEMBRE (24, new String[] {"nov", "noviembre"}, "noviembre"),
            DICIEMBRE (25, new String[] {"dic", "diciembre"}, "diciembre");
        
            private int id;
            private String arrNum[];
            private String txt;
            
            
            SUSTITUIBLES_MUN(int idSust, String arrNum[], String txt){
                this.arrNum = arrNum;
                this.txt = txt;
                this.id = idSust;
            }

        /**
         * @return the arrNum
         */
        public String[] getArrNum() {
            return arrNum;
        }

        /**
         * @param arrNum the arrNum to set
         */
        public void setArrNum(String[] arrNum) {
            this.arrNum = arrNum;
        }

        /**
         * @return the txt
         */
        public String getTxt() {
            return txt;
        }

        /**
         * @param txt the txt to set
         */
        public void setTxt(String txt) {
            this.txt = txt;
        }

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(int id) {
            this.id = id;
        }
          
    }
    
          public enum SUSTITUIBLES_COL {
        
            AGUASCALIENTES(1, new String[] {"ag", "ags", "aguascalientes"}, "aguascalientes"),
            NORTE (11, new String[] {"nte", "norte"}, "norte"),
            SUR (12, new String[] {"sur"}, "sur"),
            ORIENTE (13, new String[] {"ote", "oriente"}, "oriente"),
            PONIENTE (14, new String[] {"pte", "poniente"}, "poniente"),
            ENERO (15, new String[] {"ene", "enero"}, "enero"),
            FEBRERO (16, new String[] {"feb", "febrero"}, "febrero"),
            MARZO (17, new String[] {"mzo", "marzo"}, "marzo"),
            MAYO (18, new String[] {"may", "mayo"}, "mayo"),
            JUNIO (19, new String[] {"jun", "junio"}, "junio"),
            JULIO (20, new String[] {"jul", "julio"}, "julio"),
            AGOSTO (21, new String[] {"ago", "agosto"}, "agosto"),
            SEPTIEMBRE (22, new String[] {"sep", "septiembre"}, "septiembre"),
            OCTUBRE (23, new String[] {"oct", "octubre"}, "octubre"),
            NOVIEMBRE (24, new String[] {"nov", "noviembre"}, "noviembre"),
            DICIEMBRE (25, new String[] {"dic", "diciembre"}, "diciembre");
        
            private int id;
            private String arrNum[];
            private String txt;
            
            
            SUSTITUIBLES_COL(int idSust, String arrNum[], String txt){
                this.arrNum = arrNum;
                this.txt = txt;
                this.id = idSust;
            }

        /**
         * @return the arrNum
         */
        public String[] getArrNum() {
            return arrNum;
        }

        /**
         * @param arrNum the arrNum to set
         */
        public void setArrNum(String[] arrNum) {
            this.arrNum = arrNum;
        }

        /**
         * @return the txt
         */
        public String getTxt() {
            return txt;
        }

        /**
         * @param txt the txt to set
         */
        public void setTxt(String txt) {
            this.txt = txt;
        }

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(int id) {
            this.id = id;
        }
          
    }
     
     
    public enum TIPOS_CALLE{
        CARRETERA(1, "carretera", new String[]{"carretera", "carr", "carrt"}),
        CALZADA(2, "calzada", new String[]{"calzada", "calz", "czda"}),
        AUTOPISTA(3, "autopista", new String[]{"autopista", "aut"}),
        BOULEVARD(4, "boulevard", new String[]{"boulevard", "blvd"}),
        AMPLIACION(5, "ampliacion", new String[]{"ampliacion", "amp"}),
        AVENIDA(6, "avenida", new String[]{"avenida", "ave", "av"}),
        DIAGONAL(7, "diagonal", new String[]{"diagonal", "diag"}),
        PROLONGACION(8, "prolongacion", new String[]{"prolongacion", "prol"}),
        EJEVIAL(9, "ejevial", new String[]{"eje"}),
        CIRCUITO(11, "circuito", new String[]{"circuito", "cto"}),
        CORREDOR(12, "corredor", new String[]{"corredor", "corr"}),
        RETORNO(13, "retorno", new String[]{"retorno", "rtn", "rtno", "ret"}),
        ANDADOR(14, "andador", new String[]{"andador", "and", "anden", "adn"}),
        CALLEJON(15, "callejon", new String[]{"callejon", "cjon", "cjn"}),
        CERRADA(16, "cerrada", new String[]{"cerrada", "cda", "cerr"}),
        PASAJE(17, "pasaje", new String[]{"pasaje", "pje"}),
        PEATONAL(18, "peatonal", new String[]{"peatonal"}),
        PLAZA(19, "cerrada", new String[]{"cerrada", "cda", "cerr"}),
        PRIVADA(20, "privada", new String[]{"privada", "priv"}),
        CAMINO(22, "camino", new String[]{"camino", "cmno"}),
//        CALLE(10, "calle", new String[]{"calle", "call"}),
//        SINREFERENCIA(21, "sinreferencia", new String[]{""}),
//        NINGUNO(23, "ninguno", new String[]{""}),
//        OTRO(24, "otro", new String[]{""})
        
        ;
        
         TIPOS_CALLE(int id, String nombre, String[] sustituiblesN){
             identificador = id; 
             nombreTipo = nombre;
             sustituibles = sustituiblesN;
          }
         
         private int identificador;
         private String nombreTipo;
         private String[] sustituibles;

        /**
         * @return the identificador
         */
        public int getIdentificador() {
            return identificador;
        }

        /**
         * @param identificador the identificador to set
         */
        public void setIdentificador(int identificador) {
            this.identificador = identificador;
        }

        /**
         * @return the nombreTipo
         */
        public String getNombreTipo() {
            return nombreTipo;
        }

        /**
         * @param nombreTipo the nombreTipo to set
         */
        public void setNombreTipo(String nombreTipo) {
            this.nombreTipo = nombreTipo;
        }

        /**
         * @return the sustituibles
         */
        public String[] getSustituibles() {
            return sustituibles;
        }

        /**
         * @param sustituibles the sustituibles to set
         */
        public void setSustituibles(String[] sustituibles) {
            this.sustituibles = sustituibles;
        }
        
    }
    
    
    public List<SUSTITUIBLES> getSustituibles() {
        return sustituibles;
    }
   
    public List<CARDINALES> getCardinales() {
        return cardinales;
    }
   
    
    public List<MESES> getMeses() {
        return meses;
    }
    
    public List<TIPOS_CALLE> getTiposCalle() {
        return tiposCalle;
    }
   
}
