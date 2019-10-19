What Is It
==========
The sample combi device simulator is a sample implementation for some kind of operator module, considting of a line
display (2two lines, each 20 characters), an operator lock, an electronic key lock, a cash drawer, a scanner, a
magnetic stripe reader and a POS keyboard.

How To Connect The Simulator To The POS Application
===================================================
The simulator exchanges data with a POS application or with the JavaPOS service via one single COM port or via a TCP
connection.

If a COM port will be used for data exchange, the mode (speed, parity...) must be specified in a format accepted by
the Tcl/Tk interpreter. The COM port can be a real COM port or a virtual COM port as provided (for example) by the
com0com driver for Windows.

If a TCP port will be used, the mode value will not be used. The simulator provides a TCP server (IPv4) with the
specified port (the port must be an unused port, numerical, between 1 and 65535). Only one connection at atime is
possible. Further connections will be rejected.

The communication protocol is the same for COM and TCP connections.

What Must Be Configured In The Property File
============================================
The UPOS specification advises the service developer to provide a way to map keyboard keys to application specific
key values. The property file must have the name <packet name>.<class name>.properties
(SampleCombiDevice.Device.properties) and it must contain one line per enabled key.

The keyboard has 10x16 keys. If the key in row 7, column 12 shall generate a data event with kea value 1234,
the property file must contain the following line:

Key07-12Value = 1234

(Row in the range 01 - 10, column in range 01 - 16)

Where Must Be The Property File
===============================
The property file must be in the working directory of the application.

