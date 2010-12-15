/*
 * Created by JFormDesigner on Tue Dec 07 12:32:06 GMT-03:00 2010
 */

package aplicarFiltros;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Vector;
import javax.swing.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.*;

import objeto.Clase;
import objeto.Objeto;

/**
 * @author Oscar Giorgetti
 */
public class ClasePanel extends JPanel {
	
	public ClasePanel(Clase clase,List<Objeto> objetos) {
		initComponents();
		if (clase != null)
			labelClase.setText(clase.getNombre());
		else
			labelClase.setText("");
		
		if (objetos != null){
			Vector<ObjetoPanel> data = new Vector<ObjetoPanel>();
			DefaultTableModel tableModel = new DefaultTableModel();
			for (Objeto obj: objetos) {
				ObjetoPanel panel = new ObjetoPanel(obj); 
				data.add(panel);
				tableModel.addRow(new Object[]{panel});
				//this.panelObjetos.add(panel);
			}
			
			tableObjetos.setModel(tableModel);
			//listObjetos.setListData(data);
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Oscar Giorgetti
		panelCabecera = new JPanel();
		labelClase = new JLabel();
		scrollPaneObjetos = new JScrollPane();
		tableObjetos = new JTable();

		//======== this ========

		// JFormDesigner evaluation mark
		setBorder(new javax.swing.border.CompoundBorder(
			new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
				"JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
				javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
				java.awt.Color.red), getBorder())); addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});

		setLayout(new BorderLayout());

		//======== panelCabecera ========
		{
			panelCabecera.setBorder(new BevelBorder(BevelBorder.LOWERED));
			panelCabecera.setLayout(new BoxLayout(panelCabecera, BoxLayout.X_AXIS));

			//---- labelClase ----
			labelClase.setText("Nombre Clase");
			labelClase.setFont(labelClase.getFont().deriveFont(labelClase.getFont().getStyle() | Font.BOLD));
			panelCabecera.add(labelClase);
		}
		add(panelCabecera, BorderLayout.WEST);

		//======== scrollPaneObjetos ========
		{

			//---- tableObjetos ----
			tableObjetos.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
					null
				}
			));
			scrollPaneObjetos.setViewportView(tableObjetos);
		}
		add(scrollPaneObjetos, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Oscar Giorgetti
	private JPanel panelCabecera;
	private JLabel labelClase;
	private JScrollPane scrollPaneObjetos;
	private JTable tableObjetos;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}