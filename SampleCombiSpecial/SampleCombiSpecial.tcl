#!/bin/sh
# In case of Unix-like OS, run it with xterm\
test "$Debug" = "" && exec wish $0 || exec xterm -e wish $0

# Copyright 2020 Martin Conrad
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

# Debug handling

set Debug 0

# Click function for Debug button: Shows / hides console window
proc debug {} {
	global Debug

	if {[set Debug [expr 1 - $Debug]]} {
		console show
	} {
		console hide
	}
}

############### Test helper start ######################################################
# To disable test helper, remove all lines from here to the ### Test helper end ### line
# Test helper can only be used if simulator has been started for TCP connections.
#

# Create and initialize client socket for testing

set HelperSocket ""
proc crcl {} {
    global HelperSocket ComPort Fd

    if {$HelperSocket != ""} {
        catch {close $HelperSocket}
    }
    set HelperSocket [socket localhost $ComPort]
    fconfigure $HelperSocket -blocking 0 -buffering none -encoding utf-8 -translation {lf lf} -eofchar {{} {}}
    puts "Use 'scl <command> to send commands to device"
}

# Flush unexpected input, send client command and return service response

set HelperData ""
proc scl {data} {
    global HelperSocket HelperData
    read $HelperSocket
    puts $HelperSocket $data
    after 100 {set HelperData [read $HelperSocket]}
    tkwait variable HelperData
    return $HelperData
}

############### Test helper end ########################################################

# Communication

set Fd ""
set SFd ""
set ComPort 45678
set Mode "9600,n,8,1"
set StartStop "Start"
set LineBuffer ""
set LastFrame ""

proc startStop timeout {
	global SFd Fd Mode ComPort StartStop LineBuffer LastFrame

	if {$Fd == ""} {
	    if {[catch {expr $ComPort + 1}] == 0 && $ComPort > 0 && $ComPort < 65535} {
	        if {$SFd != ""} {
	            close $SFd
	            set SFd ""
	            set StartStop "Start"
				puts "Server socket on port $ComPort closed"
	        } {
				if {[catch {socket -server acceptConnect $ComPort} erg] == 1} {
					puts "Cannot open socket with port $ComPort: $erg"
				} {
					set SFd $erg
					set StartStop "Stop"
					puts "Server socket opened on port $ComPort"
				}
			}
	    } {
            if {[catch {open $ComPort r+} erg] == 1} {
                puts "Cannot open $ComPort: $erg"
            } {
                set Fd $erg
                set LineBuffer ""
                fconfigure $Fd -blocking 1 -buffering none -encoding utf-8 -translation {lf lf} -mode $Mode -timeout $timeout
                fileevent $Fd readable comInputCB
                set LastFrame ""
                set StartStop "Stop"
                puts "$ComPort opened, Mode: $Mode"
            }
		}
	} {
		fileevent $Fd readable {}
		close $Fd
		set Fd ""
		set StartStop "Start"
		if {$SFd != ""} {
		    close $SFd
		    set SFd ""
		}
		puts "Port $ComPort closed"
	}
}

# For TCP only: Handling for connect

proc acceptConnect {channel addr port} {
    global Fd StartStop LineBuffer
    if {$Fd != ""} {
		after idle "checkConnect $channel $addr $port"
    } {
        puts "Connect from $addr:$port accepted"
        set LineBuffer ""
        fconfigure $channel -blocking 0 -buffering none -encoding utf-8 -translation {lf lf}
        fileevent $channel readable comInputCB
        set LastFrame ""
        set Fd $channel
        set StartStop "Stop (Connected)"
    }
}

# For TCP only: Handling for connect in case of delayed disconnect

proc checkConnect {channel addr port} {
	global Fd

	if {$Fd != ""} {
        puts "Connect from $addr:$port rejected"
        close $channel
	} {
		acceptConnect $channel $addr $port
	}
}

# Input callback

