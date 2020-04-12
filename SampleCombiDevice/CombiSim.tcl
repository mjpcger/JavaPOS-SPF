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

#
 # Combined device simulator, can be used if no real combined device is avaliable.
 # In addition, you need either a null-modem cable and two COM ports or
 # a virtual COM port driver like com0com from
 #      http://com0com.sourceforge.net/
 # to allow real RS232 behavior, allow buffer overrun on both
 # sides of the COM port pair.
 # If no COM port is available, a TCP server socket port can be specified. In this case,
 # socket communication can be made instead.
#

# Commands and responses use UTF-8 encoding.
#  Drawer:
#	DO							Open drawer command
#	Dd							Drawer response. d specifies the current drawer state (C for Closed, O for Opened).
#  Lock:
#	Ll							Lock changed to position l, where l is one of the following characters: 0, 1, 2, X, Z, P or T. 
#  Electronic key:
#	Exxxxxxxxxxxx				Electronic key value changed to xxxxxxxxxxxx, where xxxxxxxxxxxx will be replaced by the hexadecimal
#								value of the electronic key, in Anker mode starting with least significant byte. This means, if the
#								key value is 0x123456789ABC, in Anker mode EBC9A78563412 will be sent, otherwise E123456789ABC. A value
#								of 000000000000 means no key is present.
#  Keyboard key:
#	Brc							Button in row r and column c has been pressed. r is one character between 0 and 9 (0 = row 1), c is
#								one character between A and M (A = column 1, ... M = column 13)
#  Magnetic stripe reader:
#	Mnnn1xx...x2yy...y3zz...z	Magnetic card has been read. nnn specified the number of characters following the thre-byte length field.
#								Tracks 1 - 3 follow, each starting with the track number (1 - 3), the start sentinel (& or ;), the track
#								data and the end sentinel (?). For unread or empty tracks, only start and end sentinel will be transmitted.
#  Scanner:
#	RE							Enable scanner command
#	RD							Disable scanner command
#	Rtdd...d					Label scanned with type specified by t, with data specified by dd...d. Supported types are A (UPC-A),
#								E (UPC-E), F (EAN-13) and FF (EAN-8).
#  Display
#   Ce							Code page to be used for text output. Valid envoding values for e are:
#									0	UTF-8 (the default),
#									1	ASCII
#									2	Code page 437
#									3	Code page 1252
#   Cx							Response. x=0: No change, x=1: changed as requested.
#	Tlnndd...d[aa...a]			Display text command. l specifies the line number (0 or 1), nn the number of characters that follow. Due
#								to UTF-8 encoding, nn is less than the number of bytes that follow if the text contains non-ascii characters.
#								dd...d specify max. 20 characters that shall be displayed. All further characters [aa...a] are optional,
#								they specify attributes for the most-left characters. The remaining characters will be displayed with
#								default attribute (normal). The following attributes are supported:
#									n	normal output,
#									r	reverse video output,
#									b	blinking output,
#									a	reverse blinking output.
#								For example, T025HELLO EARTH AND SUN!abrbn  will result in displaying "HELLO EARTH AND SUN!", where the
#								H of HELLO will be reverse and blinking, E and 2nd L of HELLO are blinking, the 1st L of HELLO is reverse
#								and the remainder will be displayed normally.
#  Beeper
#	B1							Start beeping command
#	B0							Stop beeping command
#  Device status
#	SR							Status request command
#	Sdlxxxxxxxxxxxx				Status response, d specifies the drawer state, l the lock state  xxxxxxxxxxxx will be replaced by the
#								current electronic key value. The values that replace d, l and xxxxxxxxxxxx are the same as described for
#								the corresponding device above.
set OpenSeq "DO";           # Drawer Open command
set DrawerValues {C O};     # List of possible drawer status characters for open and closed
set DrawerMask "D%s";	    # %s will be replaced by drawer value
set LockValues {0 1 2 X Z P T};  # List of possible lock positions
set LockMask "L%s";	        # %swill be replaced by the corresponding lock position
set KeyMask "E%s";	        # Specify format of Key status changed messages
set AnkerKey 1;		        # Specify byte-wise order of key value is right to left
set ButtonMask "B%s%s";     # keyboard button press. 1st %s row, 2nd %s column specifier of button
set MsrMask "M%03d1%s2%s3%s";# Specify format of MSR read message. Format of track 1 is "&<data>?", for track 2 and 3 ";<data>?". %d will be replaced by length of all tracks
set Track1size	77;	        # Length of track 1 (without start and end sentinel)
set Track2size	38;	        # Length of track 2 (without start and end sentinel)
set Track3size	105;	    # Length of track 3 (without start and end sentinel)
set ScanEnable RE;	        # Enable reader command
set ScanDisable RD;	        # disable reader command
set ScanMask "R%1s%s";      # first %s replaced by label type, 2nd %s by ladel data. Supported types are A (UPC-A), E (UPC-E), F (EAN-13) and FF (EAN-8)
set TextOut "T";	        # Prefix of text output command. Will be followed by line no. (one char), text length (2 chars) and text.
set TextLineLength 20;	    # Text line length
set TextLineCount 2;	    # No. of text lines
set StatusReq "SR";         # Request Status command
set StatusResp "S%s%s%s";   # Specify format of status response, 1st %s will be replaced by drawer status char, 2nd %s by lock position and 3rd %s by key value
set BeepOn "B1";            # Start beeping
set BeepOff "B0";           # Stop beeping
set SetEncoding "C";        # Prefix for encoding change
set Encodings {utf-8 ascii cp437 cp1252}


