package vista.PlantillaVista;

import javax.swing.JPanel;

public class CrearSubPanel{//Este metodo evita repetir el codigo de la creacion de subPaneles para envolver los componentes de las paginas.
    
    public static JPanel subPanel(){
        JPanel subPanel = new JPanel();
        subPanel.setOpaque(false);
        return subPanel;
    }
}