proc comInputCB {} {
	global SFd StartStop LineBuffer Fd LastFrame

    if {[eof $Fd] != 0 && $SFd != ""} {
        puts "Disconnect client socket"
        fileevent $Fd readable {}
        close $Fd
        set Fd ""
        set StartStop "Stop"
        return
    }
	if {[catch {read $Fd} c] == 1} {
		puts "Read error : $c"
	} {
	    set LineBuffer "$LineBuffer$c"
		while {$LineBuffer != ""} {
		    if {[set eolpos [string first "\n" $LineBuffer]] < 0} break
		    set cmd [string range $LineBuffer 0 [expr $eolpos - 1]]
		    if {[string range $cmd 0 1] == "sc"} {
		        # Signature Capture
		        doSCCommand [string range $cmd 2 end]
		    } elseif {[string range $cmd 0 0] == "l"} {
		        # Lights
                doLCommand [string range $cmd 1 end]
		    } elseif {[string range $cmd 0 0] == "g"} {
		        # Gate
                doGCommand [string range $cmd 1 end]
		    } elseif {[string range $cmd 0 1] == "ms"} {
		        # Motion Sensor
                doMSCommand [string range $cmd 2 end]
		    } elseif {[string range $cmd 0 1] == "id"} {
		        # Item Dispenser
                doIDCommand [string range $cmd 2 end]
            } elseif {$cmd == "?"} {
                puts "< $cmd"
                write $LastFrame
            } {
                # Common
                doCommand $cmd
            }
		    set LineBuffer [string range $LineBuffer [expr $eolpos + 1] end]
		}
	}
}

# Common commands

proc doCommand cmd {
    puts "< $cmdUnknown"
}

# Write data to socket or com port and generate debug message

proc write data {
    global Fd LastFrame

    set LastFrame $data
    if [catch {puts $Fd $data} error] {
        puts "> $data: Could not write: $error"
        return 0
    } {
        puts "> $data"
        return 1
    }
}

# Common GUI part

wm title . "Special Purpose Device"
pack [ttk::frame .spd] -expand 1 -fill both
pack [ttk::labelframe .spd.s -text Settings] -expand 1 -fill both -side left
pack [ttk::labelframe .spd.s.p -text Port] -side left -fill y -anchor w
pack [ttk::entry .spd.s.p.e -textvariable ComPort] -expand 1 -anchor w
pack [ttk::labelframe .spd.s.b -text Mode] -side left -fill y -anchor w
pack [ttk::entry .spd.s.b.e -textvariable Mode] -expand 1 -anchor w
pack [ttk::button .spd.s.x -textvariable StartStop -width 5 -command {startStop 10}] -expand 1 -side right -fill both -anchor e
if {[catch {console hide}] == 0} {
    pack [ttk::button .spd.s.d -text Debug -width 5 -command debug] -expand 1 -side right -fill both -anchor e
}

# Signature Capture

# Commands for signature capture:
# A: Abort capture, if capture active
# Bnnnnn: Retrieve signature block nnnnn
# S: Start capture, send captured coordinates when finished.
# X: Exit capture.

proc doSCCommand cmd {
    set scactive [.sc.b cget -state]
    puts "< sc$cmd"
    set ok 1
    switch -exact $cmd {
    A   {
            activate 0
            set repl "scA"
        }
    S   {
            if {$scactive == "enabled"} {
                set repl "scSInProgress"
            } {
                activate 1
                set repl "scS"
            }
        }
    X   {
            if {$scactive == "disabled" } {
                set repl "scXDisabled"
            } {
                sendSignature
                activate 0
                set ok 0
            }
        }
    default {
            if {[scan $cmd "B%d%c" blocknumber x] == 1 && [string length $cmd] == 6} {
                global Sign

                if {$blocknumber < 1 || $blocknumber > 1 + [llength $Sign] / 100} {
                    set repl "sc[set cmd]Invalid"
                } {
                    sendSignatureBlock $blocknumber
                    set ok 0
                }
            } {
                set repl "sc[set cmd]Unknown"
            }
        }
    }
    if $ok {
        write $repl
    }
}

set Sign {}

