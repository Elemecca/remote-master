package com.hifiremote.jp1;

import java.awt.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.text.*;

public class GeneralEditorPanel
  extends ProtocolEditorPanel
  implements DocumentListener, PropertyChangeListener
{
  public GeneralEditorPanel()
  {
    super( "General Settings" );
    setLayout( new BorderLayout());

    SpringLayout layout = new SpringLayout();
    JPanel panel = new JPanel( layout );
    add( panel, BorderLayout.NORTH );
    name = new JTextField( 20 );
    oldNames = new JTextField();
    MaskFormatter f = null;
    try
    {
      f = new MaskFormatter( "HH HH" );
    }
    catch (Exception e )
    {
      e.printStackTrace( System.err );
    }
    f.setValueClass( Hex.class );
    id = new JFormattedTextField( f );
    id.addPropertyChangeListener( "value", this );
    altId = new JFormattedTextField( f );
    altId.addPropertyChangeListener( "value", this );

    String[] labels = { "Name", "Old names", "ID", "Alternate ID" };
    JTextField[] fields = { name, oldNames, id, altId };
    String[] toolTipText = { "Enter the name of the protocol.  This is a required field.",
                             "Enter the names, separated by comman, that have been for this protocol, or have been used by KM.",
                             "Enter the hex identifier for this protocol.  This is a required fields.",
                             "Enter the alternate ID for this protocol." };

    boolean[] required = { true, false, true, false };
    int numPairs = labels.length;
    JLabel prevLabel = null;
    for ( int i = 0; i < numPairs; i++)
    {
      JLabel l = new JLabel(labels[i], JLabel.TRAILING);
      panel.add( l );
      if ( required[ i ] )
        l.setForeground( Color.RED );
      JTextField textField = fields[ i ];
      textField.getDocument().addDocumentListener( this );
      l.setLabelFor( textField );
      textField.setToolTipText( toolTipText[ i ]);
      panel.add( textField );
    }

    // Lay out the panel.
    SpringUtilities.makeCompactGrid( panel,
                                     numPairs, 2,  // rows, cols 
                                     5, 5,      // initX, initY
                                     5, 5 );    // xPad, yPad

  }

  public void commit(){;}
  public void update( ProtocolEditorNode newNode )
  {
    node = ( GeneralEditorNode )newNode;
    name.setText( node.getName());
    oldNames.setText( node.getOldNames());
    id.setText( node.getId().toString());
    altId.setText( node.getAltId().toString());
  }

  // DocumentListener methods
  public void docChanged( DocumentEvent e )
  {
    Document doc = e.getDocument();
    if ( doc == name.getDocument() )
      node.setName( name.getText());
    else if ( doc == oldNames.getDocument())
      node.setOldNames( oldNames.getText());
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

  // PropertyChangeListener methods
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    if ( source == id )
      node.setId( ( Hex )id.getValue());
    else if ( source == altId )
      node.setAltId( ( Hex )altId.getValue());
  }

  private GeneralEditorNode node = null;
  private JTextField name = null;
  private JTextField oldNames = null;
  private JFormattedTextField id = null;
  private JFormattedTextField altId = null;
}
