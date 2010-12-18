package com.hifiremote.jp1;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class ChoiceArea extends JTextArea implements PropertyChangeListener
{
  private JFileChooser chooser = null;
  
  public ChoiceArea( JFileChooser chooser )
  {
    super();
    setLineWrap( true );
    setEditable( false );
    setOpaque( false );
    setFont( UIManager.getFont( "Tree.font" ) );
    setFocusable( false );
    this.chooser = chooser;
    chooser.addPropertyChangeListener( this );
  }
  
  @Override
  public void propertyChange( PropertyChangeEvent e )
  {
    if ( e.getNewValue() instanceof File )
    {
      File file = chooser.getSelectedFile();
      if ( file != null )
      {
        setText( file.getAbsolutePath() );
      }
    }
  }    
}