# No further changes necessary if only communication sequences shall be changed
set Status 0
set Lock 0
set Key "000000000000"
set Fd ""
set Track1 ""
set Track2 ""
set Track3 ""
set Reader ""

# Activate or deactivate bell
proc ringBell {val} {
    if {$val == 0} {
        .bell configure -background "" -text ""
    } elseif {$val == 1} {
        bell
        .bell configure -background red -text "Ringing Bell"
    }
}
# enable / disable scanner handling
proc changeScanner {enableordisable} {
	# 1 = enable, 0 = disable.
	# In this version, do nothing (no simulated display)
	if {$enableordisable} {
	    .ds.s.b configure -state normal
	    .ds.s.e configure -state normal
	} {
	    .ds.s.b configure -state disable
	    .ds.s.e configure -state disable
	}
}
proc changeText {line text btext} {
	# line: 0 - TextLineCount-1, text longer than TextLineLength will be trunkated. Shorter texts will be filled with spaces.
	global TextLineLength TextLineCount BlinkOn

	if {$line >= 0 && $line < $TextLineCount} {
        global CLine$line ALine$line

        set CLine$line [string range "$text                    " 0 19]
        set ALine$line [string range "[set btext]nnnnnnnnnnnnnnnnnnnn" 0 19]
        setLine $line
	}
}
# callback for reading from COM port
proc comInputCB {} {
	global SFd StartStop OpenSeq DrawerValues DrawerMask LockValues ScanEnable ScanDisable TextOut TextLineCount TextLineLength StatusReq StatusResp Status Lock Key Fd BeepOn BeepOff SetEncoding Encodings

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
		while {$c != ""} {
			if {[string range $c 0 [string length $OpenSeq]-1] == $OpenSeq} {
				if {$Status != 1} {
					catch {puts -nonewline $Fd "[format $DrawerMask [lindex $DrawerValues [set Status 1]]]"}
				}
				set c [string range $c [string length $OpenSeq] end]
				puts "Drawer opened"
			} elseif {[string range $c 0 [string length $StatusReq]-1] == $StatusReq} {
				catch {puts -nonewline $Fd [format $StatusResp [lindex $DrawerValues $Status] [lindex $LockValues $Lock] $Key]}
				set c [string range $c [string length $StatusReq] end]
				puts "Status: [lindex $DrawerValues $Status] [lindex $LockValues $Lock] $Key"
			} elseif {[string range $c 0 [string length $ScanEnable]-1] == $ScanEnable} {
				changeScanner 1
				set c [string range $c [string length $ScanEnable] end]
				puts "Scanner enabled"
			} elseif {[string range $c 0 [string length $ScanDisable]-1] == $ScanDisable} {
				changeScanner 0
				set c [string range $c [string length $ScanDisable] end]
				puts "Scanner disabled"
			} elseif {[string range $c 0 [string length $SetEncoding]-1] == $SetEncoding} {
			    puts $c
				set c [string range $c [string length $SetEncoding] end]
			    if {[scan [string range $c 0 0] %d encidx] == 1 && $encidx >= 0 && $encidx < [llength $Encodings]} {
			        fconfigure $Fd -encoding [lindex $Encodings $encidx]
			        puts -nonewline $Fd "C1"
			        puts "Encoding changed to [lindex $Encodings $encidx]"
			    } {
                    puts -nonewline $Fd "C0"
                    puts "Encoding <[string range $c 1 1]> not supported"
			    }
			    set c ""
			} elseif {[string range $c 0 [string length $TextOut]-1] == $TextOut} {
			    puts $c
				set c [string range $c [string length $TextOut] end]
				catch {scan $c "%1d%2d" line len} count
				if {$count == 2 && $line >= 0 && $line < $TextLineCount} {
				    set buffer [string range $c 3 $len+2]
					set c [string range $c $len+3 end]
					set text [string range $buffer 0 $TextLineLength-1]
					if {$TextLineLength >= $len} {
                        changeText $line $text ""
					} {
					    changeText $line $text [string range $buffer $TextLineLength [expr $TextLineLength * 2 - 1]]
					}
					puts [format {Line %d: %s}  $line $text]
				} {
					puts "Invalid command, discard $TextOut$c"
					set c ""
				}
			} elseif {[string range $c 0 [string length $BeepOn]-1] == $BeepOn} {
			    ringBell 1
				set c [string range $c [string length $BeepOn] end]
			    puts "Ring bell"
			} elseif {[string range $c 0 [string length $BeepOff]-1] == $BeepOff} {
			    ringBell 0
				set c [string range $c [string length $BeepOff] end]
			    puts "Stop ringing bell"
			} {
				puts "Unknown command: $c"
				set c ""
			}
		}
	}
}

