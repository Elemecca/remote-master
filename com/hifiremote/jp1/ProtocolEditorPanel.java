package com.hifiremote.jp1;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

public abstract class ProtocolEditorPanel
  extends JPanel
{
  public ProtocolEditorPanel( String title )
  {
    super( new BorderLayout());
    border = BorderFactory.createTitledBorder( title );
    setBorder( border );
    textArea = new JTextArea();
    textArea.setFont( border.getTitleFont());
    textArea.setEditable( false );
    textArea.setLineWrap( true );
    textArea.setWrapStyleWord( true );
    textArea.setBackground( getBackground());
    textArea.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    add( textArea, BorderLayout.NORTH );
  }
  
  public void setText( String text )
  {
    textArea.setText( text );
  }

  public String getTitle(){ return border.getTitle(); }
  public void setTitle( String title ){ border.setTitle( title ); }
  public abstract void update( ProtocolEditorNode node );
  public void commit(){};

  protected TitledBorder border = null;
  protected JTextArea textArea = null;
}

