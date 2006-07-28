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
  public JP1Table( TableModel model )
  {
    super( model );
    setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    //  getSelectionModel().addListSelectionListener( this );
    setCellSelectionEnabled( true );
    setSurrendersFocusOnKeystroke( true );
    setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
    tableHeader.setReorderingAllowed( false );
    DefaultCellEditor e = ( DefaultCellEditor )getDefaultEditor( String.class );
    new TextPopupMenu(( JTextComponent )e.getComponent());
  }

  public void initColumns( JP1TableModel model )
  {
    TableColumnModel columnModel = getColumnModel();
    TableColumn column;

    int cols = columnModel.getColumnCount();
    for ( int i = 0; i < cols; i++ )
    {
      boolean isFixed = model.isColumnWidthFixed( i );
      setColumnWidth( i, model.getColumnPrototypeName( i ), model.isColumnWidthFixed( i ), 0 );
      column = columnModel.getColumn( i );

      TableCellEditor editor = model.getColumnEditor( i );
      if ( editor != null )
        column.setCellEditor( editor );

      TableCellRenderer renderer = model.getColumnRenderer( i );
      if ( renderer != null )
        column.setCellRenderer( renderer );
      
      if ( model.getColumnName( i ).startsWith( "<html>" ))
      {
        column.setHeaderRenderer( getTableHeader().getDefaultRenderer());
      }
    }
    doLayout();
  }
}
