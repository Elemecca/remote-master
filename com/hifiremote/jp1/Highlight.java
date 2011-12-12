package com.hifiremote.jp1;

import java.awt.Color;

public class Highlight
{
  private int memoryUsage = 0;
  
  private Color highlight = Color.WHITE;
  
  private Segment segment = null;
  
  private int index = 0;
  
  public Color getHighlight()
  {
    return highlight;
  }
  
  public void setHighlight( Color highlight )
  {
    this.highlight = highlight;
  }
  
  public int getMemoryUsage()
  {
    return memoryUsage;
  }

  public void clearMemoryUsage()
  {
    memoryUsage = 0;
  }
  
  public void setMemoryUsage( int memoryUsage )
  {
    this.memoryUsage = memoryUsage;
  }
  
  public void addMemoryUsage( int memoryUsage )
  {
    this.memoryUsage += memoryUsage;
  }

  public Segment getSegment()
  {
    return segment;
  }

  public void setSegment( Segment segment )
  {
    this.segment = segment;
  }
  
  public void setSegment( Segment segment, int index )
  {
    this.segment = segment;
    this.index = index;
  }

  public int getIndex()
  {
    return index;
  }
  
}
