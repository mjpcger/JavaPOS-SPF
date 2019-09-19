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

# Fiscal Printer simulator. Simulates a fiscal printer with integrated line display:
#
# All commands and responses use UTF-8 character encoding. The commands have the form
#	cmd [ETB param1 [... ETB paramn]] ETB CHK ETX
# with ETB = 17h as parameter delimiter and ETX = 03h as command terminator, parameters enclosed by "[" and "]"
# are mandatory or optional, where
#	cmd must be replaced by a valid command,
#	param1 ... paramn by mandatory or optional parameters.
# Format and number of mandarory and optional parameters depend on the specific command.
#
# All positive responses have the form
#	ACK status [ETB param1 [... ETB paramn]] ETB CHK ETX
# with ETB, ETX and [] as for commands and ACK = 06h. While ACK will be sent immediately after starting command
# processing, the remainder of the response follows when the command processing has been finished. The application
# can assume a response for printing commands within 5 seconds and within 1 second for all other commands.
# status is a -digit numerical value, where each digit represents one device status value:
# 	1st digit: 0: Command finished successfully, 1: Command aborted
#	2nd digit: 0: Printer not fiscalized, 1: Printer fiscalized, 2: Fiscal block
#	3rd digit: 0: Printer in normal mode, 1: Printer in training mode
#	4th digit: Fiscal memory state: 0: OK, 1: nearly full (space for less than 10 periods), 2: Full
#	5th digit: 0: Period not started, 1: period started
#	6th digit: Receipt state: 0 Not in receipt, 1: itemizing, 2: In Payment, 3: Finalizing, 4: Non-Fiscal receipt, 5: Blocked
#	7th digit: Printer state: 0: OK, 1: near end, 2: paper end, 3: cover open or printer error
#	8th digit: Drawer state: 0: closed, 1: open
#
# For a detailed description of commands and the corresponding responses see the comments in functions listed in proc usage.
#
# If a command is invalid (invalid command name or invalid number of parameters), the response will be
#	NAK
# with NAK = 15h.
#
# Chk is one single byte. Its byte value is 128 - (SUM % 96), where SUM is the sum of all previous characters of the command or
# response (ACK not included).
#
# Command lastResponse can be used to retrieve the last generated response. It can be used to retrieve the last response in
# case of a line break or unexpected disconnect or in case of a timeout.
#
# Simulator can communicate using COM ports (keep in mind: On Windows, COM ports > COM9 must be specified with
# leading "\\.\", e.g. COM12 must be specified as \\.\COM12) or TCP. For TCP, the given port must be an integer
# between 1 and 65535. If a COM port has been specified, a mode must be specified as well. The other end-point
# of the communication line must use the corresponding line settings.

# Structure of electronic journal
# Every receipt will be stored in a list containing receipt no, date and the receipt itself.
# All receipts of one period will be stored within a period-specific file. 

pack [ttk::labelframe .a -text Settings] -expand 1 -fill both
pack [ttk::labelframe .a.p -text Port] -expand 1 -fill both -side left
pack [ttk::entry .a.p.e -textvariable Port -width 10 -validate key -validatecommand {portCheck %P}] -expand 1 -fill both
pack [ttk::labelframe .a.m -text Mode] -expand 1 -fill both -side left
pack [ttk::combobox .a.m.b -state disabled -values {"9600,n,8,2" "9600,e,8,1" "9600,o,8,1"}] -expand 1 -fill both
pack [ttk::button .a.dbg -text "Debug On" -width 5 -command setDebug] -fill both -expand 1 -side left
pack [ttk::button .a.st -text Start -width 5 -command startstop] -fill both -expand 1 -side left

set Port 65432
set Modes {
	"9600,n,8,2" "9600,e,8,1" "9600,o,8,1"
	"38400,n,8,2" "38400,e,8,1" "38400,o,8,1"
}

font create fontA -family Courier -size 12 -weight normal
font create boldA -family Courier -size 12 -weight bold
font create fontB -family Courier -size 9 -weight normal
font create boldB -family Courier -size 9 -weight bold
font create dispFont -family Courier -size 27 -weight bold
pack [ttk::labelframe .d -text "Display Window"]
set DisplayLine1 ""
set DisplayLine2 ""
pack [ttk::entry .d.l1 -textvariable DisplayLine1 -state readonly -width 20 -font dispFont]
pack [ttk::entry .d.l2 -textvariable DisplayLine2 -state readonly -width 20 -font dispFont]
pack [ttk::labelframe .t -text "Printer Window"]
pack [text .t.x -width 42 -height 30 -font fontA -state disabled -yscrollcommand [list .t.s set]] -side left -expand 1 -fill y
pack [scrollbar .t.s -command [list .t.x yview]] -side right -expand 1 -fill y
.t.x tag add ABold end
.t.x tag configure ABold -font boldA
.t.x tag add ANormal end
.t.x tag configure ANormal -font fontA
.t.x tag add BBold end
.t.x tag configure BBold -font boldB
.t.x tag add BNormal end
.t.x tag configure BNormal -font fontB
.t.x tag add Underline end
.t.x tag configure Underline -underline 1
.t.x tag add Red end
.t.x tag configure Red -foreground red
.t.x tag add Left end
.t.x tag configure Left -justify left
.t.x tag add Right end
.t.x tag configure Right -justify right
.t.x tag add Center end
.t.x tag configure Center -justify center
.t.x tag add Strike end
.t.x tag configure Strike -overstrike 1

set Taglist {ANormal Left}
set CurrentCodepage utf-8
set FirstPos [.t.x index "end - 1 chars"]
set StartPos $FirstPos

# Add the given tag to Taglist, if it is not yet present in the list.
#				tagname must be one of Underline, Red
proc addTag {tagname} {
	global Taglist
	
	if {[lsearch $Taglist $tagname] == -1} {
		lappend Taglist $tagname
	}
}

# Delete the given tag from Taglist, if it is püresent in the list.
#				tagname must be one of Underline, Red
proc delTag {tagname} {
	global Taglist
	
	if {[set index [lsearch $Taglist $tagname]] > 0} {
		if {[llength $Taglist] - $index == 1} {
			set Taglist [lrange $Taglist 0 [expr $index - 1]]
		} {
			set Taglist [concat [lrange $Taglist 0 [expr $index - 1]] [lrange $Taglist [expr $index + 1] end]]
		}
	}
}

# Set font tag. fontname must be A or B. Taglist will be modified in its first element to start with the
#				given font name.
proc setFont {fontname} {
	global Taglist

	lset Taglist 0 "$fontname[string range [lindex $Taglist 0] 1 end]"
}

# Set font tag. weightname must be Normal or Bold. Tablist will be modified in its first element to end with
#				the given weight name.
proc setWeight {weightname} {
	global Taglist
	
	lset Taglist 0 "[string range [lindex $Taglist 0] 0 0]$weightname"
}

# Set orientation tag. tagname must be Jeft, Center or Right. The second element of tallist will be set to
#				the given tag name.
proc setOrientation {tagname} {
	global Taglist
	
	lset Taglist 1 $tagname
}

set FiscLineCount 0
set LineCount 0
set PaperLimit 1000
set NearEndOffset 100
set StatusList {Operational {Paper Low} {Paper End} {Cover Open} {Printer Error}}
set DrawerStatusList {Closed Open}

pack [ttk::labelframe .s -text Status] -expand 1 -fill both
pack [ttk::labelframe .s.ml -text "Max.Lines"] -expand 1 -fill both -side left
pack [ttk::entry .s.ml.e -textvariable PaperLimit -width 5] -expand 1 -fill both
pack [ttk::labelframe .s.ne -text "Near End"]  -expand 1 -fill both -side left
pack [ttk::entry .s.ne.e -textvariable NearEndOffset -width 3] -expand 1 -fill both
pack [ttk::labelframe .s.pl -text "Prt.Lines"] -expand 1 -fill both -side left
pack [ttk::entry .s.pl.e -textvariable LineCount -width 5 -state readonly] -expand 1 -fill both
pack [ttk::labelframe .s.st -text "Status"] -expand 1 -fill both -side left
pack [ttk::combobox .s.st.b -state readonly -values $StatusList -width 11] -expand 1 -fill both
.s.st.b current 0
pack [ttk::labelframe .s.ds -text "Drawer"] -expand 1 -fill both -side left
set DrawerStatusValue ""
pack [ttk::combobox .s.ds.b -state readonly -values $DrawerStatusList -width 7 -textvariable DrawerStatusValue] -expand 1 -fill both
.s.ds.b current 0
pack [ttk::button .s.rp -text "Change Paper" -width 13 -command changePaper] -expand 1 -fill both

set MaxLinesShown 1005
set PaperEndNotPrinted 1

# Change paper button click handler
proc changePaper {} {
    global LineCount
	if {[.s.st.b current] == 3} {
		set LineCount 0
		.t.x configure -state normal
		.t.x delete 1.0 end
		.t.x configure -state disabled
	} {
		tk_messageBox -icon error -title "Change Paper Error" -message "Cover must be opened for paper change" -type ok
	}
}

proc drawerCheck {} {
	global DrawerStatusValue ReceiptState
	
	while 1 {
		tkwait variable DrawerStatusValue
		if {[.s.ds.b current] == 0} {
			if {$ReceiptState != 3} {
				display 1 ""
				display 2 ""
			}
			break
		}
	}
}

# Display text in line display
# Parameters:
#	line	line no, 1 or 2
#	text	text to be displayed, shall not contain control characters
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc display {line text} {
	global DisplayLine$line
	
	if {[lsearch -exact {1 2} $line] < 0} {
		return -1
	}
	if {[string length $text] > 20} {
		return -2
	}
	.d.l$line configure -state normal
	set DisplayLine$line $text
	.d.l$line configure -state readonly
	return 1
}

