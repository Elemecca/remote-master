package com.hifiremote.jp1;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.text.*;

public class FixedDataEditorPanel
  extends ProtocolEditorPanel
  implements PropertyChangeListener
{
  public FixedDataEditorPanel()
  {
    super( "Device Parameters" );
    setLayout( new BorderLayout());
    JPanel panel = new JPanel( new BorderLayout( 5, 5 ));
    panel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    add( panel, BorderLayout.NORTH );
    JLabel label = new JLabel( "Fixed data" );
    fixedData = new JFormattedTextField( new HexFormatter()); 
    fixedData.setToolTipText( "Enter the default fixed data for this protocol, in hex." );
    fixedData.addPropertyChangeListener( this );
    new TextPopupMenu( fixedData );
    label.setLabelFor( fixedData );
    panel.add( label, BorderLayout.WEST );
    panel.add( fixedData, BorderLayout.CENTER );
    JTextArea ta = new JTextArea();
    ta.setEditable( false );
    ta.setLineWrap( true );
    ta.setWrapStyleWord( true );
    ta.setBackground( getBackground());
    ta.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    ta.setText( "If you want the user to be able to specify the contents of the Fixed Data, you must add some device parameters." );
    add( ta, BorderLayout.CENTER );
  }

  public void commit(){;}
  public void update( ProtocolEditorNode newNode )
  {
    node = ( FixedDataEditorNode )newNode;
  }

  // PropertyChangeListener methods
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    if (source == fixedData )
    {
      if ( node != null )
        node.setFixedData(( Hex )fixedData.getValue());
    }
}

  private FixedDataEditorNode node = null;
  private JFormattedTextField fixedData = null;
}