proc sendSignature {} {
    global parts Sign
    set Sign {}
    foreach part $parts {
        if {[.sc.c itemcget $part -fill] == "blue"} {
            foreach {x y} [.sc.c coords $part] {
                set x [checkrange $x 0 500]
                set y [checkrange $y 0 200]
                if {"[lrange $Sign end-1 end]" != "[list $x $y]"} {
                    lappend Sign $x $y
                }
            }
            lappend Sign -1 -1
        }
    }
    set ok [write "scX"]
}

proc checkrange {x min max} {
    if {[set x [format "%1.0f" $x]] < $min} {
        set x $min
    } elseif {$x >= $max} {
        set x [expr $max - 1]
    }
    return $x
}

proc sendSignatureBlock {block} {
    global Sign

    set repl "[format "scB%05d" $block]"
    if {[set maxindex [llength $Sign]] > [set limit [expr $block * 100]]} {
        set maxindex $limit
    }
    for {set i [expr ($block - 1) * 100]} {$i < $maxindex} {incr i} {
        set repl "$repl[format "%03d" [lindex $Sign $i]]"
    }
    return [write $repl]
}

set parts {}
set dots {}
set part {}
set pressed 0
set immediate 0

proc activate enable {
    global dots parts part pressed

    if $enable {
        bind .sc.c <ButtonPress> {
            set pressed 1
            set dots {}
            lappend parts [set part [.sc.c create line {1 1 2 2} -smooth 1 -fill white]]
        }
        bind .sc.c <ButtonRelease> {
            set pressed 0
        }
        bind .sc.c <Motion> {
            if $pressed {
                lappend dots %x %y
                if {[llength $dots] >= 4} {
                    .sc.c coords $part $dots
                    .sc.c itemconfigure $part -fill blue
                }
            }
        }
        .sc.b configure -state enabled
    } {
        bind .sc.c <ButtonPress> {}
        bind .sc.c <ButtonRelease> {}
        bind .sc.c <Motion> {}
        foreach part $parts {
            .sc.c delete $part
        }
        set parts {}
        .sc.b configure -state disabled
    }
}

# GUI part of signature capture

pack [ttk::labelframe .sc -text "Signature Capture"]
pack [canvas .sc.c -height 200 -width 500 -bg white]
pack [ttk::button .sc.b -text Ready -state disabled -command {
    sendSignature
    activate 0
}]

# Commands for Lights:
# Sxc, where x = 1 ... 5 the light number and c = 0 ... 7 the color value: 0: black, 1: red, 2: green, 3: yellow, 4: blue, 5: magenta, 6: cyan, 7: white

proc doLCommand cmd {
    puts "< l$cmd"
    if {[string range $cmd 0 0] == "S" && [string length $cmd] == 3} {
        if {[scan "[string range $cmd 1 1] [string range $cmd 2 2]" "%d %d" ln color] == 2} {
            if {$color < 0 || $color > 7} {
                write "l[set cmd]BadColor"
            } {
                if [catch {.li.l$ln configure -background [lindex {black red green yellow blue magenta cyan white} $color]}] {
                    write "l[set cmd]BadLight"
                } {
                    write "l$cmd"
                }
            }
        } {
            write "l[set cmd]Invalid"
        }
    } {
        write "l[set cmd]Unknown"
    }
}

# GUI part of lights

pack [ttk::labelframe .li -text Lights] -expand 1 -fill both
for {set i 1} {$i < 6} {incr i} {
    pack [ttk::label .li.l$i -width 2 -text " " -background black] -expand 1 -side left
}

proc setLight {index color} {
    catch {.li.l$index configure -background $color}
}

# Commands for Gate:
# O Open the gate (gate closes automatically if not blocked)
# S Get gate state: Returns gSO, gSC, gSo or gSc for open, closed, open blocked or closed blocked

proc doGCommand cmd {
    global GateCloser GateState GateBlocked
    puts "< g$cmd"
    if {$cmd == "O"} {
        if $GateBlocked {
            if $GateState {
                write "gO"
            } {
                write "gOBlocked"
            }
        } {
            if {$GateCloser != ""} {
                after cancel $GateCloser
            } {
                set GateState 1
            }
            activateGateCloser
            write "gO"
        }
    } elseif {$cmd == "S"} {
        sendGateState
    } {
        write "g[set cmd]Unknown"
    }
}

