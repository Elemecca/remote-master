package com.hifiremote.jp1;

public class IndirectDefaultValue
 extends DefaultValue
{
  public IndirectDefaultValue( int index, Parameter ref )
  {
    this.index = index;
    this.ref = ref;
  }
  public Object value()
  {
     return ref.getValueOrDefault();
  }

  private Parameter ref;
  private int index = 0;

  public String toString(){ return "[" + index + "]"; }
}