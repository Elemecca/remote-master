package com.hifiremote.jp1;

import java.beans.*;
import java.io.PrintWriter;
import javax.swing.tree.DefaultMutableTreeNode;

// TODO: Auto-generated Javadoc
/**
 * The Class ProtocolEditorNode.
 */
public abstract class ProtocolEditorNode
  extends DefaultMutableTreeNode
{
  
  /**
   * Instantiates a new protocol editor node.
   * 
   * @param name the name
   * @param askAllowsChildren the ask allows children
   */
  public ProtocolEditorNode( String name, boolean askAllowsChildren )
  {
    super( name, askAllowsChildren );
    propertyChangeSupport = new PropertyChangeSupport( this );
  }

  /**
   * Sets the name.
   * 
   * @param newName the new name
   */
  public void setName( String newName )
  {
    propertyChangeSupport.firePropertyChange( "Name", super.getUserObject(), newName );
    super.setUserObject( newName );
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName()
  {
    return ( String )getUserObject();
  }

  /**
   * Creates the child.
   * 
   * @return the protocol editor node
   */
  public ProtocolEditorNode createChild()
  {
    return null;
  }

  /**
   * Can add children.
   * 
   * @return true, if successful
   */
  public boolean canAddChildren(){ return getAllowsChildren(); }
  
  /**
   * Can delete.
   * 
   * @return true, if successful
   */
  public boolean canDelete(){ return false; }

  /**
   * Gets the editing panel.
   * 
   * @return the editing panel
   */
  public abstract ProtocolEditorPanel getEditingPanel();
  
  /**
   * Prints the.
   * 
   * @param pw the pw
   */
  public abstract void print( PrintWriter pw );

  /**
   * Adds the property change listener.
   * 
   * @param propertyName the property name
   * @param listener the listener
   */
  public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
  {
    propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
  }

  /**
   * Removes the property change listener.
   * 
   * @param propertyName the property name
   * @param listener the listener
   */
  public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
  { 
   propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
  }

  /**
   * Fire property change.
   * 
   * @param propertyName the property name
   * @param oldValue the old value
   * @param newValue the new value
   */
  public void firePropertyChange( String propertyName, Object oldValue, Object newValue )
  {
    propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
  }        

  /** The property change support. */
  private PropertyChangeSupport propertyChangeSupport = null;
}

