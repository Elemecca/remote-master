package com.hifiremote.jp1;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class KMTableModel.
 */
public abstract class KMTableModel< E >
  extends JP1TableModel< E >
{
  
  /**
   * Instantiates a new kM table model.
   */
  public KMTableModel()
  {
    super();
  }

  /**
   * Instantiates a new kM table model.
   * 
   * @param data the data
   */
  public KMTableModel( List< E > data )
  {
    super();
    setData( data );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  public boolean isColumnWidthFixed( int col )
  {
    int lastCol = getColumnCount() - 1;
    if (( col == 1 ) || ( col == lastCol ))
      return false;
    return true;
  }
}
