#!/bin/sh
# In case of Unix-like OS, run it with xterm\
test "$Debug" = "" && exec wish $0 || exec xterm -e wish $0

# Copyright 2019 Martin Conrad
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Coin Dispenser Simulator:
# Has one slot for 1, 5, 10, 50 and 100 cent and two slots for 2, 20 and 200 cent, This allows correct payment for every
# amount up to 5 euro. Therefore we have 11 slots ordered by coin size; 1, 2, 2, 10, 5, 20, 20, 100, 50, 200, 200.
# A description of all frames can be found near the end of this script (function reader).
# We store the amount of coins in the slots in array slots, Initially, every slot has 10 coins:
set slot(1)    10
set slot(2a)   10
set slot(2b)   10
set slot(5)    10
set slot(10)   10
set slot(20a)  10
set slot(20b)  10
set slot(50)   10
set slot(100)  10
set slot(200a) 10
set slot(200b) 10
# The communication port. This sample supports only TCP ports.
set port 56789
# The TCP server handle
set SFd ""
# The TCP client handle
set Fd ""
# The jam flag. If set, dispenser is blocked for cash back.
set Jam 0
# The debug flag
set Debug 0
# The GUI: Entry for port, start/stop button, check box for jam, entry for each slot
pack [ttk::labelframe .c -text "Configuration"] -expand 1 -fill both
pack [ttk::labelframe .c.p -text "Port"] -fill both -side left
pack [ttk::entry .c.p.e -textvariable port -width 5]
pack [ttk::frame .c.j] -expand 1 -fill both -side left
pack [ttk::checkbutton .c.j.c -text "Jam" -variable Jam -onvalue 1 -offvalue 0] -anchor s
if {[catch {console hide}] == 0} {
    pack [ttk::button .c.d -text "Debug" -width 6 -command debug] -fill y -side left
}
pack [ttk::button .c.s -text "Start" -width 5 -command startstop] -fill y -side right
pack [ttk::labelframe .s -text "Slots"] -fill both
pack [ttk::labelframe .s.s1 -text "0,01"] -fill both -side left
pack [ttk::entry .s.s1.e -textvariable slot(1) -width 3]
pack [ttk::labelframe .s.s2a -text "0,02"] -fill both -side left
pack [ttk::entry .s.s2a.e -textvariable slot(2a) -width 3]
pack [ttk::labelframe .s.s2b -text "0,02"] -fill both -side left
pack [ttk::entry .s.s2b.e -textvariable slot(2b) -width 3]
pack [ttk::labelframe .s.s10 -text "0,10"] -fill both -side left
pack [ttk::entry .s.s10.e -textvariable slot(10) -width 3]
pack [ttk::labelframe .s.s5 -text "0,05"] -fill both -side left
pack [ttk::entry .s.s5.e -textvariable slot(5) -width 3]
pack [ttk::labelframe .s.s20a -text "0,20"] -fill both -side left
pack [ttk::entry .s.s20a.e -textvariable slot(20a) -width 3]
pack [ttk::labelframe .s.s20b -text "0,20"] -fill both -side left
pack [ttk::entry .s.s20b.e -textvariable slot(20b) -width 3]
pack [ttk::labelframe .s.s100 -text "1,00"] -fill both -side left
pack [ttk::entry .s.s100.e -textvariable slot(100) -width 3]
pack [ttk::labelframe .s.s50 -text "0,50"] -fill both -side left
pack [ttk::entry .s.s50.e -textvariable slot(50) -width 3]
pack [ttk::labelframe .s.s200a -text "2,00"] -fill both -side left
pack [ttk::entry .s.s200a.e -textvariable slot(200a) -width 3]
pack [ttk::labelframe .s.s200b -text "2,00"] -fill both -side left
pack [ttk::entry .s.s200b.e -textvariable slot(200b) -width 3]

