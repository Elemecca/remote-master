package com.hifiremote.jp1;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;

public class JP1Table
  extends JTableX
{
  public JP1Table( JP1TableModel model )
  {
    super( model );
    setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    //  getSelectionModel().addListSelectionListener( this );
    //  setCellSelectionEnabled( true );
    setSurrendersFocusOnKeystroke( true );
    setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
    tableHeader.setReorderingAllowed( false );
    DefaultCellEditor e = ( DefaultCellEditor )getDefaultEditor( String.class );
    new TextPopupMenu(( JTextComponent )e.getComponent());

    initColumns();
  }
    
  protected void initColumns()
  {
    JP1TableModel model = ( JP1TableModel )dataModel;

    TableColumnModel columnModel = getColumnModel();
    TableColumn column;

    int cols = columnModel.getColumnCount();
    for ( int i = 0; i < cols; i++ )
    {
      boolean isFixed = model.isColumnWidthFixed( i );
      setColumnWidth( i, 
                      model.getColumnPrototypeName( i ),
                      model.isColumnWidthFixed( i ),
                      0 );
      column = columnModel.getColumn( i );

      TableCellEditor editor = model.getColumnEditor( i );
      if ( editor != null )
        column.setCellEditor( editor );

      TableCellRenderer renderer = model.getColumnRenderer( i );
      if ( renderer != null )
        column.setCellRenderer( renderer );
      
      if ( model.getColumnName( i ).startsWith( "<html>" ))
      {
        column.setHeaderRenderer( tableHeader.getDefaultRenderer());
      }
    }
    doLayout();
  }

  public void setFont( Font aFont )
  {
    super.setFont( aFont );
    if ( aFont == null )
      return;
    setRowHeight( aFont.getSize() + 2 );
    // if ( model != null )
    initColumns();
  }
}
