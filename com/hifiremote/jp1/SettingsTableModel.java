package com.hifiremote.jp1;

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class SettingsTableModel.
 */
public class SettingsTableModel extends JP1TableModel< Setting > implements CellEditorModel
{

  /**
   * Instantiates a new settings table model.
   */
  public SettingsTableModel()
  {
    comboEditor.setClickCountToStart( RMConstants.ClickCountToStart );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    if ( remoteConfig != null )
    {
      colorEditor = new RMColorEditor( remoteConfig.getOwner() );
      setData( remoteConfig.getRemote().getSettings() );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    int count = colNames.length - 1;
    if ( remoteConfig != null && remoteConfig.allowHighlighting() )
    {
      ++count;
    }
    return count;
  }

  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "Setting", "Value", "<html>Size &amp<br>Color</html>"
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  /** The Constant colPrototypeNames. */
  private static final String[] colPrototypeNames =
  {
      " 00 ", "A Setting Name", "A Value", "Color_"
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, Setting.class, Color.class
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable( int row, int col )
  {
    return ( col == 3 ) || ( col == 2 && row < remoteConfig.getRemote().getStartReadOnlySettings() - 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int row, int column )
  {
    Remote r = remoteConfig.getRemote();
    Setting setting = r.getSettings()[ row ];
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return setting.getTitle();
      case 2:
      {
        int val = setting.getValue();
        Object[] choices = setting.getOptions( r );
        if ( choices == null )
        {
          return new Integer( val );
        }

        if ( val > choices.length )
        {
          return null;
        }

        return choices[ val ];
      }
      case 3:
        return setting.getHighlight();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    Remote r = remoteConfig.getRemote();
    Setting setting = r.getSettings()[ row ];
    if ( col == 2 )
    {
      Object[] choices = setting.getOptions( r );
      if ( choices == null )
      {
        Integer oldValue = setting.getValue();
        setting.setValue( ( ( Integer )value ).intValue() );
        propertyChangeSupport.firePropertyChange( "value", oldValue, value );
      }
      else
      {
        for ( int i = 0; i < choices.length; ++i )
        {
          Object oldValue = choices[ setting.getValue() ];
          if ( choices[ i ].equals( value ) )
          {
            setting.setValue( i );
            propertyChangeSupport.firePropertyChange( "value", oldValue, value );
          }
        }
      }
    }
    else if ( col == 3 )
    {
      setting.setHighlight( ( Color )value );
      propertyChangeSupport.firePropertyChange( "highlight", null, null );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    else if ( col == 3 )
    {
      return colorRenderer;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.CellEditorModel#getCellEditor(int, int)
   */
  public TableCellEditor getCellEditor( int row, int col )
  {
    if ( col == 2 )
    {
      Remote r = remoteConfig.getRemote();
      Setting setting = remoteConfig.getRemote().getSettings()[ row ];
      Object[] options = setting.getOptions( r );
      if ( options == null )
      {
        int bits = setting.getNumberOfBits();
        intEditor.setMin( 0 );
        intEditor.setMax( ( 1 << bits ) - 1 );
        return intEditor;
      }

      JComboBox cb = ( JComboBox )comboEditor.getComponent();
      cb.setModel( new DefaultComboBoxModel( options ) );
      return comboEditor;
    }
    else if ( col == 3 )
    {
      return colorEditor;
    }
    return null;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;

  /** The int editor. */
  private BoundedIntegerEditor intEditor = new BoundedIntegerEditor();

  /** The combo editor. */
  private DefaultCellEditor comboEditor = new DefaultCellEditor( new JComboBox() );
  private RMColorEditor colorEditor = null;
  private RMColorRenderer colorRenderer = new RMColorRenderer();
}
