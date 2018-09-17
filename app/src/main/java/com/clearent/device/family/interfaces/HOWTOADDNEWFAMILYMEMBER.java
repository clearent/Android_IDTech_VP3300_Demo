package com.clearent.device.family.interfaces;


/**
 *
 * The goal of all this is to point the developer to the DeviceFactory and have them pick which device they need to support.
 * The factory will create the objects by supporting all known constructors.
 *
 * I first started with VP3300. I 'optimistically' inherited its interface as a IDTDevice interface
 *
 * I was hoping to flush out the common implementations across the entire family. But, I think this will be a
 * work in progress. Maybe after we do the steps I have listed below we can see what's duplicated.
 *
 * The goal is to use the IDTDevice interface for our own benefit and still make the developer
 * use specific device interface/Impl they get from the DeviceFactory.
 *
 *
 * 1 - Identify the family member. Ex Augusta
 * 2 - Find the IDT_XXXX class. Ex IDT_Augusta
 * 3 - Copy the decompiled code into a temp package, like interfaces.
 * 4 - Extract out an interface representing the class.
 * 5 - Create a new class similar to VP3300Impl and implement the new interface.
 * 6 - use the IDE to implement any methods that are not in IDTDevice.
 * 7 - change the implementation of these methods to reference the getSDKInstance().
 *
 *
 *
 * I went ahead and put augusta into its own package but they could probably just be all dropped into a device package. (so family.device)
 *
 *
 */
public class HOWTOADDNEWFAMILYMEMBER {



}
