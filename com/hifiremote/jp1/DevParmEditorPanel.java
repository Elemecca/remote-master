package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class DevParmEditorPanel
  extends ProtocolEditorPanel
  implements DocumentListener, ChangeListener
{
  public DevParmEditorPanel()
  {
    super( "Device Parameter" );
    setLayout( new BorderLayout());

    SpringLayout layout = new SpringLayout();
    JPanel panel = new JPanel( layout );
    panel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    add( panel, BorderLayout.NORTH );
    name = new JTextField();
    name.getDocument().addDocumentListener( this );
    bits = new JSpinner( new SpinnerNumberModel( 8, 0, 16, 1 ));
    bits.addChangeListener( this );
    bits.setMaximumSize( bits.getPreferredSize());

    String[] labels = { "Name", "Bits" };
    JComponent[] fields = { name, bits };
    String[] toolTipText = { "Enter the name of the parameter.  This is a required field.",
                             "Enter the number of bits for this parameter." };

    boolean[] required = { true, true };
    int numPairs = labels.length;
    JLabel prevLabel = null;
    for ( int i = 0; i < numPairs; i++)
    {
      JLabel l = new JLabel(labels[i], JLabel.TRAILING);
      panel.add( l );
      if ( required[ i ] )
        l.setForeground( Color.RED );
      JComponent c = fields[ i ];
      l.setLabelFor( c );
      c.setToolTipText( toolTipText[ i ]);
      panel.add( c );
    }

    // Lay out the panel.
    SpringUtilities.makeCompactGrid( panel,
                                     numPairs, 2,  // rows, cols 
                                     5, 5,      // initX, initY
                                     5, 5 );    // xPad, yPad

  }

  public void update( ProtocolEditorNode newNode )
  {
    node = ( DevParmEditorNode )newNode;
    name.setText( node.getName());
    bits.setValue( new Integer( node.getBits()));
  }

  // DocumentListener methods
  private void docChanged( DocumentEvent e )
  {
    Document doc = e.getDocument();
    if ( doc == name.getDocument())
      node.setName( name.getText());
  }

  public void changedUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  public void insertUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  public void removeUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  // ChangeListener methods
  public void stateChanged( ChangeEvent e )
  {
    Object source = e.getSource();
    if ( source == bits )
    {
      if ( node != null )
        node.setBits((( Integer )bits.getValue()).intValue());
    }
  }

  private DevParmEditorNode node = null;
  private JTextField name = null;
  private JSpinner bits = null;

}
