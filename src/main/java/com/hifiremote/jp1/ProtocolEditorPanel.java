package com.hifiremote.jp1;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

// TODO: Auto-generated Javadoc
/**
 * The Class ProtocolEditorPanel.
 */
public abstract class ProtocolEditorPanel
  extends JPanel
{
  
  /**
   * Instantiates a new protocol editor panel.
   * 
   * @param title the title
   */
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
  
  /**
   * Sets the text.
   * 
   * @param text the new text
   */
  public void setText( String text )
  {
    textArea.setText( text );
  }

  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle(){ return border.getTitle(); }
  
  /**
   * Sets the title.
   * 
   * @param title the new title
   */
  public void setTitle( String title ){ border.setTitle( title ); }
  
  /**
   * Update.
   * 
   * @param node the node
   */
  public abstract void update( ProtocolEditorNode node );
  
  /**
   * Commit.
   */
  public void commit(){};

  /** The border. */
  protected TitledBorder border = null;
  
  /** The text area. */
  protected JTextArea textArea = null;
}

