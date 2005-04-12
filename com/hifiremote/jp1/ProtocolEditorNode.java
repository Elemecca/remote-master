package com.hifiremote.jp1;

import java.beans.*;
import java.io.PrintWriter;
import javax.swing.tree.DefaultMutableTreeNode;

public abstract class ProtocolEditorNode
  extends DefaultMutableTreeNode
{
  public ProtocolEditorNode( String name, boolean askAllowsChildren )
  {
    super( name, askAllowsChildren );
    propertyChangeSupport = new PropertyChangeSupport( this );
  }

  public void setName( String newName )
  {
    propertyChangeSupport.firePropertyChange( "Name", super.getUserObject(), newName );
    super.setUserObject( newName );
  }

  public String getName()
  {
    return ( String )getUserObject();
  }

  public ProtocolEditorNode createChild()
  {
    return null;
  }

  public boolean canAddChildren(){ return getAllowsChildren(); }
  public boolean canDelete(){ return false; }

  public abstract ProtocolEditorPanel getEditingPanel();
  public abstract void print( PrintWriter pw );

  public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
  {
    propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
  }

  public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
  { 
   propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
  }

  public void firePropertyChange( String propertyName, Object oldValue, Object newValue )
  {
    propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
  }        

  private PropertyChangeSupport propertyChangeSupport = null;
}

