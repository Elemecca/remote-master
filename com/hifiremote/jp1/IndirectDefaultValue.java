package com.hifiremote.jp1;

public class IndirectDefaultValue
 extends DefaultValue
{
  public IndirectDefaultValue( Parameter ref )
  {
     this.ref = ref;
  }
  public Object value()
  {
     return ref.getValueOrDefault();
  }

  private Parameter ref;
}