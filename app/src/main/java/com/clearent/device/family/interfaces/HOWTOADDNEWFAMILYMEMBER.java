package com.clearent.device.family.interfaces;


/**
 *
 * Here are some goals driving the DeviceFactory solution:
 *
 * 1 - We can wrap all known methods and provide Swagger documentation.
 * 2 - We can insert code when necessary.
 * 3 - We can still expose the IDT_Device object just like IDTech does.
 *
 * So, point the developer to the DeviceFactory and have them pick which device they need to support.
 * The factory will create the objects by supporting all known constructors.
 *
 * The Device class is abstract and implements all known methods of the IDTDevice interface (which represents IDT_Device).
 * It requires its implementor to provide an IDT_Device.
 *
 * I first started with VP3300. I used the interfaces package as a work area. First I copied the code of IDT_VP3300 into a class.
 * I then extracted the interface.
 *
 * The goal is to use the IDTDevice interface for our own benefit but still make the developer
 * use specific device interface/Impl they get from the DeviceFactory. This doesn't stop them from looking at the object with the
 * IDTDevice interface, just the factory doesn't have to return it.
 *
 *
 * Here's how I did the second device.
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
 * I went ahead and put augusta into its own package but they could probably just be all dropped into a device package. (so family.device)
 *
 */
public class HOWTOADDNEWFAMILYMEMBER {



}