# Print function. Writes text containing escape sequences and control characters as described
# for SamplePrinterSimulator to the print window.
# Parameter: Text to be printed. 
proc print {text} {
	global Taglist CurrentCodepage LineCount PaperLimit NearEndOffset MaxLinesShown PaperEndNotPrinted
	
	set len [string length $text]
	set toBeInserted ""
	.t.x configure -state normal
	for {set i 0} {$i < $len} {incr i} {
		set c [string range $text $i $i]
		if {$c == "\14" || $c == "\33"} {
			if {$toBeInserted != "" && [.s.st.b current] < 2} {
				.t.x insert "end-1c" [encoding convertfrom $CurrentCodepage $toBeInserted] $Taglist
				set toBeInserted ""
			}
			if {$c == "\14"} {
				global StartPos FirstPos

                set cutline [expr [.t.x index end] - 3]
                if {$cutline >= 1} {
                    .t.x delete $cutline "$cutline + 1l"
                    if {[.s.st.b current] < 2} {
                        .t.x insert $cutline "                                          \n" {{ANormal} {Strike}}
                    }
           	    }
			} {
				incr i
				set c [string range $text $i $i]
				if {$c == "d"} {
					.s.ds.b current 1
				} elseif {[lsearch {b c f o p s u} $c] >= 0} {
					incr i
					set v [string range $text $i $i]
					if {$c == "b"} {
						if {$v == "0"} {
							setWeight Normal
						} elseif {$v == "1"} {
							setWeight Bold
						}
					} elseif {$c == "c"} {
						if {$v == "0"} {
							delTag Red
						} elseif {$v == "1"} {
							addTag Red
						}
					} elseif {$c == "f"} {
						if {$v == "A" || $v == "B"} {
							setFont $v
						}
					} elseif {$c == "o"} {
						if {$v == "l"} {
							setOrientation Left
						} elseif {$v == "c"} {
							setOrientation Center
						} elseif {$v == "r"} {
							setOrientation Right
						}
					} elseif {$c == "u"} {
						if {$v == "0"} {
							delTag Underline
						} elseif {$v == "1"} {
							addTag Underline
						}
					} elseif {$c == "s"} {
						if {$v == "0"} {
							delTag Strike
						} elseif {$v == "1"} {
							addTag Strike
						}
					}
				}
			}
		} {
			set toBeInserted "$toBeInserted$c"
			if {$c == "\12"} {
			    if {[.s.st.b current] < 2} {
                    .t.x insert "end-1c" [encoding convertfrom $CurrentCodepage $toBeInserted] $Taglist
			        set toBeInserted ""
                    incr LineCount
                    setStatus
				}
			}
		}
	}
	if {$toBeInserted != "" && [.s.st.b current] < 2} {
		.t.x insert "end-1c" [encoding convertfrom $CurrentCodepage $toBeInserted] $Taglist
	}
    if {[.s.st.b current] >= 2 && $PaperEndNotPrinted} {
        .t.x insert "end-1c" "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" {{ANormal}}
        set PaperEndNotPrinted 0
    }
	if {[set deletecount [expr int([.t.x index end] - $MaxLinesShown)]] > 0} {
	    .t.x delete 1.0 "1.0+[set deletecount]l"
	}
	.t.x see end
	.t.x configure -state disabled
}

# Printer status update routine
proc setStatus {} {
	global LineCount NearEndOffset PaperLimit PaperEndNotPrinted

	if {[.s.st.b current] == 0} {
		if {$LineCount >= $PaperLimit - $NearEndOffset} {
			.s.st.b current 1
		}
		set PaperEndNotPrinted 1
	}
	if {[.s.st.b current] == 1} {
		if {$LineCount >= $PaperLimit} {
			.s.st.b current 2
		}
		set PaperEndNotPrinted 1
	}
}

# Debug button click handler
proc setDebug {} {
	if {[.a.dbg cget -text] == "Debug On"} {
		.a.dbg configure -text "Debug Off"
		console show
	} {
		.a.dbg configure -text "Debug On"
		console hide
	}
}

set SFd ""
set Fd ""

# TCP server
proc waitConnect {fd ip port} {
	global Fd
	
	if {$Fd != ""} {
		close $fd
		puts "Reject connect from $ip:$port"
	} {
		fconfigure $fd -blocking 0 -buffering none -translation binary -encoding binary
		fileevent $fd readable processData
		set Fd $fd
		puts "Connect from $ip:$port, waiting for data"
	}
}

# Handling Start / Stop button click
proc startstop {} {
	global Port SFd Fd Modes
	
	if {[.a.st cget -text] == "Start"} {
		if {[.a.m.b cget -state] == "disabled"} {
			if [catch {socket -server waitConnect $Port} SFd] {
				set SFd ""
				return 0
			}
			puts "$Port opened, waiting for connect"
		} {
			if {[set mode [lindex $Modes [.a.m.b current]]] == "" || [catch {
				set SFd [open $Port r+]
				fconfigure $SFd -blocking 1 -buffering none -translation binary -encoding binary -mode $mode -timeout 10
				fileevent $SFd readable processData	
				set Fd $SFd
			}]} {
				set SFd ""
				return 0
			}
			puts "$Port opened, waiting for data"
		}
		.a.st configure -text "Stop"
		.a.p.e configure -state readonly
		.a.m.b configure -state disabled
		.s.ml.e configure -state readonly
		.s.ne.e configure -state readonly
	} {
		catch {
			close $SFd
			close $Fd
		}
		set Fd [set SFd ""]
		puts "$Port closed"
		.a.st configure -text "Start"
		.a.p.e configure -state normal
		portCheck $Port
		.s.ml.e configure -state normal
		.s.ne.e configure -state normal
	}
}

# Checks port for validity. Disables or enables communication mode settings, depending on
# port value: If numeric between 0 and 65535, mode setting will be disabled (TCP), else enabled. 
proc portCheck {value} {
	set currentstate [.a.m.b cget -state]
	if {[catch {expr int($value)} val] == 0 && $val == $value && $val > 0 && $val < 0xffff} {
		if {$currentstate != "disabled"} {
			.a.m.b configure -state disabled
		}
	} {
		if {$currentstate != "readonly"} {
			.a.m.b configure -state readonly
		}
	}
	return 1
}

set ParamSetter "[file rootname [file normalize $argv0]]Vars.tc"
set JournalFile "[file rootname [file normalize $argv0]]Journal"

