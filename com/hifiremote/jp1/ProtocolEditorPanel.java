package com.hifiremote.jp1;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

public abstract class ProtocolEditorPanel
  extends JPanel
{
  public ProtocolEditorPanel( String title )
  {
    super();
    border = BorderFactory.createTitledBorder( title );
    setBorder( border );
  }

  public String getTitle(){ return border.getTitle(); }
  public void setTitle( String title ){ border.setTitle( title ); }
  public abstract void update( ProtocolEditorNode node );
  public void commit(){};

  protected TitledBorder border = null;
}