proc changeLock {} {
	global Fd Lock LockValues LockMask

	catch {puts -nonewline $Fd [format $LockMask [lindex $LockValues $Lock]]}
	puts "Lock changed to pos. [lindex $LockValues $Lock]"
}

proc changeDrawer {} {
	global Fd DrawerValues DrawerMask Status

	catch {puts -nonewline $Fd [format $DrawerMask [lindex $DrawerValues $Status]]}
	puts "Drawer changed to [lindex {closed open} $Status]"
}

proc validateKey {P} {
    if {[catch {expr $P << 1}] == 0 && [scan $P {%ld} x] && $x >= 0 && $x < 0x1000000000000} {
        return true
    } elseif {[string compare $P ""] == 0} {
        return true
    }
    return false
}
proc validateTrack1 {c s l} {
	# No start and end sentinel, % and ?, allowed. 64-bit code.
	for {set i [expr [string length $c] - 1]} {$i >= 0} {incr i -1} {
		if {[string first [string range $c $i $i] { !"#$&'()*+,-./0123456789:;<=>@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_}] == -1} {;#"
			return false
		}
	}
	return [expr [string length $s] <= $l ? true : false]
}

proc validateTrack23 {c s l} {
	# No start and end sentinel, ; and ?, allowed. 16-bit code.
	for {set i [expr [string length $c] - 1]} {$i >= 0} {incr i -1} {
		if {[string first [string range $c $i $i] {0123456789:<=>}] == -1} {
			return false
		}
	}
	return [expr [string length $s] <= $l ? true : false]
}

proc validateScanner {c s} {
	# Only 0 - 9 in EAN and UPC allowed. No control digit check.
	for {set i [expr [string length $c] - 1]} {$i >= 0} {incr i -1} {
		if {[string first [string range $c $i $i] {0123456789}] == -1} {
			return false
		}
	}
	return [expr [string length $s] < 14 ? true : false]
}

proc getScanLabelType {} {
	global Reader

	set len [string length $Reader]
	if {$len == 7} {
		return "E";	# UPC-E
	} elseif {$len == 8} {
		return "FF";	# EAN-8
	} elseif {$len == 12} {
		return "A";	# UPC-A
	} elseif {$len == 13} {
		return "F";	# EAN-13
	} {
		return "";	# Invalid
	}
}

proc scanData {} {
	global Reader Fd ScanMask

	set label [getScanLabelType]
	if {$label == ""} {
		tk_messageBox -icon error -title "Bad Scan Value" -message "Scan value length must be 7, 8, 12 or 13" -type ok
	} {
		catch {puts -nonewline $Fd [format $ScanMask $label $Reader]}
		puts "Scanner: $Reader <$label>"
	}
}

proc msrData {} {
	global MsrMask Track1 Track2 Track3 Fd

	if {$Track1 == ""} {set t1 ""} {set t1 "&$Track1?"}
	if {$Track2 == ""} {set t2 ""} {set t2 ";$Track2?"}
	if {$Track3 == ""} {set t3 ""} {set t3 ";$Track3?"}
	set len [expr [string length [format $MsrMask 1 $t1 $t2 $t3]] - [string first 1 [format $MsrMask 1 $t1 $t2 $t3]] - 1]
	catch {puts -nonewline $Fd [format $MsrMask $len $t1 $t2 $t3]}
	puts "MSR: \n  1: $t1\n  2: $t2\n  3: $t3"
}

proc buttonData {row column} {
    global ButtonMask Fd

    catch {puts -nonewline $Fd [format $ButtonMask [format %c [expr $row + 47]] [format %c [expr $column + 64]]]}
    puts "Keyboard: $row/$column"
}
# Defaults for entry fields
set ComPort 34567
set Mode "9600,n,8,1"
set Timeout 10
set StartStop "Start"
set SFd {}

# Click function for Start / Stop button: Opens configured COM port / shut down communication port
proc startStop {} {
	global SFd Fd Mode ComPort StartStop Timeout

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
                fconfigure $Fd -blocking 1 -buffering none -encoding utf-8 -translation {lf lf} -mode $Mode -timeout $Timeout
                fileevent $Fd readable comInputCB
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

proc acceptConnect {channel addr port} {
    global Fd Timeout StartStop
    if {$Fd != ""} {
		after idle "checkConnect $channel $addr $port"
    } {
        puts "Connect from $addr:$port accepted"
        fconfigure $channel -blocking 0 -buffering none -encoding utf-8 -translation {lf lf}
        fileevent $channel readable comInputCB
        set Fd $channel
        set StartStop "Stop (Connected)"
    }
}

proc checkConnect {channel addr port} {
	global Fd
	
	if {$Fd != ""} {
        puts "Connect from $addr:$port rejected"
        close $channel
	} {
		acceptConnect $channel $addr $port
	}
}

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

# Click function for electronic key
proc updateKeyValue {} {
    global EditKey ButtonKey Key Fd KeyMask AnkerKey

    if {$ButtonKey} {
        if {[catch {expr $EditKey << 1}] == 0 && $EditKey > 0 && $EditKey < 0x1000000000000} {
            set Key [format %012lX $EditKey]
            if {$AnkerKey} {
                scan $Key %2s%2s%2s%2s%2s%2s a b c d e f
                set Key "$f$e$d$c$b$a"
            }
            catch {puts -nonewline $Fd [format $KeyMask $Key]}
            puts "New electronic key value: $Key"
        } {
            set ButtonKey 0
            tk_messageBox -icon error -title "Bad Key Value" -message "Key value must be between 1 and (2^48-1)" -type ok
        }
    } {
        set Key "000000000000"
        catch {puts -nonewline $Fd [format $KeyMask $Key]}
        puts "New electronic key value: $Key"
    }
}

# Setup GUI
wm title . "Combi Device"
font create trackfont -family ansifixed -size 7 -weight normal
pack [ttk::frame .sd] -expand 1 -fill both
pack [ttk::labelframe .sd.s -text Settings] -expand 1 -fill both -side left
pack [ttk::labelframe .sd.s.p -text Port] -side left -fill y -anchor w
pack [ttk::entry .sd.s.p.e -textvariable ComPort] -expand 1 -anchor w
pack [ttk::labelframe .sd.s.b -text Mode] -side left -fill y -anchor w
pack [ttk::entry .sd.s.b.e -textvariable Mode] -expand 1 -anchor w
pack [ttk::button .sd.s.x -textvariable StartStop -width 5 -command startStop] -expand 1 -side right -fill both -anchor e
pack [ttk::button .sd.s.d -text Debug -width 5 -command debug] -expand 1 -side right -fill both -anchor e
set EditKey 0
set ButtonKey 0
pack [ttk::frame .ds] -expand 1 -fill both
pack [ttk::labelframe .ds.dr -text Drawer] -expand 1 -fill both -side left 
pack [ttk::radiobutton .ds.dr.o -text Open -value 1 -variable Status -command changeDrawer] -expand 1 -side left -fill both
pack [ttk::radiobutton .ds.dr.c -text Closed -value 0 -variable Status -command changeDrawer] -expand 1 -side left -fill both
pack [ttk::labelframe .ds.l -text Lock] -expand 1 -fill both -side left
pack [ttk::radiobutton .ds.l.pt -text T -value 6 -variable Lock -command changeLock] -expand 1 -side left -fill both
pack [ttk::radiobutton .ds.l.pp -text P -value 5 -variable Lock -command changeLock] -expand 1 -side left -fill both
pack [ttk::radiobutton .ds.l.p0 -text 0 -value 0 -variable Lock -command changeLock] -expand 1 -side left -fill both
pack [ttk::radiobutton .ds.l.p1 -text I -value 1 -variable Lock -command changeLock] -expand 1 -side left -fill both
pack [ttk::radiobutton .ds.l.p2 -text II -value 2 -variable Lock -command changeLock] -expand 1 -side left -fill both
pack [ttk::radiobutton .ds.l.px -text X -value 3 -variable Lock -command changeLock] -expand 1 -side left -fill both
pack [ttk::radiobutton .ds.l.pz -text Z -value 4 -variable Lock -command changeLock] -expand 1 -side left -fill both
pack [ttk::labelframe .ds.k -text {Electronic Key}] -expand 1 -fill both -side left
pack [ttk::entry .ds.k.e -width 15 -font oemfixed -textvariable EditKey -validate key -validatecommand {validateKey %P}] -expand 1 -fill y -side left -anchor w
pack [ttk::checkbutton .ds.k.a -text Anker -onvalue 1 -offvalue 0 -variable AnkerKey] -expand 1 -side left -anchor e
pack [ttk::checkbutton .ds.k.b -text Valid -onvalue 1 -offvalue 0 -variable ButtonKey -command updateKeyValue] -expand 1 -side right -anchor e
pack [ttk::labelframe .ds.s -text Scanner] -side left -fill both
pack [ttk::entry .ds.s.e -width 13 -font oemfixed -textvariable Reader -validate key -validatecommand {validateScanner %S %P}] -expand 1 -fill y -side left
pack [ttk::button .ds.s.b -text Scan -width 4 -command scanData] -expand 1 -fill both -side left
pack [ttk::labelframe .m -text {Magnetic stripe reader}] -expand 1 -fill both
pack [ttk::frame .m.t12] -expand 1 -fill both
pack [ttk::labelframe .m.t12.t1 -text {Track 1}] -expand 1 -fill both -side left
pack [ttk::entry .m.t12.t1.e -textvariable Track1 -width $Track1size -font trackfont -validate key -validatecommand {validateTrack1 %S %P $Track1size}] -expand 1 -fill y -anchor w
pack [ttk::labelframe .m.t12.t2 -text {Track 2}] -expand 1 -fill both -side left
pack [ttk::entry .m.t12.t2.e -textvariable Track2 -width $Track2size -font trackfont -validate key -validatecommand {validateTrack1 %S %P $Track2size}] -expand 1 -fill y -anchor w
pack [ttk::frame .m.t3b] -expand 1 -fill both 
pack [ttk::labelframe .m.t3b.t3 -text {Track 3}] -fill both -side left
pack [ttk::entry .m.t3b.t3.e -textvariable Track3 -width $Track3size -font trackfont -validate key -validatecommand {validateTrack1 %S %P $Track3size}] -expand 1 -fill y -anchor w -side left
pack [ttk::button .m.t3b.b -text "Read" -width 4 -command msrData] -expand 1 -fill both -side left
font configure ansifixed -size 36 -weight bold
pack [ttk::labelframe .di -text Display] -fill both
pack [text .di.sp -width 20 -height $TextLineCount -borderwidth 2 -state disabled -font ansifixed]
.di.sp tag add Normal end
.di.sp tag configure Normal -foreground black -background white
.di.sp tag add Reverse end
.di.sp tag configure Reverse -foreground white -background black
set CLine0 "                    "
set ALine0 "nnnnnnnnnnnnnnnnnnnn"
for {set i 1} {$i < $TextLineCount} {incr i} {
    .di.sp insert end "                    \n"
    set CLine$i "                    "
    set ALine$i "nnnnnnnnnnnnnnnnnnnn"
}

pack [ttk::labelframe .kb -text Keyboard] -fill both
for {set i 1} {$i <= 13} {incr i} {
    for {set j 1} {$j <= 10} {incr j} {
        grid [ttk::button .kb.t[format %02d%02d $j $i] -text [format %c%c [expr $j + 64] [expr $i + 64]] -width 6 -command "buttonData $j $i"] -row $j -column $i -sticky news
    }
}
pack [ttk::label .bell -text "" -anchor center] -expand 1 -fill both
#
set BlinkOn 0
proc blink {} {
    global BlinkOn TextLineCount
    set BlinkOn [expr 1 - $BlinkOn]
    for {set i 0} {$i < $TextLineCount} {incr i} {
        setLine $i
    }
    after 750 blink
}

proc setLine {i} {
    global CLine$i ALine$i BlinkOn
    set pos "[expr $i + 1].0"
    .di.sp configure -state normal
    .di.sp delete $pos "$pos+1l-1c"
    set text [set CLine$i]
    set attrib [set ALine$i]
    for {set j 0} {$j < 20} {incr j} {
        set c [string range $text $j $j]
        set a [string range $attrib $j $j]
        if {($a == "a" || $a == "b") && $BlinkOn == 0} {
            set c " "
        }
        if {$a == "r" || $a == "a"} {
            set tag Reverse
        } {
            set tag Normal
        }
        .di.sp insert "$pos + $j chars" $c "{$tag}"
    }
    .di.sp configure -state disabled
}

blink