# Generates parameter setter script to be used during startup to restore internal variables
proc updateParamSetter {} {
	global ParamSetter Period VAT HdLine TrLine TaxPayerID SerialNo GrandTotal Memory Total CurrentTotal Position ReceiptState Training LineCount CurrentCodepage Till Cashier DisplayLine1 DisplayLine2 At Blocked JournalBuffer JournalValueLength LastFrameSent
	
	set fd [open $ParamSetter w]
	fconfigure $fd -encoding $CurrentCodepage
	puts $fd {# Current period, 0: pre-fiscal, MaxPeriod: block}
	puts $fd "set Period $Period"
	puts $fd {# Up to 4 VAT rates, unused rate set to any negative value}
	puts $fd "array set VAT {[array get VAT]}"
	puts $fd {# Up to 4 header lines, unused header lines empty}
	puts $fd "set HdLine {$HdLine}"
	puts $fd {# Up to 4 trailer lines, unused header lines empty}
	puts $fd "set TrLine {$TrLine}"
	puts $fd {# Tax payer ID}
	puts $fd "set TaxPayerID {$TaxPayerID}"
	puts $fd {# Till number}
	puts $fd "set Till {$Till}"
	puts $fd {# Cashier number}
	puts $fd "set Cashier {$Cashier}"
	puts $fd {# Unique fiscal serial number}
	puts $fd "set SerialNo {$SerialNo}"
	puts $fd {# Current grand total}
	puts $fd "set GrandTotal $GrandTotal"
	puts $fd {# Contents of fiscal memory, the totals for each former period}
	puts $fd "set Memory {"
	foreach period $Memory {
		puts $fd "    {$period}"
	}
	puts $fd "}"
	puts $fd {# Totals and counters of current period}
	puts $fd "array set Total {[array get Total]}"
	puts $fd {# Totals of current receipt}
	puts $fd "array set CurrentTotal {[array get CurrentTotal]}"
	puts $fd {# VAT and amount of current position, for payment only amount}
	puts $fd "array set Position {[array get Position]}"
	puts $fd {# Current receipt state}
	puts $fd "set ReceiptState $ReceiptState"
	puts $fd {# Current training mode state}
	puts $fd "set Training $Training"
	puts $fd {# Current receipt block}
	puts $fd "set Blocked $Blocked"
	puts $fd {# at character, delimiter between quantity and unit price}
	puts $fd "set At $At"
	puts $fd {# Receipt journal}
	puts $fd "set JournalBuffer {$JournalBuffer}"
	puts $fd {# Length of journal values}
	puts $fd "set JournalValueLength $JournalValueLength"
	puts $fd {# Last frame sent}
	puts $fd "set LastFrameSent {$LastFrameSent}"
	puts $fd {# Current line count}
	puts $fd "set LineCount $LineCount"
	puts $fd {# Current display line 1}
	puts $fd "display 1 {$DisplayLine1}"
	puts $fd {# Current display line 2}
	puts $fd "display 2 {$DisplayLine2}"
	close $fd
}

if {[file exists $ParamSetter]} {
	source -encoding $CurrentCodepage $ParamSetter
} {
	set Period 0
	array set VAT {1 -1 2 -1 3 -1 4 -1}
	set HdLine {{} {} {} {} {}}
	set TrLine {{} {} {} {} {}}
	set TaxPayerID {}
	set Till 0
	set Cashier 0
	set SerialNo {}
	set GrandTotal 0
	set Memory {}
	array set Total "All 0 Voids 0 Aborts 0 Refunds 0 Adjustments 0 CAdjustments 0 CAll 0 CVoids 0 CAborts 0 CRefunds 0 VAT1 0 VAT2 0 VAT3 0 VAT4 0 Date 0 Fiscal 0 Normal 0"
	array set CurrentTotal {All 0 Voids 0 Refunds 0 Adjustments 0 CAdjustments 0 CAll 0 CVoids 0 CAborts 0 CRefunds 0 VAT1 0 VAT2 0 VAT3 0 VAT4 0}
	array set Position {VAT 1 Amount 0 Adjustments 0 CAdjustments 0 Flag {}}
	set ReceiptState 0
	set Training 0
	set Blocked 0
	set At [encoding convertfrom utf-8 à]
	set JournalBuffer {}
	set JournalValueLength 12
	set LastFrameSent {}
	updateParamSetter
}

set Buffer {}
set CharTio 100
set MaxPeriod 100
set FiscalLogo "_\n\33s1\\\33s0\33u1/\33u0"
set TioID {}

# Print text lines
# Parameters:
#	nonfiscal: 	0 for fiscal receipts, else non-fiscal receipt
#	text:		Text to be printed, may contain linefeed characters as line separator and SO and SI to mark
#               secret contents (will be replaced by '*' characters in log file).
proc printfisc {nonfiscal text} {
	global CurrentCodepage Training FiscLineCount JournalBuffer
	
	set len [llength [set lines [split $text "\n"]]]
	set result {}
	set journal {}
	for {set i 0} {$i < $len} {incr i} {
		lappend result [replacesecrets [lindex $lines $i] 0]
		lappend journal [replacesecrets [lindex $lines $i] 1]
		if {$FiscLineCount % 5 == 4 && $nonfiscal} {
			if {$Training} {
				lappend result [centeredLine "*** Training Mode ***"]
			} {
				lappend result [centeredLine "*** Non Fiscal***"]
			}
		}
		incr FiscLineCount
	}
	set JournalBuffer "$JournalBuffer[join $journal "\n"]\n"
	print [encoding convertto $CurrentCodepage [format "%s%s" [join $result "\n"] "\n"]]
}

# Search al characters between SO and SI and replace these characters by asterisk, if requested.
# Paratemets:
#   line:   Text line to be modified
#   flag:   Specifies whether text between SO and SI shall be replaced by a sequence of asterisks (1) or
#           not (0)
proc replacesecrets {line flag} {
    set result ""
    while {[set index [string first "\16" $line]] >= 0} {
        set result "$result[string range $line 0 $index-1]"
        if {[set index2 [string first "\17" $line $index]] > $index} {
            set sotext [string range $line $index+1 $index2-1]
            set line [string range $line $index2+1 end]
        } {
            set sotext [string range $line $index+1 end]
            set line ""
        }
        if $flag {
            set sotext [string repeat "*" [string length $sotext]]
        }
        set result "$result$sotext"
    }
    return "$result$line"
}

# Set header line
# Parameters:
#	line	Line number, 1 - 4
#	text	Line contents
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc setHeaderLine {line text} {
	global Period ReceiptState HdLine Total MaxPeriod

	if {[lsearch -exact {1 2 3 4 5} $line] < 0} {
		return -1
	}
	if {$Period >= $MaxPeriod || ($Total(Fiscal) != 0 && $line <= 4) || $ReceiptState != 0} {
		return 0
	}
	if {[string length $text] > [.t.x cget -width]} {
		return -2
	}
	incr line -1
	lset HdLine $line $text
	updateParamSetter
	return 1
}

# Set trailer line
# Parameters:
#	line	Line number, 1 - 4
#	text	Line contents
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc setTrailerLine {line text} {
	global Period ReceiptState TrLine Total MaxPeriod

	if {[lsearch -exact {1 2 3 4 5} $line] < 0} {
		return -1
	}
	if {$Period >= $MaxPeriod || (($Total(Fiscal) != 0 || $ReceiptState != 0) && $line <= 4)} {
		return 0
	}
	if {[string length $text] > [.t.x cget -width]} {
		return -2
	}
	incr line -1
	lset TrLine $line $text
	updateParamSetter
	return 1
}

# Set VAT rate
# Parameters:
#	index	Index of VAT rate to be set 1, 2, 3 or 4
#	rate	corresponding VAT rate in percent. VAT rates must be in decreasing order (VAT(n) <= VAT(m) for n > m)
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc setVatRate {index rate} {
	global Period ReceiptState VAT Total MaxPeriod

	if {$Period >= $MaxPeriod || $Total(Fiscal) != 0 || $ReceiptState != 0} {
		return 0
	}
	if {[array names VAT -exact $index] == ""} {
		return -1
	}
	if {[scan $rate "%f%s" v x] != 1 || $v >= 100 || ($v != [format "%5.2f" $v] && $v >= 0) || ($index > 1 && $VAT([expr $index - 1]) < $v)} {
		return -2
	}
	set VAT($index) $v
	updateParamSetter
	return 1
}

# Set tax payer ID
# Parameter: Tax payer ID, max. 20 letters
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc setTaxPayerID {text} {
	global Period ReceiptState TaxPayerID Total MaxPeriod

	if {$Period >= $MaxPeriod || $Total(Fiscal) != 0 || $ReceiptState != 0} {
		return 0
	}
	if {[string length $text] > 20} {
		return -1
	}
	set TaxPayerID $text
	updateParamSetter
	return 1
}

# Set till number.
# Parameter: Till number, positive < 100000
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc setTill {text} {
	global Period ReceiptState Till Total MaxPeriod

	if {$Period >= $MaxPeriod || $Total(Fiscal) != 0 || $ReceiptState != 0} {
		return 0
	}
	if {[scan $text "%d%s" val x] != 1 || $val <= 0 || $val >= 100000} {
		return -1
	}
	set Till $val
	updateParamSetter
	return 1
}

# Set cashier number for fiscal receipt ending.
# Parameter: Cashier number, positive < 100000
# Return:
#	1 on success, -1 if cashier number out of range (0 - 99999).
proc setCashier {text} {
	global Cashier

	if {[scan $text "%d%s" val x] != 1 || $val < 0 || $val >= 100000} {
		return -1
	}
	set Cashier $val
	updateParamSetter
	return 1
}

# Provide centered line for printing
proc centeredLine {line} {
	return "[format %[expr ([.t.x cget -width] + [string length $line]) / 2]s $line]"
}

# Provide header lines for printing.
proc headerlines {} {
	global HdLine TaxPayerID FiscalLogo
	
	set result ""
	foreach line $HdLine {
		if {$line != ""} {
			set result "$result[centeredLine $line]\n"
		}
	}
	set result "$result[centeredLine [format {Tax ID: %s} $TaxPayerID]]\n"
	return $result
}

# Provide trailer lines for printing.
# Parameters are:
#	what	Fiscal or Normal, specifies receipt type (Normal = non-fiscal receipt)
#	print	if 0, trailer lines will not be printed
proc trailerlines {what print} {
	global TrLine Period Till Cashier Total CurrentDate FiscalLogo
	
	set result "\n"
	if {$print} {
		foreach line $TrLine {
			if {$line != ""} {
				set result "$result[centeredLine $line]\n"
			}
		}
	}
	incr Total($what)
	set lastline [format {%s %05d %5d %03d-%05d} [clock format [set CurrentDate [clock seconds]] -format {%Y-%m-%d %H:%M}] $Till $Cashier $Period [expr $Total(Fiscal) + $Total(Normal)]]
	if {$what == "Normal"} {
		return "$result\n   $lastline"
	}
	return "$result$FiscalLogo $lastline"
}

# Retrieve journal size:
# Returns:
#   Sum of size of all journal files.
proc getJournalUsed {} {
    global JournalFile

    if {[catch {glob $JournalFile*} files] == 0} {
        set sum 0
        foreach file $files {
            incr sum [file size $file]
        }
        return $sum
    }
    return 0;
}

# Cut command: Terminates receipt and writes electronic journal
proc cutFisc {} {
	global CurrentDate JournalBuffer JournalFile Period JournalValueLength Total FiscalLogo

	set valformat [format "%%0%dld" $JournalValueLength]
	if {[file exists "$JournalFile.[format %03d $Period].dat"] == 0} {
		set fd [open "$JournalFile.[format %03d $Period].dat" w]
		fconfigure $fd -translation binary -encoding utf-8
		puts -nonewline $fd [format $valformat $JournalValueLength]
		close $fd
	}
	set fd [open "$JournalFile.[format %03d $Period].dat" r+]
	fconfigure $fd -translation binary -encoding utf-8
	scan [read $fd $JournalValueLength] $valformat pos
	seek $fd $pos
	set table [read $fd]
	seek $fd $pos
	if {[set first [string first $FiscalLogo $JournalBuffer]] >= 0} {
		set JournalBuffer [string replace $JournalBuffer $first [expr $first + [string length $FiscalLogo] - 1] "\n  "]
	}
	puts -nonewline $fd $JournalBuffer
	set tablepos [tell $fd]
	puts -nonewline $fd $table
	puts $fd [format "$valformat$valformat$valformat$valformat$valformat" $pos $tablepos $CurrentDate [expr $Total(Normal) + $Total(Fiscal)] [checksum $JournalBuffer]]
	seek $fd 0
	puts -nonewline $fd [format $valformat $tablepos]
	close $fd
	printfisc 0 "\n\n\14"
	set JournalBuffer {}
	updateParamSetter
}

set JournalData {}
# Retrieve from journal. Parameters:
#	period	Period for which journal will be retrieved
#	from	Receipt number of first receipt to be retrieved
#	to		Receipt number of last receipt to be retrieved
#	what	0: retrieve dates, 1: retrieve print data
# Return:
#	1 on success, 0 if state blocks command processing. On success, journal data are in variable JournalData.
proc retrieveJournal {period from to what} {
	global Period JournalFile JournalValueLength JournalData
	
	if {($period >= 0 && $period <= $Period && ![file exists "$JournalFile.[format %03d $period].dat"])} {
		return 0
	}
	if {$period < 0 || $period > $Period} {
		return -1
	}
	set valformat [format "%%0%dld" $JournalValueLength]
	set fd [open "$JournalFile.[format %03d $period].dat" r+]
	fconfigure $fd -translation binary -encoding utf-8
	scan [read $fd $JournalValueLength] $valformat pos
	seek $fd $pos
	set table [split [string range [read $fd] 0 end-1] "\n"]
	scan [lindex $table 0] "$valformat$valformat$valformat$valformat$valformat" spos1 epos1 date1 num1 chk1
	scan [lindex $table end] "$valformat$valformat$valformat$valformat$valformat" spose epose datee nume chke
	close $fd
	if {$from < $num1 || $from > $nume} {
		return -2
	}
	if {$to < $from || $to > $nume} {
		return -3
	}
	if {[lsearch -exact {1 0} $what] < 0} {
		return -4
	}
	set fd [open "$JournalFile.[format %03d $period].dat" r+]
	fconfigure $fd -translation binary
	set JournalData {}
	for {set i [expr $from - 1]} {$i <= $to - 1} {incr i} {
		if {[scan [lindex $table $i] "$valformat$valformat$valformat$valformat$valformat%s" spos epos date num chk x] != 5 || $num - 1 != $i} {
			close $fd
			return 0
		}
		if {$what == 0} {
			lappend JournalData $date
		} elseif {$what == 1} {
			seek $fd $spos
			if {[set sum [checksum [set data [encoding convertfrom utf-8 [read $fd [expr $epos - $spos]]]]]] != $chk} {
				puts "$data\nSUM: $sum != $chk!!!"
				close $fd
				return 0
			}
			lappend JournalData $data
		}
	}
	close $fd
	return 1
}

# Print from journal. Parameters:
#	period	Period for which journal will be printed
#	from	Receipt number of first receipt to be printed
#	to		Receipt number of last receipt to be printed
# Return:
#	1 on success, 0 if state blocks command processing.
proc printJournal {period from to} {
	global JournalData CurrentCodepage
	
	if {[paper] && [set res [retrieveJournal $period $from $to 1]] == 1} {
		set delim "[string range " _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _" 1 [.t.x cget -width]]\n\n"
		print "[encoding convertto $CurrentCodepage [join $JournalData $delim]]\n\n\14"
		return [paper]
	}
	return 0
}

# Compute checksum for receipt data
proc checksum {data} {
	set len [string length $data]
	set sum 0xaffebddc
	for {set i 0} {$i < $len} {incr i} {
		scan [string range $data $i $i] %c code
		set sum [expr (($sum * 3) + $code) % 0x100000000]
	}
	return $sum
}

# Print fiscalization report
# Return:
#	1 on success, 0 if state blocks command processing.
proc fiscalize {} {
	global Period VAT HdLine TaxPayerID SerialNo Training Total FiscLineCount Memory
	
	set FiscLineCount 0
	if {[.s.ds.b current] > 0 || ![paper] || $Period != 0 || $SerialNo == "" || $TaxPayerID == "" || [join HdLine ""] == "" || $VAT(1) <= 0} {
		return 0
	}
	set receipt "[headerlines]\n[centeredLine {Fiscalization}]\n\n"
	for {set i 1} {[array names VAT -exact $i] != ""} {incr i} {
		if {$VAT($i) < 0} {
			set receipt "$receipt    VAT$i: Invalid\n"
		} {
			set receipt "$receipt[format {    VAT %s: %5.2f %%%c} [string range " ABCDEFGH" $i $i] $VAT($i) 10]"
		}
	}
    printfisc $Training "$receipt[trailerlines [lindex {Fiscal Normal} $Training] 1]"
	if [paper] {
        cutFisc
        if {$Training == 0} {
            incr Period
            if {$Total(Date) == 0} {
                set Total(Date) [clock seconds]
            }
            lappend Memory [array get Total]
            incr Total(Fiscal) -1
            updateParamSetter
        }
		return 1
	}
	incr Total([lindex {Fiscal Normal} $Training]) -1
	updateParamSetter
	return 0
}

# Print period end report
# Return:
#	1 on success, 0 if state blocks command processing.
proc zReport {} {
	global Period Memory Total Training MaxPeriod
	
	if {$Total(Fiscal) == 0 || [currentReport Fiscal] == 0} {
		return 0
	}
	if {[paper] && $Training == 0} {
		incr Period
		if {$Total(Date) == 0} {
			set Total(Date) [clock seconds]
		}
		lappend Memory [array get Total]
		array set Total "All 0 Voids 0 Aborts 0 Refunds 0 Adjustments 0 CAdjustments 0 CAll 0 CVoids 0 CAborts 0 CRefunds 0 VAT1 0 VAT2 0 VAT3 0 VAT4 0 Date 0 Fiscal 0 Normal 0"
		updateParamSetter
	}
	return [paper]
}

# Print intermediate report for current period.
# Return:
#	1 on success, 0 if state blocks command processing.
proc xReport {} {
	return [currentReport Normal]
}

# Print report for current period.
# Parameter: Specified whether it is an intermediate report ($what == Normal) or a period finalizing report ($what == Fiscal)
# Return:
#	1 on success, 0 if state blocks command processing.
proc currentReport {what} {
	global Period ReceiptState Total Training VAT MaxPeriod FiscLineCount

	set FiscLineCount 0
	if {[.s.ds.b current] > 0 || $Period <= 0 || $Period >= $MaxPeriod || $Total(Fiscal) + $Total(Normal) == 0 || $ReceiptState != 0} {
		return 0
	}
	set receipt "[headerlines]\n"
	if {$what == "Fiscal"} {
		set receipt "$receipt[centeredLine [format {Report for Period %d / %s} $Period [clock format $Total(Date) -format {%Y-%m-%d %H:%M}]]]\n\n"
	} {
		set receipt "$receipt[centeredLine [format {Summary for Period %d / %s} $Period [clock format $Total(Date) -format {%Y-%m-%d %H:%M}]]]\n\n"
	}
	set receipt "$receipt  [format {No. of fiscal receipts:    %5d} $Total(Fiscal)]\n"
	set receipt "$receipt  [format {No. of non-fiscal receipts:%5d} $Total(Normal)]\n"
	set receipt "$receipt  [format {Total amount:      (%6d)  %9.2f} $Total(CAll) $Total(All)]\n"
	set receipt "$receipt  [format {Total void:        (%6d)  %9.2f} $Total(CVoids) $Total(Voids)]\n"
	set receipt "$receipt  [format {Total abort:       (%6d)  %9.2f} $Total(CAborts) $Total(Aborts)]\n"
	set receipt "$receipt  [format {Total refund:      (%6d)  %9.2f} $Total(CRefunds) $Total(Refunds)]\n"
	set receipt "$receipt  [format {Total adjustments: (%6d)  %9.2f} $Total(CAdjustments) $Total(Adjustments)]\n"
	if {$VAT(1) >= 0} {
		set receipt "$receipt  [format {Total VAT A (%5.2f%%):  %9.2f} $VAT(1) $Total(VAT1)]\n"
	}
	if {$VAT(2) >= 0} {
		set receipt "$receipt  [format {Total VAT B (%5.2f%%):  %9.2f} $VAT(2) $Total(VAT2)]\n"
	}
	if {$VAT(3) >= 0} {
		set receipt "$receipt  [format {Total VAT C (%5.2f%%):  %9.2f} $VAT(3) $Total(VAT3)]\n"
	}
	if {$VAT(4) >= 0} {
		set receipt "$receipt  [format {Total VAT D (%5.2f%%):  %9.2f} $VAT(4) $Total(VAT4)]\n"
	}
    printfisc $Training "$receipt[trailerlines [lindex "$what Normal" $Training] 0]"
	if [paper] {
        cutFisc
		return 1
	}
	incr Total([lindex "$what Normal" $Training]) -1
	updateParamSetter
	return 0
}

# Period report, prints data from fiscal memory for a specified period range.
# Parameters are:
#	from 	First period within range
#	to		Last period within range
#	sum		0 to print report and sum for all periods within range, else prints only sum.
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc report {from to sum} {
	global Period ReceiptState Total Training Memory MaxPeriod FiscLineCount VAT

	if {[.s.st.b current] > 0 || [.s.ds.b current] > 0 || $Period <= 0 || $Period >= $MaxPeriod || $ReceiptState != 0} {
		return 0
	}
	if {[scan "$from $to" %d%d%s f t x] != 2 || $from <= 0 || $to >= $Period || $from > $to} {
		return [expr ($from > 0 && $from < $Period) ? -2 : -1]
	}
	if {[scan $sum %d%s s x] != 1} {
		return -3
	}
	set FiscLineCount 0
	set receipt "[headerlines]\n[centeredLine [format {Report for period %d - %d} $from $to]]\n\n"
	array set sums {All 0 Voids 0 Aborts 0 Refunds 0 Adjustments 0 CAdjustments 0 CAll 0 CVoids 0 CAborts 0 CRefunds 0 Date 0 VAT1 0 VAT2 0 VAT3 0 VAT4 0 Fiscal 0 Normal 0}
	for {set i $from} {$i <= $to} {incr i} {
		array set total [lindex $Memory $i]
		if {$sum == 0} {
			set receipt "$receipt  Period $i / [clock format $total(Date) -format {%Y-%m-%d %H:%M:%S}]:\n"
			set receipt "$receipt    [format {No. of fiscal receipts:    %5d} $total(Fiscal)]\n"
			set receipt "$receipt    [format {No. of non-fiscal receipts:%5d} $total(Normal)]\n"
            set receipt "$receipt    [format {Total amount:      (%6d)  %9.2f} $Total(CAll) $Total(All)]\n"
            set receipt "$receipt    [format {Total void:        (%6d)  %9.2f} $Total(CVoids) $Total(Voids)]\n"
            set receipt "$receipt    [format {Total abort:       (%6d)  %9.2f} $Total(CAborts) $Total(Aborts)]\n"
            set receipt "$receipt    [format {Total refund:      (%6d)  %9.2f} $Total(CRefunds) $Total(Refunds)]\n"
            set receipt "$receipt    [format {Total adjustments: (%6d)  %9.2f} $Total(CAdjustments) $Total(Adjustments)]\n"
			if {$VAT(1) >= 0} {
				set receipt "$receipt    [format {Total VAT A (%5.2f%%):  %9.2f} $VAT(1) $total(VAT1)]\n"
			}
			if {$VAT(2) >= 0} {
				set receipt "$receipt    [format {Total VAT B (%5.2f%%):  %9.2f} $VAT(2) $total(VAT2)]\n"
			}
			if {$VAT(3) >= 0} {
				set receipt "$receipt    [format {Total VAT C (%5.2f%%):  %9.2f} $VAT(3) $total(VAT3)]\n"
			}
			if {$VAT(4) >= 0} {
				set receipt "$receipt    [format {Total VAT D (%5.2f%%):  %9.2f} $VAT(4) $total(VAT4)]\n"
			}
			printfisc $Training $receipt
			set receipt ""
			if {[paper] == 0} {
				return 0
			}
		}
		incr sums(Fiscal) $total(Fiscal)
		incr sums(Normal) $total(Normal)
		incr sums(CAll) $total(CAll)
		incr sums(CVoids) $total(CVoids)
		incr sums(CAborts) $total(CAborts)
		incr sums(CRefunds) $total(CRefunds)
		incr sums(CAdjustments) $total(CAdjustments)
		set sums(All) [format %4.2f [expr $sums(All) + $total(All)]]
		set sums(Voids) [format %4.2f [expr $sums(Voids) + $total(Voids)]]
		set sums(Aborts) [format %4.2f [expr $sums(Aborts) + $total(Aborts)]]
		set sums(Refunds) [format %4.2f [expr $sums(Refunds) + $total(Refunds)]]
		set sums(Adjustments) [format %4.2f [expr $sums(Adjustments) + $total(Adjustments)]]
		set sums(VAT1) [format %4.2f [expr $sums(VAT1) + $total(VAT1)]]
		set sums(VAT2) [format %4.2f [expr $sums(VAT2) + $total(VAT2)]]
		set sums(VAT3) [format %4.2f [expr $sums(VAT3) + $total(VAT3)]]
		set sums(VAT4) [format %4.2f [expr $sums(VAT4) + $total(VAT4)]]
	}
	set receipt "$receipt  Cumulated values:\n"
	set receipt "$receipt    [format {No. of fiscal receipts:    %5d} $sums(Fiscal)]\n"
	set receipt "$receipt    [format {No. of non-fiscal receipts:%5d} $sums(Normal)]\n"
    set receipt "$receipt    [format {Total amount:      (%6d)  %9.2f} $sums(CAll) $sums(All)]\n"
    set receipt "$receipt    [format {Total void:        (%6d)  %9.2f} $sums(CVoids) $sums(Voids)]\n"
    set receipt "$receipt    [format {Total abort:       (%6d)  %9.2f} $sums(CAborts) $sums(Aborts)]\n"
    set receipt "$receipt    [format {Total refund:      (%6d)  %9.2f} $sums(CRefunds) $sums(Refunds)]\n"
    set receipt "$receipt    [format {Total adjustments: (%6d)  %9.2f} $sums(CAdjustments) $sums(Adjustments)]\n"
	if {$VAT(1) >= 0} {
		set receipt "$receipt    [format {Total VAT A (%5.2f%%):  %9.2f} $VAT(1) $sums(VAT1)]\n"
	}
	if {$VAT(2) >= 0} {
		set receipt "$receipt    [format {Total VAT B (%5.2f%%):  %9.2f} $VAT(2) $sums(VAT2)]\n"
	}
	if {$VAT(3) >= 0} {
		set receipt "$receipt    [format {Total VAT C (%5.2f%%):  %9.2f} $VAT(3) $sums(VAT3)]\n"
	}
	if {$VAT(4) >= 0} {
		set receipt "$receipt    [format {Total VAT D (%5.2f%%):  %9.2f} $VAT(4) $sums(VAT4)]\n"
	}
	printfisc $Training "$receipt[trailerlines Normal 0]"
	if [paper] {
        cutFisc
		return 1
	}
	incr Total(Normal) -1
	updateParamSetter
	return 0
}

# Start non-fiscal receipt
# Return:
#	1 on success, 0 if state blocks command processing.
proc nfStart {} {
	global Period ReceiptState MaxPeriod FiscLineCount
	
	set FiscLineCount 0
	if {[.s.st.b current] > 0 || [.s.ds.b current] > 0 || $Period <= 0 || $Period >= $MaxPeriod || $ReceiptState != 0} {
		return 0
	}
	set ReceiptState 4
	printfisc 1 "[headerlines]\n"
	updateParamSetter
	return [paper]
}

# Print non-fiscal receipt line
# Parameter: Line to be printed.
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc nfPrint {line} {
	global Period ReceiptState MaxPeriod
	
	if {![paper] || $ReceiptState != 4} {
		return 0
	}
	if {[string length $line] > [.t.x cget -width]} {
		return -1
	}
	printfisc 1 "$line"
	updateParamSetter
	return [paper]
}

# Print non-fiscal receipt end
# Parameter print specifies whether trailer lines shall be printed (1) or not (0)
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc nfEnd {print} {
	global Period ReceiptState Total MaxPeriod Blocked
	
	if {$ReceiptState != 4} {
		return 0
	}
	if {[scan $print %d%s p x] != 1} {
		return -1
	}
	set Blocked 0
	if [paper] {
		printfisc 1 "[trailerlines Normal $print]"
	}
	if [paper] {
		cutFisc
		set ReceiptState 0
		updateParamSetter
		return 1
	}
	incr Total(Normal) -1
	updateParamSetter
	return 0
}

set AdditionalTextLine 0

# Print fiscal receipt start
# Return:
#	1 on success, 0 if state blocks command processing.
proc fStart {} {
	global Period ReceiptState Total MaxPeriod Position Training FiscLineCount AdditionalTextLine
	
	set FiscLineCount 0
	if {[.s.st.b current] > 0 || [.s.ds.b current] > 0 || $Period <= 0 || $Period >= $MaxPeriod || $ReceiptState != 0} {
		return 0
	}
	printfisc $Training "[headerlines]"
	if [paper] {
		set ReceiptState 1
		set AdditionalTextLine 0
		array set Position {VAT 1 Amount 0 Adjustments 0 CAdjustments 0 Flag {}}
		if {$Total(Date) == 0} {
			set Total(Date) [clock seconds]
		}
		display 1 ""
		display 2 [format "Subtotal: %10.2f" 0]
		updateParamSetter
		return 1
	}
	return 0
}

# Print additional text line
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc fPrint {text} {
	global ReceiptState Training AdditionalTextLine
	
	if {![paper] || $ReceiptState % 4 == 0 || $AdditionalTextLine >= 3} {
		return 0
	}
	if {[string length $text] > [.t.x cget -width] - 2} {
		return -1
	}
	printfisc $Training "$text"
	if [paper] {
		updateParamSetter
		incr AdditionalTextLine
		return 1
	}
	return 0
}

# Get the current transaction total, rounded to 2-digits.
# Parameter:
#   what    0 (total), 1 - 4 (total for specified VAT rate)
# Return:
#   Requested total on success, -1 if what is invalid.
proc getCurrentTotal {what} {
    global VAT Position CurrentTotal ReceiptState

    if {[set validwhat [lsearch -exact {0 1 2 3 4} $what]] >= 0} {
        if {[lsearch -exact {1 2} $ReceiptState] >= 0} {
            set val $CurrentTotal([lindex {All VAT1 VAT2 VAT3 VAT4} $what])
            if {$what == 0 || $what == $Position(VAT)} {
                if {$Position(Flag) == "" && $ReceiptState == 1} {
                    set val [expr $val + $Position(Amount)]
                } {
                    set val [expr $val - $Position(Amount)]
                }
            }
            return [format %4.2f $val]
        }
        return 0
    }
    error "Invalid parameter: $what, must be one of 0, 1, 2, 3 or 4" 1
}

# Add current position values to current receipt values and store new position values.
# Parameters:
#	amount	New amount value. May be 0 if no new position is available.
#	vat		VAT index of current position. In case of payment position, the remainder or the return.
#	flag	specifies kind of position. May be "Voids", "Refunds" or "".
proc itemPositionToCurrent {amount vat flag} {
	global CurrentTotal Position GrandTotal

	set update 0
	if {$Position(Amount) != 0} {
		if {$Position(Flag) == ""} {
			set CurrentTotal(All) [format %4.2f [expr $CurrentTotal(All) + $Position(Amount)]]
			incr CurrentTotal(CAll)
			set CurrentTotal(VAT$Position(VAT)) [format %4.2f [expr $CurrentTotal(VAT$Position(VAT)) + $Position(Amount)]]
			set CurrentTotal(Adjustments) [format %4.2f [expr $CurrentTotal(Adjustments) + $Position(Adjustments)]]
			incr CurrentTotal(CAdjustments) $Position(CAdjustments)
		} {
			set CurrentTotal(All) [format %4.2f [expr $CurrentTotal(All) - $Position(Amount)]]
			incr CurrentTotal(CAll) -1
			set CurrentTotal(VAT$Position(VAT)) [format %4.2f [expr $CurrentTotal(VAT$Position(VAT)) - $Position(Amount)]]
			set CurrentTotal($Position(Flag)) [format %4.2f [expr $CurrentTotal($Position(Flag)) + $Position(Amount)]]
			incr CurrentTotal(C$Position(Flag))
			set CurrentTotal(Adjustments) [format %4.2f [expr $CurrentTotal(Adjustments) - $Position(Adjustments)]]
			incr CurrentTotal(CAdjustments) -$Position(CAdjustments)
		}
		set GrandTotal [format %4.2f [expr $GrandTotal + $Position(Amount)]]
		set update 1
	}
	if {$amount != 0 || $Position(Amount) != 0} {
		set Position(Amount) $amount
		set Position(VAT) $vat
		set Position(Flag) $flag
		set update 1
	}
	if $update {
		updateParamSetter
	}
}

# Print item position.
# Parameters:
#	text		Item text, max. 20 characters
#	vat			vat index, 1, 2, 3 or 4
#	price		Unit price
#	quantity	Item quantity
#   dimension	quantity name, e.g. kg, cm
#	qdec		quantity decimals, 0 - 3
#	amount		Amount (quantity * price)
#	flag		Voids, Refunds or empty string for normal item position
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc fPrintItem {text vat price quantity dimension qdec amount flag} {
	global ReceiptState Training CurrentTotal AdditionalTextLine VAT At
	
	if {[paper] == 0 || $ReceiptState != 1} {
		return 0
	}
	if {[string length $text] > 20 || $text == ""} {
		return -1
	}
	if {[scan $vat %d%s v x] != 1 || [array names VAT -exact $v] == {} || $VAT($v) < 0} {
		return -2
	}
	if {[scan $price %f%s p x] != 1 || $p < 0 || [format %4.2f $p] != $p} {
		return -3
	}
	if {[scan "$quantity $qdec" %f%d%s q d x] != 2 || $q < 0 || [format "%4.[expr ($d < 0 || $d > 3) ? 0 : $d]f" $q] != $q} {
		return -4
	}
	if {[string length $dimension] >= 6} {
		return -5
	}
	if {$d < 0 || $d > 3} {
		return -6
	}
	if {[scan "$amount" %f%s a x] != 1 || $a < 0 || [format %4.2f $a] != $a || ($flag != "" && $a > $CurrentTotal(VAT$v))} {
		return -7
	}
	if {[lsearch -exact {{} Voids Refunds} $flag] < 0} {
		return -8
	}
	set AdditionalTextLine 0
	itemPositionToCurrent $a $v $flag
	if {$flag != ""} {
		if {$flag == "Voids"} {
			printfisc $Training [centeredLine "**** VOID ****"]
		} {
			printfisc $Training [centeredLine "**** REFUND ****"]
		}
		set a [expr -$a]
		set q [expr -$q]
	}
	if {$quantity != 1} {
		printfisc $Training [format "%1.[set d]f $dimension %s %4.2f EUR" $q $At $p]
	}
	printfisc $Training [format "%-20s%[expr [.t.x cget -width] - 22].2f %s" $text $a [string range " ABCDEFGH" $v $v]]
	updateParamSetter
	set da [format %4.2f $a]
	display 1 [format "%s %s" [string range [format %-20s $text] 0 [expr 20 - [string length $da] - 2]] $da]
	display 2 [format "Subtotal: %10.2f" [expr $CurrentTotal(All) + $a]]
	return [paper]
}

# Print adjustment. After an item position, this is an item adjustment. After subtotal, it is a subtotal adjustment. Keep in mind that
# adjustments onitems with VAT rate 0 are not allowed.
# Parameters:
#	text	Adjustment description
#	amount	Amount of adjustment, either fixed value or percentage. Negative values for discount, positive for surcharge
#	percent	If 0, amount is a fixed value. Otherwise, it is a percentage value and the amount will be computed from previous position
#			(item adjustment) or receipt total (total adjustment). If percent = 1, the amount will be computed as percentage
#           of the position or total: a = v * (1 + p / 100) - v. If percent = 2, the amount will be computed as percentage
#           of the new position or total: a = v / (1 - p / 100) - v. As result, giving a 20% discount with percent = 1, then
#           a 20% surcharge with percent = 2, will compute the original result. This feature is especially important for discount
#           and surcharge void operations.
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc fPrintAdjustment {text amount percent} {
	global ReceiptState CurrentTotal Training Position AdditionalTextLine VAT GrandTotal
	
	if {![paper] || $ReceiptState != 1} {
		return 0
	}
	if {[string length $text] > 20 || $text == ""} {
		return -1
	}
	if {[scan $amount %f%s a x] != 1 || [format %4.2f $a] != $a || $a == 0} {
		return -2
	}
	if {[scan $percent %d%s p x] != 1 || $p < 0 || $p > 2} {
		return -3
	}
	if {$Position(Amount) != 0} {
		# Position adjustment
		if {($a < 0 && (!$p || $a < -100) && ($p || $Position(Amount) + $a <= 0))} {
			return -2
		}
		if $p {
		    if {$p == 1} {
			    set amount [expr ($Position(Amount) * (1 + $a / 100.0)) - $Position(Amount)]
			} {
			    set amount [expr ($Position(Amount) / (1 - $a / 100.0)) - $Position(Amount)]
			}
			set text [format "%-20s (%6.2f%%) " $text $a]			
		} {
			set amount $a
			set text [format "%-20s           " $text]
		}
		if {$VAT($Position(VAT)) == 0 || ($Position(Flag) != "" && $Position(Amount) + $amount >= $CurrentTotal(VAT$Position(VAT)))} {
			return 0
		}
		set AdditionalTextLine 0
		set Position(Amount) [expr $Position(Amount) + $amount]
		set Position(Adjustments) [expr $Position(Adjustments) + ($amount < 0 ? -$amount : $amount)]
		incr Position(CAdjustments)
		updateParamSetter
		set v $Position(VAT)
		set da [format %4.2f [expr [string length $Position(Flag)] ? -$amount : $amount]]
		display 1 [format "%s %s" [string range [format %-20s $text] 0 [expr 20 - [string length $da] - 2]] $da]
		display 2 [format "Subtotal: %10.2f" [expr $CurrentTotal(All) + ([string length $Position(Flag)] ? -$Position(Amount) : $Position(Amount))]]
		printfisc $Training [format "%s%[expr [.t.x cget -width] - [string length $text] - 2].2f %s" $text $da [string range " ABCDEFGH" $v $v]]
	} {
		# Subtotal adjustment
		set subtotal(All) 0
		set count 0
		set AdditionalTextLine 0
		for {set i 1} {[array names VAT -exact $i] != ""} {incr i} {
			if {$VAT($i) > 0} {
				set subtotal(All) [expr $subtotal(All) + $CurrentTotal(VAT$i)]
				incr count
			}
		}
		if {$subtotal(All) == 0} {
			return 0
		}
		if {$a < 0 && (!$p || $a <= -100) && ($p || $subtotal(All) + $a <= 0)} {
			return -2
		}
		if $p {
		    set previoustotal $CurrentTotal(All)
			set CurrentTotal(All) 0
			for {set i 1} {[array names VAT -exact $i] != ""} {incr i} {
				if {$VAT($i) > 0} {
                    if {$p == 1} {
                        set subtotal($i) [expr ($CurrentTotal(VAT$i) * (1 + $a / 100.0)) - $CurrentTotal(VAT$i)]
                    } {
                        set subtotal($i) [expr ($CurrentTotal(VAT$i) / (1 - $a / 100.0)) - $CurrentTotal(VAT$i)]
                    }
					set CurrentTotal(VAT$i) [expr $CurrentTotal(VAT$i) + $subtotal($i)]
					set GrandTotal [format %4.2f [expr $GrandTotal + ($subtotal($i) > 0 ? $subtotal($i) : -$subtotal($i))]]
				}
				set CurrentTotal(All) [expr $CurrentTotal(All) + [format %4.2f $CurrentTotal(VAT$i)]]
			}
			set amount [expr $CurrentTotal(All) - $previoustotal]
			set text [format "%-20s (%6.2f%%) " $text $a]			
		} {
			set amount $a
			for {set i 1} {[array names VAT -exact $i] != ""} {incr i} {
				if {$VAT($i) > 0} {
					incr count -1
					if $count {
						set amount [format %4.2f [expr $amount - [set subtotal($i) [format %4.2f [expr ($CurrentTotal(VAT$i) * $a) / $subtotal(All)]]]]]
					} {
						set subtotal($i) $amount
					}
					set CurrentTotal(VAT$i) [format %4.2f [expr $CurrentTotal(VAT$i) + $subtotal($i)]]
					set CurrentTotal(All) [format %4.2f [expr $CurrentTotal(All) + $subtotal($i)]]
					set GrandTotal [format %4.2f [expr $GrandTotal + ($subtotal($i) > 0 ? $subtotal($i) : -$subtotal($i))]]
				}
			}
			set amount $a
			set text [format "%-20s           " $text]
		}
		set da [format %4.2f $amount]
		set CurrentTotal(Adjustments) [format %4.2f [expr $CurrentTotal(Adjustments)] + ($amount < 0 ? -$amount : $amount)]
		incr CurrentTotal(CAdjustments)
		display 1 [format "%s %s" [string range [format %-20s $text] 0 [expr 20 - [string length $da] - 2]] $da]
		display 2 [format "Subtotal: %10.2f" $CurrentTotal(All)]
		printfisc $Training [format "%s%[expr [.t.x cget -width] - [string length $text] - 2].2f" $text $amount]
		for {set i 1} {[array names VAT -exact $i] != ""} {incr i} {
			if {[array names subtotal -exact $i] != "" && $subtotal($i) != 0} {
				printfisc $Training [format "   VAT [string range " ABCDEFGH" $i $i] (%5.2f%%):%[expr [.t.x cget -width] - 20].2f %s" $VAT($i) $subtotal($i) [string range " ABCDEFGH" $i $i]]
			}
		}
	}
	updateParamSetter
	return [paper]
}

# Subtotal operation.
# Parameter: 0 if subtotal shall be printed, otherwise subtotal will only be displayed
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc fPrintSubtotal {print} {
	global ReceiptState CurrentTotal Training AdditionalTextLine
	
	if {![paper] || $ReceiptState != 1} {
		return 0
	}
	if {[scan $print %d%s p x] != 1} {
		return -1
	}
	set AdditionalTextLine 0
	itemPositionToCurrent 0 1 ""
	display 1 ""
	display 2 [format "Subtotal:%11.2f" $CurrentTotal(All)]
	if $p {
		printfisc $Training [format "Subtotal: %[expr [.t.x cget -width] - 12].2f" $CurrentTotal(All)]
		updateParamSetter
	}
	return [paper]
}

# Total operation. Enters payment mode.
# Return:
#	1 on success, 0 if state blocks command processing.
proc fPrintTotal {} {
	global ReceiptState CurrentTotal Training AdditionalTextLine VAT
	
	if {![paper] || $ReceiptState != 1} {
		return 0
	}
	set AdditionalTextLine 0
    itemPositionToCurrent 0 1 ""
    set CurrentTotal(All) [format %4.2f $CurrentTotal(All)]
    foreach i [array names VAT] {
        set CurrentTotal(VAT$i) [format %4.2f $CurrentTotal(VAT$i)]
    }
	if {$CurrentTotal(All) == 0} {
		incr ReceiptState 2
		display 1 ""
		display 2 "No Sale"
		printfisc $Training "\n[centeredLine {*** No Sale ***}]"
		print "\33d"
	} {
		incr ReceiptState
		display 2 [format "Remaining:%10.2f" $CurrentTotal(All)]
		display 1 [format "Total:%14.2f" $CurrentTotal(All)]
		printfisc $Training [format "\nTotal: %[expr [.t.x cget -width] - 9].2f" $CurrentTotal(All)]
	}
	for {set i 1} {[array names VAT -exact $i] != ""} {incr i} {
		if {$CurrentTotal(VAT$i) > 0} {
			printfisc $Training [format "   VAT [string range " ABCDEFGH" $i $i] (%5.2f%%):%[expr [.t.x cget -width] - 22].2f" $VAT($i) $CurrentTotal(VAT$i)]
		}
	}
	printfisc $Training ""
	updateParamSetter
	return [paper]
}

# Print payment line
# Parameters:
#	text	Payment description, max. 20 characters
#	amount	Amount of payment
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc fPrintPayment {text amount} {
	global ReceiptState CurrentTotal Training Position AdditionalTextLine

	if {![paper] || $ReceiptState != 2} {
		return 0
	}
	if {[string length $text] > 20 || $text == ""} {
		return -1
	}
	if {[scan $amount %f%s a x] != 1 || $a < 0 || $a != [format %4.2f $a] || ($a == 0) != ($CurrentTotal(All) == 0)} {
		return -2
	}
	set AdditionalTextLine 0
	set da [format %4.2f $a]
	display 1 "[string range [format %-20s $text] 0 [expr 19 - [string length $da]]]$da"
	if {[set Position(Amount) [format %4.2f [expr $Position(Amount) + $a]]] >= $CurrentTotal(All)} {
		incr ReceiptState
		if {$Position(Amount) > $CurrentTotal(All)} {
			display 2 [format "Return%14.2f" [expr $Position(Amount) - $CurrentTotal(All)]]
		} {
			display 2 "Balanced"
		}
	} {
		display 2 [format "Remaining:%10.2f" [expr $CurrentTotal(All) - $Position(Amount)]]
	}
	if {$a > 0} {
    	printfisc $Training [format "   %-20s  %[expr [.t.x cget -width] -27].2f" $text $a]
	}
	if {$ReceiptState == 3} {
		print "\33d"
		if {$Position(Amount) > $CurrentTotal(All)} {
			printfisc $Training [format "   Return                %[expr [.t.x cget -width] -27].2f" [expr $Position(Amount) - $CurrentTotal(All)]]
		}
		printfisc $Training ""
	}
	updateParamSetter
	return [paper]
}

# Abort fiscal receipt.
# Parameter: Text describing abort reason
# Return:
#	1 on success, 0 if state blocks command processing, -parameter index in case of invalid parameter.
proc fAbort {text} {
	global ReceiptState Total Training AdditionalTextLine CurrentTotal Blocked

	if {[lsearch -exact {1 2 3} $ReceiptState] < 0} {
		return 0
	}
	if {[string length $text] > [.t.x cget -width]} {
		return -1
	}
	set Blocked 0
	if [paper] {
		printfisc $Training [centeredLine "*** ABORTED ***"]
		if {$text != ""} {
			printfisc $Training [centeredLine $text]
		}
	}
	if [paper] {
		if {$ReceiptState == 1} {
			itemPositionToCurrent 0 1 ""
		}
		if {$Training == 0} {
			set Total(Aborts) [format %4.2f [expr $Total(Aborts) + $CurrentTotal(All)]]
			incr Total(CAborts)
			array set CurrentTotal {All 0 Voids 0 Refunds 0 VAT1 0 VAT2 0 VAT3 0 VAT4 0}
		}
		set ReceiptState 3
		set AdditionalTextLine 0
		updateParamSetter
		return 1
	}
	return 0
}

# Print end of fiscal receipt
# Return:
#	1 on success, 0 if state blocks command processing.
proc fEnd {} {
	global Period ReceiptState Total MaxPeriod CurrentTotal Training
	
	if {![paper] || $ReceiptState != 3} {
		return 0
	}
    printfisc $Training "[trailerlines [lindex {Fiscal Normal} $Training] 1]"
	if [paper] {
		cutFisc
		set ReceiptState 0
		if {[.s.ds.b current] == 0} {
			display 1 ""
			display 2 ""
		} {
			after 1 drawerCheck
		}
		if {$Training == 0} {
			set Total(All) [format %4.2f [expr $Total(All) + $CurrentTotal(All)]]
			set Total(Voids) [format %4.2f [expr $Total(Voids) + $CurrentTotal(Voids)]]
			set Total(Refunds) [format %4.2f [expr $Total(Refunds) + $CurrentTotal(Refunds)]]
			set Total(Adjustments) [format %4.2f [expr $Total(Adjustments) + $CurrentTotal(Adjustments)]]
			incr Total(CAll) $CurrentTotal(CAll)
			incr Total(CVoids) $CurrentTotal(CVoids)
			incr Total(CRefunds) $CurrentTotal(CRefunds)
			incr Total(CAdjustments) $CurrentTotal(CAdjustments)
			set Total(VAT1) [format %4.2f [expr $Total(VAT1) + $CurrentTotal(VAT1)]]
			set Total(VAT2) [format %4.2f [expr $Total(VAT2) + $CurrentTotal(VAT2)]]
			set Total(VAT3) [format %4.2f [expr $Total(VAT3) + $CurrentTotal(VAT3)]]
			set Total(VAT4) [format %4.2f [expr $Total(VAT4) + $CurrentTotal(VAT4)]]
		}
		array set CurrentTotal {All 0 Voids 0 Refunds 0 Adjustments 0 CAll 0 CVoids 0 CRefunds 0 CAdjustments 0 VAT1 0 VAT2 0 VAT3 0 VAT4 0}
		return 1
	}
	incr Total([lindex {Fiscal Normal} $Training]) -1
	updateParamSetter
	return 0
}

# Open the drawer
# Return:
#	1 on success (always)
proc openDrawer {} {
	print "\33d"
	return 1
}

# Set the printer date (dummy, use always system date)
# Return:
#   1 on success (always)
proc setDate {newdate} {
    return 1
}

# Set training mode
# Parameters:
#   mode New rainingmode, 0 (training mode off) or 1 (training mode on)
# Return:
#   1   on success
#   0   mode just set
#   -1  Invalid mode (must be 0 or 1)
proc setTrainingMode {mode} {
    global Training

    if {[lsearch -exact {0 1} $mode] >= 0} {
        if {$Training != $mode} {
            set Training $mode
            return 1
        }
        return 0
    }
    return -1
}

# get printer date
# Return:
#   Date in format YYYYMMDDHHmmss
proc getDate {} {
    return [clock format [clock seconds] -format {%Y%m%d%H%M%S}]
}

proc usage {} {
	puts -nonewline {Available functions:
	display line text
	fStart
	fPrint text
	fPrintItem text vatrate price quantity dimension quantitydecimals amount ""|Voids|Refunds
	fPrintAdjustment text amount percent
	fPrintSubtotal print
	fPrintTotal
	fPrintPayment text amount
	fAbort text
	fEnd
	fiscalize
	get GrandTotal|Till|SerialNo|TaxPayerID|Period ...
	get Total|CurrentTotal|Position|VAT name [name2 ...]
	get TrLine|HdLine index [index2 ...]
	get Memory index name [name2 ...]
	getDate
	getJournalUsed
	nfStart
	nfPrint line
	nfEnd printtrailer
	openDrawer
	printJournal period from to
	report from to sum
	retrieveJournal period from to 0|1
	setCashier number
	setDate YYYYmmddHHMMSS
	setHeaderLine line text
	setTaxPayerID taxid
	setTill number
	setTrailerLine line text
	setTrainingMode mode
	setVatRate index rate
	xReport
	zReport
	}
}

# Returns whether printer is working (1) or not (0)
proc paper {} {
	global Blocked ReceiptState
	
	if {[set ret [expr [.s.st.b current] < 2]] == 0 && [lsearch -exact {1 2 3} $ReceiptState] >= 0} {
		set Blocked 1
	}
	return [expr ($ReceiptState > 0 && $Blocked) ? 0 : $ret]
}

# Get fiscal printer state
proc getState {success} {
	global Period Training Total ReceiptState MaxPeriod Blocked
	
	if {$Period % $MaxPeriod == 0} { set fiscalstate [expr ($Period / $MaxPeriod) * 2] } { set fiscalstate 1}
	if {$Period > $MaxPeriod - 10} { set memstate [expr ($Period / $MaxPeriod) + 1]} { set memstate 0}
	set started [expr $Total(Fiscal) > 0]
	if {[set printerstate [.s.st.b current]] > 3} { set printerstate 3 }
	set drstate [.s.ds.b current]
	set recstate [expr $Blocked ? 5 : $ReceiptState]
	return [format "%d%d%d%d%d%d%d%d" $success $fiscalstate $Training $memstate $started $recstate $printerstate $drstate]
}

# Process input data
proc processData {} {
	global Fd SFd Port Buffer CharTio TioID Total CurrentTotal Position VAT GrandTotal Till Cashier SerialNo TaxPayerID Period Memory TrLine HdLine LastFrameSent

	set ETX [format %c 3]
	set ACK [format %c 6]
	set NAK [format %c 0x15]
	set ETB [format %c 0x17]
	set datacmds {
	    getJournalUsed
	    getDate
	    getCurrentTotal
	}
	set stdcmds {
	    setDate
	    display
	    openDrawer
	    setCashier
	    setHeaderLine
	    setTrailerLine
	    setVatRate
	    setTaxPayerID
	    setTill
	    setTrainingMode
	    fiscalize
	    zReport
	    xReport
	    report
	    nfStart
	    nfPrint
	    nfEnd
	    fStart
	    fPrint
	    fPrintItem
	    fPrintAdjustment
	    fPrintSubtotal
	    fPrintTotal
	    fPrintPayment
	    fAbort
	    fEnd
	    printJournal
	}
	set arrayvars {
        Total
        CurrentTotal
        Position
        VAT
	}
	set vars {
        GrandTotal
        Cashier
        Till
        SerialNo
        TaxPayerID
        Period
	}
	set listvars {
        TrLine
        HdLine
    }
	set arraylistvars {
	    Memory
	}
	
	if {$TioID != {}} {
		after cancel $TioID
		set TioID {}
	}
	if {$Fd != $SFd && [eof $Fd]} {
		fileevent $Fd readable ""
		close $Fd
		set Fd ""
		puts "Got disconnect. Waiting for connect on port $Port"
		return
	}
	if {[catch {read $Fd} c] == 1} {
		puts "Read error : $c"
	} {
		set Buffer "$Buffer$c"
		while {[set nlpos [string first $ETX $Buffer]] >= 0} {
			set cmd [string range $Buffer 0 [expr $nlpos - 1]]
			set Buffer [string range $Buffer [expr $nlpos + 1] end]
			puts "< [join [split $cmd $ETB] " ETB "] ETX"
			set params [lrange [split [validateCheckSum $cmd] $ETB] 0 end-1]
			if {[set parlen [llength $params]] == 0} {
				puts -nonewline $Fd $NAK
				puts "> NAK"
			} {
				puts -nonewline $Fd $ACK
				puts "> ACK"
				if {[lsearch -exact $stdcmds [set cmdname [lindex $params 0]]] >= 0} {
					# Just eval the list
					set res [catch {eval $params} success]
					if {$res == 0} {
						set resp "[getState [expr $success == 1]]"
						if {$success != 1} {
							set resp "$resp$ETB[expr -$success]"
						}
					} {
						set resp "[getState 0]$ETB$res$ETB$success" 
					}
				} elseif {[lsearch -exact $datacmds [set cmdname [lindex $params 0]]] >= 0} {
				    # Just eval the list. Return is requested value.
					set res [catch {eval $params} success]
					if {$res == 0} {
						set resp "[getState 1]$ETB$success"
					} {
					    global errorInfo
						set resp "[getState 0]$ETB[lindex [split $errorInfo " "] 0]$ETB$success"
					}
				} elseif {$cmdname == "get" && $parlen > 1} {
					set varname [lindex $params 1]
					if {[lsearch -exact $arrayvars $varname] >= 0} {
						# Just return param1(param2), param1(param3) ...
						if {$parlen >= 3} {
							set resp "[getState 1]"
							for {set i 2} {$i < $parlen} {incr i} {
    						    set idname [lindex $params $i]
							    if {$varname == "Position" && [lsearch -exact {Amount Adjustments} $idname] >= 0 ||
							        $varname == "CurrentTotal" && [lsearch -exact {VAT1 VAT2 VAT3 VAT4} $idname] >= 0} {
							        set resp "$resp$ETB[format %4.2f [set [set varname]($idname)]]"
							    } {
                                    set res [catch {set "[set varname]([lindex $params $i])"} success]
                                    if {$res == 0} {
                                        set resp "$resp$ETB$success"
                                    } {
                                        set resp "[getState 0]$ETB$i$ETB$success"
                                        break
                                    }
								}
							}
						} {
							set resp "[getState 0]$ETB$parlen$ETB[set success {Too few parameters}]"
						}
					} elseif {[lsearch -exact $vars $varname] >= 0} {
						# Just return variable values
						set resp "[getState 1]"
						for {set i 1} {$i < $parlen} {incr i} {
						    if {[lsearch -exact $vars [set varname [lindex $params $i]]] >= 0} {
						        set resp "$resp$ETB[set $varname]"
						    } {
						        set resp "[getState 0]$ETB$i[set ETB]Invalid variable: $varname"
						        break;
						    }
						}
					} elseif {[lsearch -exact $listvars $varname] >= 0} {
						# Just return param1[param2], param1[param3] ...
						if {$parlen >= 3} {
							set resp "[getState 1]"
							for {set i 2} {$i < $parlen} {incr i} {
								set index [lindex $params $i]
								set res [catch {lindex [set $varname] $index} success]
								if {$res == 0} {
									if {[llength [set $varname]] > $index && $index >= 0} {
										set resp "$resp$ETB$success"
									} {
										set resp "[getState 0]$ETB$i$ETB[set success "Index '$index' out of range"]"
										break
									}
								} {
									set resp "[getState 0]$ETB$i$ETB$success"
									break
								}
							}
						} {
							set resp "[getState 0]$ETB$parlen$ETB[set success {Too few parameters}]"
						}
					} elseif {[lsearch -exact $arraylistvars $varname] >= 0} {
						# Just return param1[param2](param3), param1[param2(param4) ...
						if {$parlen >= 4} {
							set res [catch {
										array set arrayvar [lindex [set $varname] [set index [lindex $params 2]]]
										if {[llength [array names arrayvar]] == 0} {
											error "Index '$index' out of range"
										}
									} success]
							if {$res == 0} {
								set resp "[getState 1]"
								for {set i 3} {$i < $parlen} {incr i} {
									set res [catch {set arrayvar([lindex $params $i])} success]
									if {$res == 0} {
										set resp "$resp$ETB$success"
									} {
										set resp "[getState 0]$ETB$i$ETB$success"
										break
									}
								}
							} {
								set resp "[getState 0]$ETB[expr 2]$ETB$success"
							}
						} {
							set resp "[getState 0]$ETB$parlen$ETB[set success {Too few parameters}]"
						}
					} {
						# Invalid value name
						set resp "[getState 0]$ETB[expr 0]$ETB[set success {Unsupported value name }'$varname']"
					}
				} elseif {$cmdname == "lastResponse"} {
					set resp $LastFrameSent
				} elseif {$cmdname == "retrieveJournal"} {
					global JournalData
					
					if {$parlen == 5} {
						set res [catch {eval $params} success]
						if {$res == 0} {
							if {$success != 1} {
								set resp "[getState 0]$ETB[expr -$success]"
							} {
								set resp "[getState 1]$ETB[llength $JournalData]$ETB[join $JournalData $ETB]"
							}
						} {
							set resp "[getState 0]$ETB$res$ETB$success" 
						}
					} {
						set resp "[getState 0]$ETB$parlen$ETB[set success {Wrong number of parameters}]"
					}
				} {
					# Invalid command
                    set resp "[getState 0]$ETB$parlen$ETB[set success {Invalid command }]'$cmdname'"
				}
				set LastFrameSent $resp
				set resp [addCheckSum "$resp$ETB"]
				puts -nonewline $Fd "$resp$ETX"
				puts "> [join [split $resp $ETB] " ETB "] ETX"
			}
		}
		if {$Buffer != ""} {
			set TioID [after $CharTio charTio]
		}
	}
}

# Add a checksum to a frame to be sent. Must not include ACK 
proc addCheckSum {data} {
	set datalen [string length [set data [encoding convertto utf-8 $data]]]
	set sum 0
	set etx 3
	for {set i 0} {$i < $datalen} {incr i} {
		scan [string range $data $i $i] %c val
		if {$val == $etx} break
		incr sum $val
	}
	return "[string range $data 0 [expr $i - 1]][format %c [expr 127 - ($sum % 96)]][string range $data $i end]"
}

# Validate a frame using its checksum.
proc validateCheckSum {data} {
	set datalen [string length $data]
	set sum 0
	set etx 3
	for {set i 0} {$i < $datalen} {incr i} {
		scan [string range $data $i $i] %c val
		if {$val == $etx} break
		incr sum $val
	}
	if {$sum % 96 == 31} {
		return [encoding convertfrom utf-8 [string range $data 0 [expr $i - 2]]]
	}
	return ""
}

# Character timeout handler
proc charTio {} {
	global TioID Buffer
	
	puts "< $Buffer TIMEOUT"
	set TioID [set Buffer {}]
}

#
# For testing, only TCP
#

set F {}

proc starttest {} {
	global F Port

	set F [socket localhost $Port]
	fconfigure $F -blocking 0 -buffering none -translation binary -encoding binary
}

proc send {args} {
	global F
	
	puts -nonewline $F "[addCheckSum "[join $args "\27"]\27"]\3"
}

proc recv {} {
	global F

	set frames [split [read $F] "\3"]
	if {[lindex $frames end] == {}} {
		set frames [lrange $frames 0 end-1]
	}
	foreach part $frames {
		while {[scan $part %c val] && $val == 0x15} {
			puts "Got NAK"
			set part [string range $part 1 end]
		}
		if {$val == 6} {
			puts "Got ACK, [join [split [validateCheckSum [string range $part 1 end]] "\27"] ", "]ETX"
		} {
			puts "Got invalid data, use 'lastResponse' to retrieve result."
		}
	}
}

proc sendrecv {args} {
	eval send $args
	after 1000 recv
}