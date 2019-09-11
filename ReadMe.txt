JavaPOS-SPF
===========

JavaPOS-SPF is a short for JavaPOS Service Programming Framework and provides Java packages which allow relatively easy implementation of JavaPOS service objects for a wide range of POS devices.

Major advantage of using JavaPOS-SPF is that event handling becomes much easier, compared with pure JavaPOS service objects based on the framework provided by MCS (Monroe Consulting Services, http://monroecs.com/unifiedpos.htm): Simply create an event object and call handleEvent(), the rest (FreezeEvent handling, event buffering, error recovery) will be handled automatically within the framework.

Furthermore, the framework supports automatic creation of log files: If activated, each property and method usage will be logged as well as data exchange with POS devices, Simply use the methods provided by the UniqueIOProcessor class (currently restricted to TCP client communication via IPv4 and serial communication (real COM ports or virtual COM ports via USB, Bluetooth or other virtual COM ports, e.g. Com0Com).

In addition, a lot of plausibility checks will be performed by the framework itself, see the JavaDoc for the specific interfaces for details.

The framework has been developed unter Apache License, Version 2.0. For details, see
http://www.apache.org/licenses/LICENSE-2.0
