package com.hifiremote.jp1;

import java.util.Comparator;

public class DependentUpgradeComparator implements Comparator< DeviceUpgrade >
{
/*
 *   (non-Javadoc)
 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
 */
  
/*
 * This comparator follows that used in IR.exe to sort upgrades in a device dependent
 * upgrade section into an order that provides efficient use of available space, i.e.
 * so that upgrades which have the same PID can share the same protocol code.  The
 * sort order is decreasing order of PID, increasing order of size.  Since the
 * upgrade area will be filled from top down, this puts the smallest upgrades in the
 * highest memory position.  The explanation in the IR.exe source code is as follows:
 * 
 * This will cluster all devices using the same protocol together, allowing the same
 * protocol code to be pointed to by the maximum number of devices.  Note that putting
 * the smallest devices in the highest memory region will generally provide the optimal
 * size, but not always.  For example, with upgrade sizes 10, 7F, 7F, E0, E0, the first
 * 2 could use the same protocol, but then each of the following 3 will require their
 * own.  If the ordering was 7F, 7F, 10, E0, E0, then the first 2 could share a protocol,
 * the next 2 could share, and the last would need its own.  Thus this could be optimized
 * further, but the reality is that the device upgrades are small, and it is considered
 * quite rare that a more comprehensive optimization routine would provide better results.
 */
  
  public int compare( DeviceUpgrade upg1, DeviceUpgrade upg2 )
  {
    // sort first in *decreasing* order of PID.
    int result = upg2.getProtocol().getID().get(0) - upg1.getProtocol().getID().get(0);
    if ( result == 0 )
    {
      // if same protocol, sort in *increasing* order of size
      result = upg1.getUpgradeLength() - upg2.getUpgradeLength();
    }
    // Note that the Java sorting algorithm is stable, so that if this returns 0 then
    // the original order is preserved.

    return result;    
  }
}