# Debug: Hides od shows the console window, depending on Debug
proc debug {} {
    global Debug

    if {$Debug} {
        console hide
        set Debug 0
    } {
        console show
        set Debug 1
    }
}

# server: Tcp service command, stores the handle, produces connect message, configures tsp stream
proc server {fd ip port} {
    global Fd

    if {$Fd != $fd} {
        catch {close $Fd}
        set Fd $fd
    }
    puts "Connect from $ip:$port"
    fconfigure $fd -buffering line -encoding binary -translation binary
    fileevent $fd readable "reader $fd"
}

# reader: Command reader, reads and interprets command and sends response. Valid commands are
#   R\n: Read coin count for all slots
#   A a b c d e f g h i j k\n: Add values a - k to the corresponding slots, a to slot(1) ... k to slot(200b)
#   O a b c d e f g h i j k\n: Subtract values a - k from the corresponding slots, only allowed values are 0 and 1
# All commands return OK a b c d e f g h i j k\n if no error occurred, KO\n otherwise. a - k are the current slot values.
proc reader {fd} {
    global slot Jam Fd

    set slotindices {{} 1 2a 2b 10 5 20a 20b 100 50 200a 200b}
    set OK 0
    if {[catch {gets $fd command}] || [eof $fd]} {
        # Channel closed by server or broken
        puts "Channel closed by client"
        catch {close $fd}
        set Fd ""
    } {
        # Got command
        puts "<< $command"
        if {$command == "R" && $Jam == 0} {
            # Read coin count: Command complete and valid
            set OK 1
        } elseif {[llength $command] == 12 && [lindex $command 0] == "A" && $Jam == 0} {
            # Adjust coin slot command: Add the component values to the current slot values after validation check
            set OK 1
            for {set i 1} {$i <= 11} {incr i} {
                if {[catch {expr $slot([lindex $slotindices $i]) + [lindex $command $i]} j] || $j < 0} {
                    # Component must be numeric and new coin count must be positive
                    set OK 0
                    break
                }
            }
            if {$OK} {
                # All components checked and valid: Make the adjustment
                for {set i 1} {$i <= 11} {incr i} {
                    incr slot([lindex $slotindices $i]) [lindex $command $i]
                }
            }
        } elseif {[llength $command] == 12 && [lindex $command 0] == "O" && $Jam == 0} {
            # Open marked slot. Slots to be opened must be specified by 1, slots that shall remain closed by 0
            set OK 1
            for {set i 1} {$i <= 11} {incr i} {
                if {[catch {expr $slot([lindex $slotindices $i]) - [lindex $command $i]} j] || $j < 0 || ([lindex $command $i] & ~1) != 0} {
                    #Component must be numeric, 0 or 1 and new coin count must be positive
                    set OK 0
                    break
                }
            }
            if {$OK} {
                # All components checked and valid: Decrement coin coint of opend slots by one
                for {set i 1} {$i <= 11} {incr i} {
                    incr slot([lindex $slotindices $i]) [expr 0 - [lindex $command $i]]
                }
            }
        }
        # Response handling is the same for all commands
        if {$OK} {
            # Generate positive response containing the new slot count
            set resp "OK"
            for {set i 1} {$i <= 11} {incr i} {
                lappend resp $slot([lindex $$slotindices $i])
            }
        } {
            # Generate negative response
            set resp "KO"
        }
        if {[catch {puts $fd $resp}] == 0} {
            puts ">> $resp"
        }
    }
}

# Starts or stops the server, depending on start/stop button text
proc startstop {} {
    global SFd Fd port

    if {[.c.s cget -text] == "Start"} {
        if {[catch {socket -server server $port} fd] == 0} {
            .c.s configure -text "Stop"
            set SFd $fd
            puts "Server started on port $port"
        }
    } {
        .c.s configure -text "Start"
        catch {close $SFd}
        set SFd ""
        catch {close $Fd}
        set $Fd ""
        puts "Server stopped"
    }
}