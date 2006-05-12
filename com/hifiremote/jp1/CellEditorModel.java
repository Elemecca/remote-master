package com.hifiremote.jp1;

import javax.swing.table.*;
import java.util.*;

public interface CellEditorModel
{
  public TableCellEditor getCellEditor( int row, int col  );
}