proc sendGateState {} {
    global GateState GateBlocked
    write "gS[string range [lindex {"Cc" "Oo"} $GateState] $GateBlocked $GateBlocked]"
}

# Handler for blocking

proc blockingHandler {} {
    global GateCloser GateBlocked GateState

    if $GateBlocked {
        if {$GateCloser != ""} {
            after cancel $GateCloser
            set GateCloser ""
        }
    } {
        if $GateState {
            activateGateCloser
        }
    }
    sendGateState
}

proc activateGateCloser {} {
    global GateState GateCloser
    set GateCloser [after 5000 {
        set GateState 0
        set GateCloser ""
        write "gSC"
    }]
}

# GUI part of Gate

set GateState 0
set GateBlocked 0
set GateCloser ""

pack [ttk::labelframe .gt -text Gate] -expand 1 -fill both
pack [ttk::radiobutton .gt.c -text Closed -variable GateState -value 0 -state disabled] -expand 1 -side left
pack [ttk::radiobutton .gt.o -text Opened -variable GateState -value 1 -state disabled] -expand 1 -side left
pack [ttk::checkbutton .gt.b -text Blocked -variable GateBlocked -command blockingHandler] -expand 1 -side left

# Commands for motion sensor:
# S Get gate state: Returns msS or mss for in motion (S) or not in motion (s). Will be sent on status change.

proc doMSCommand cmd {
    global MSState

    puts "< ms$cmd"
    if {$cmd != "S"} {
        write "ms[set cmd]Unknown"
    } {
        msStateHandler
    }
}

proc msStateHandler {} {
    global MSState

    write "ms[string range {sS} $MSState $MSState]"
}

# GUI part of motion sensor

set MSState 0

pack [ttk::labelframe .ms -text "Motion Sensor"] -expand 1 -fill both
pack [ttk::checkbutton .ms.im -text "Someone or something is in motion" -variable MSState -command msStateHandler] -side left

# Commands for item dispenser:
# Axnn Add nn to item count of slot x(adjust). x is the slot number - 1 (0 <= x < 9).
# Dxnn Dispense nn items from slot x.
# R   Retrieve item count for all slots. Response: idRn0n1n2n3n4n5n6n7n8n9, where nx is the current item count of slot x + 1.

proc doIDCommand cmd {
    puts "< id$cmd"
    set cnt [scan "[string range $cmd 1 1] [string range $cmd 2 3]" "%d %d" slot count]
    set cmdstruct "[string range $cmd 0 0]$cnt[string length $cmd]"
    if {$cmdstruct == "A24"} {
        set var "IDs[expr $slot + 1]"
        global $var
        set newval [expr [set $var] + $count]
        if {$newval >= 0 && $newval <= 20} {
            set $var $newval
            write "id[set cmd]"
        } {
            write "id[set cmd]Invalid"
        }
    } elseif {$cmdstruct == "D24"} {
        set var "IDs[expr $slot + 1]"
        global $var
        set newval [expr [set $var] - $count]
        if {$newval >= 0 && $newval <= 20} {
            set $var $newval
            write "id[set cmd]"
        } {
            write "id[set cmd]Invalid"
        }
    } elseif {$cmd == "R"} {
        set resp "id$cmd"
        for {set slot 0} {$slot < 10} {incr slot} {
            set var "IDs[expr $slot + 1]"
            global $var
            set resp "$resp[format {%02d} [set $var]]"
        }
        write $resp
    } {
        write "id[set cmd]Unknown"
    }
}

# GUI part of item dispenser

pack [ttk::labelframe .id -text "Item Dispenser"] -expand 1 -fill both
for {set i 1} {$i <= 10} {incr i} {
    set IDs$i 20
    pack [ttk::labelframe .id.s$i -text $i] -side left -expand 1
    pack [scale .id.s$i.sc -orient vertical -variable IDs$i -from 20 -to 0]
}