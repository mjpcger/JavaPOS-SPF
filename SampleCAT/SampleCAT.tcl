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

# Simulator for an EFT device, for handling with a CAST service. Communication happens
# via TCP service port. A description of all supported frames can be found near the
#end of this script, at procedure processInput.

set Port 54545

set cardno ""
set expdate ""
set tip "0.00"
set ident 1

set line0 ""
set line1 ""
set line2 ""
set line3 ""

set result -1
set appresult -1
set transno 12345
set issuer ""

set Status 1
set CardState 0

set IssuerList {{CHUP {China UnionPay}} {AMEX {American Express}} {DICL {Diners Club}} {DISC {Discover Card}} {JCB JCB} {LASR Laser} {MAES Maestro} {DCRD Dankort} {MSTR MasterCard} {VISE {Visa Electron}} {VISA Visa}}
set TransList {}

set PrintLineLen 28

set SFd ""
set Fd ""
set Buffer {}

# Communication
pack [ttk::labelframe .co -text Setup] -fill both
pack [ttk::labelframe .co.port -text "Port"] -fill y -side left
pack [ttk::entry .co.port.e -textvariable Port -width 5] -fill both -side left
pack [ttk::button .co.debug -text "Debug On" -command debug] -fill y -side left
pack [ttk::button .co.startstop -text "Start" -command startstop] -expand 1 -side left -fill both
# Terminal
pack [ttk::labelframe .di -text Display] -fill both
font create displayfont -family Courier -size 14 -weight bold
for {set i 0} {$i < 4} {incr i} {
	pack [ttk::entry .di.en$i -textvariable line$i -font displayfont -width 40 -state readonly] -fill y -anchor w
}
# Card
pack [ttk::labelframe .cr -text "Card data"] -fill both -expand 1
font create cardfont -family Courier -size 10 -weight bold
pack [ttk::frame .cr.f] -expand 1 -fill both
pack [ttk::labelframe .cr.f.cn -text "Card No."] -fill both -side left
pack [ttk::entry .cr.f.cn.e -textvariable cardno -font cardfont -width 16 -validate key -validatecommand {validatelen 16 %P 1}] -fill both
pack [ttk::labelframe .cr.f.xd -text "ExDt"] -fill both -side left
pack [ttk::entry .cr.f.xd.e -textvariable expdate -font cardfont -width 4 -validate key -validatecommand {validatelen 4 %P 1}] -fill both
pack [ttk::labelframe .cr.f.tp -text "Tip"] -fill both -side left
pack [ttk::entry .cr.f.tp.e -textvariable tip -font cardfont -width 5 -justify right -validate key -validatecommand {validatelen 5 %P 0}] -fill both
pack [ttk::label .cr.f.lb -text "" -font cardfont -width 16 -anchor s] -fill both -side left
pack [ttk::button .cr.f.va -text "Validate" -command CardValidation] -expand 1 -fill both -side left
pack [ttk::frame .cr.g] -expand 1 -fill both
pack [ttk::labelframe .cr.g.op -text "Identification"] -side left -fill both
pack [ttk::checkbutton .cr.g.op.sg -text "With PIN" -variable ident] -side left -fill both
pack [ttk::menubutton .cr.g.mb -text Result -menu .cr.g.mb.m] -side left -fill both
menu .cr.g.mb.m -title "Select a result from the list"
.cr.g.mb.m add command -label "Success" -command {setResult OK}
.cr.g.mb.m add command -label "Cancelled" -command {setResult Cancelled}
.cr.g.mb.m add command -label "Card Locked" -command {setResult Locked}
.cr.g.mb.m add command -label "Retain Card"  -command {setResult DrawCard}
.cr.g.mb.m add command -label "Card Error" -command {setResult Error}
.cr.g.mb.m add command -label "Approval Error" -command {setResult ApError}
pack [ttk::button .cr.g.bt -text Finish -command doIt] -side left -expand 1 -fill both

proc sourceText {tx} {
	return [encoding convertfrom utf-8 [encoding convertto [encoding system] $tx]]
}

proc doIt {} {
	global line3 result appresult cardno expdate tip CardState Status line1 transno issuer

	if {$Status > 3} {
		if {$CardState == 0} CardValidation
		if {$CardState == 1} {
			if {[.cr.g.mb.m entrycget 1 -state] == "normal"} {
				setResult OK
			} {
				setResult Cancelled
			}
		}
		set endframe [finalizeTransaction $Status $transno $result $appresult $cardno $expdate $issuer $line1 $tip [clock seconds]]
		set cardno ""
		set expdate ""
		set tip ""
		if {$result == 0 && $appresult != 0 && $Status == 4} {
			setLine 2 "Verify Signature"
			setLine 3 "** Check Sign **"
			set Status 2
		} {
			setLine 2 "Remove Card"
			set Status 1
			if {$result != 0} {
				setLine 3 "*** ABORTED ***"
			} {
				setLine 3 "*** SUCCESS ***"
			}
		}
        endTransaction
        sendFrame $endframe
	}
}

proc endTransaction {} {
	after 3000 {
		global Status
		
		if {$Status == 1} {
			setLine 0 ""
			setLine 1 ""
			setLine 2 ""
			setLine 3 "*** READY ***"
		}
	}
}

proc setResults {res appres} {
	global result appresult
	set result $res
	set appresult $appres
}

proc sendFrame {frame} {
	global Fd
	
	catch {
		puts -nonewline $Fd [encoding convertto utf-8 $frame]
		dumpframe [sourceText {►}] $frame
	}
}

proc dumpframe {direction frame} {
	set cmd "[string range $frame 0 0]"
	set data [join [split [join [split [string range $frame 1 end-1] "\n"] {<LF>}] "\2"] {<STX>}]
	puts "$direction <$cmd>$data<ETX>"
}

proc setLine {no text} {
	global line$no
	
	if {[set line$no] != $text} {
		set line$no $text
		sendFrame [format "D%1d\2%s\3" $no $text]
	}
}

proc setTicket {count ticket} {
	sendFrame [format "P%1d\2%s\3" $count $ticket]
}

proc setResult {res} {
	global result appresult transno ident line2 line1 tip CardState
	
	if {$CardState == 1} {
		setLine 2 "Waiting..."
		incr CardState
		if {$tip == ""} {
			set tip "0.00"
		}
		switch $res OK {
			.cr.g.mb configure -text "Success"
			if {$ident == 1 || $line1 < 25.00 - $tip} {
				setResults 0 0
				if {$ident == 1} {
					setLine 2 "Enter PIN"
					after 5000 {
						global line2
						
						if {$line2 == "Enter PIN"} {
							setLine 2 "Waiting..."
						}
					}
				}
			} {
				setResults 0 1111
			}
			incr transno
		} Cancelled {
			.cr.g.mb configure -text "Cancelled"
			setLine 2 "Abort By User"
			setResults 100 0
		} Locked {
			.cr.g.mb configure -text "Card Locked"
			setLine 2 "Card locked"
			setResults 101 1234
		} DrawCard {
			.cr.g.mb configure -text "Retain Card"
			setLine 2 "Retain Card"
			setResults 102 5678
		} Error {
			.cr.g.mb configure -text "Card Error"
			setLine 2 "Card Error"
			setResults 103 0
		} ApError {
			.cr.g.mb configure -text "Approval Error"
			setLine 2 "Approval Error"
			setResults 104 9012
		}
	}
}

proc CardValidation {} {
	global cardno expdate type issuer CardState Status IssuerList

	if {$Status > 2} {
		set issuer ""
		if {[string length $cardno] == 16 && [string length $expdate] == 4} {
			set type1 [expr $cardno / 1000000000000000]
			set type2 [expr $cardno / 100000000000000]
			set type4 [expr $cardno / 1000000000000]
			# The following is a guess, it's not exact.
			if {[lsearch -exact {62 88} $type2] >= 0} {
				set issuer "CHUP"
			} elseif {[lsearch -exact {34 37} $type2] >= 0} {
				set issuer "AMEX"
			} elseif {[lsearch -exact {30 36 38 39} $type2] >= 0} {
				set issuer "DICL"
			} elseif {[lsearch -exact {60 62 64 65} $type2] >= 0} {
				set issuer "DISC"
			} elseif {$type2 == 35} {
				set issuer "JCB"
			} elseif {[lsearch -exact {6304 6706 6771 6709} $type4] >= 0} {
				set issuer "LASR"
			} elseif {[lsearch -exact {5018 5020 5038 5612 5893 6304 6759 6761 6762 6763 0604 6390} $type4] >= 0} {
				set issuer "MAES"
			} elseif {$type4 == 5019} {
				set issuer "DCRD"
			} elseif {[lsearch -exact {50 51 52 53 54 55} $type2] >= 0} {
				set issuer "MSTR"
			} elseif {[lsearch -exact {4026 417500 4405 4508 4844 4913 4917} $type4] >= 0} {
				set issuer "VISE"
			} elseif {$type1 == 4} {
				set issuer "VISA"
			}
			if {$issuer != ""} {
				.cr.f.lb configure -text [lindex [lsearch -index 0 -inline $IssuerList $issuer] 1]
				scan $expdate "%2d%2d" month year
				scan [clock format [clock seconds] -format %m%y] "%2d%2d" actmonth actyear
				if {(($year > $actyear && $actyear >= $year - 20) || ($year == $actyear && $month >= $actmonth)) && $month >= 1 && $month <= 12 } {
					set CardState 1
					.cr.g.mb.m entryconfigure 1 -state normal
					setLine 2 "With [.cr.f.lb cget -text]..."
					return 0
				} elseif {$month < 1 || $month > 12} {
					setLine 2 "Invalid Expiration Date"
				} {
					setLine 2 "Card Expired"
				}
			} {
				setLine 2 "Card Unknown"
			}
		} {
			setLine 2 "No Or Unknown Card"
		}
		set CardState 1
		.cr.g.mb.m entryconfigure 1 -state disabled
	}
	return 1
}

proc validatelen {len value integer} {
	if {$value == ""} {
		return 1
	} {
		if {[string length $value] <= $len} {
			switch -glob "0$value" {*[-+e]*} {
				return 0
			}
			if {[catch {
				if {$integer == 0} {
					set retval [expr $value + 0 >= 0]
				} {
					set retval [expr "1$value" % 1000000000 >= 0] 
				}
			}] == 0} {
				return $retval
			}
		}
	}
	return 0
}

proc lock {lock} {
	global Status
	
	setLine 0 ""
	setLine 1 ""
	setLine 2 ""
	if $lock {
		if {$Status > 0} {
			setLine 3 "*** LOCKED ***"
			sendFrame "L0\3"
			set Status 0
		} {
			sendFrame "L4\3"		
			return 1
		}
	} {
		if {$Status == 0} {
			setLine 3 "*** READY ***"
			sendFrame "U0\3"
			set Status 1
		} {
			sendFrame "U4\3"
			return 1
		}
	}
	return 0
}

proc startTransaction {} {
	global cardno expdate tip Status
	
	if {$Status == 1} {
		setLine 0 "Transaction Active"
		setLine 1 "0.00"
		setLine 2 "Swipe Card"
		setLine 3 "*** Waiting... ***"
		set cardno ""
		set expdate ""
		set tip "0.00"
		.cr.f.lb configure -text ""
		.cr.g.mb configure -text "Result"
		set Status 3
		sendFrame "B0\3"
		return 0
	}
	sendFrame [format "B%d\3" [expr $Status == 0 ? 4 : ($Status == 2 ? 6 : 7)]]
	return 1
}

proc startSale {amount} {
	global ident line1 result appresult tip Status

	if {$Status == 3} {
		set Status 4
		set ident 1
		setLine 0 "Sale Transaction"
		setLine 1 [format %4.2f $amount]
		return 0
	}
	sendFrame [format "E%d\3" [expr $Status == 0 ? 4 : ($Status == 1 ? 5 : ($Status == 2 ? 6 : 7))]]
	return 1
}

proc startReturn {amount} {
	global ident line1 result appresult tip Status

	if {$Status == 3} {
		set Status 5
		set ident 2
		setLine 0 "Refund Transaction"
		setLine 1 [format %4.2f $amount]
		return 0
	}
	sendFrame [format "E%d\3" [expr $Status == 0 ? 4 : ($Status == 1 ? 5 : ($Status == 2 ? 6 : 7))]]
	return 1
}

proc startVoid {transact} {
	global ident line1 result appresult tip Status cardno expdate TransList

	if {[set index [lsearch -index 0 $TransList [format {%08d} $transact]]] >= 0 && $Status == 3 && $cardno == "" && $expdate == ""} {
		set Status 6
		set ident 2
		setLine 0 "Void Transaction"
		lassign [lindex $TransList $index] transno result status id exp issuer amount tip
		if {$result < 0} {
			setLine 1 ""
			setLine 2 "Invalid transaction"
			setLine 3 "*** ABORTED ***"
			set index -1
		} {
			set endframe [finalizeTransaction $status $transno -1 $index $id $exp $issuer $amount $tip [clock seconds]]
			setLine 1 [format %4.2f $amount]
			setLine 2 ""
			setLine 3 "*** SUCCESS ***"
			set Status 1
			sendFrame $endframe
			return 0
		}
	}
	set Status 1
	sendFrame [format "E%d\3" [expr $Status == 0 ? 4 : ($Status == 1 ? 5 : ($Status == 2 ? 6 : ($index < 0 ? 8 : 7)))]]
	return 1
}

proc header {} {
	# Set ticket header here. Lines must not start or end with spaces (' '), tabs are allowed
	set ret "[printcentered {Sample Market}]"
	set ret "$ret\n[printcentered {Sample Street 1}]"
	set ret "$ret\n[printcentered {12345 Sample Town}]"
	set ret "$ret\n[printcentered {Fiscal ID 1234-XY-5678}]\n"
	return $ret
}

proc trailer {} {
	# Trailer is currently only one line containing date and time.
	return printcentered [clock format [clock seconds] -format "%Y-%m-%d %H:%M:%S"]
}

proc printline {leftpart rightpart} {
	global PrintLineLen
	if {[string length "$leftpart $rightpart"] >= $PrintLineLen} {
		return "$leftpart $rightpart"
	}
	return [format "%s%[expr $PrintLineLen - [string length $leftpart]]s" $leftpart $rightpart]
}

proc printcentered {line} {
	global PrintLineLen
	return [format "%[expr ($PrintLineLen + [string length $line]) / 2]s" $line]
}

proc reformat {format value} {
    if {$value == ""} {
        set value 0
    }
    scan $value "%d" x
    return [format $format $x]
}

proc maskCardNo {cardno} {
    if {$cardno == ""} {
        set cardno 0
    }
    return [expr $cardno % 10000]
}

proc finalizeTransaction {status transno result appresult cardno expdate issuer amount tip now} {
	global TransList IssuerList
	
	if {$result == -1} {
		lset TransList "$appresult 1" -1
		set void 1
	} {
		lappend TransList [list [reformat {%08d} $transno] $appresult $status $cardno $expdate $issuer $amount $tip $now]
		set void 0
	}
	set ticket [header]
	if $void {set ticket "$ticket\n[printcentered {**** VOID ****}]"}
	switch $status 4 {set ticket "$ticket\n[printcentered {D E B I T}]"} 5 {set ticket "$ticket\n[printcentered {C R E D I T}]"}
	set ticket "$ticket\n[printline {Amount:} [format {%4.2f EUR} $amount]]"
	if {$tip > 0} {
		set ticket "$ticket\n[printline {Tip:} [format {%4.2f EUR} $tip]]"
		set ticket "$ticket\n[printline {Balance:} [format {%4.2f EUR} [expr $amount + $tip]]]"
	}
	set ticket "$ticket\n[printline {Card Issuer:} [lindex [lsearch -index 0 -inline $IssuerList $issuer] 1]]"
	set ticket "$ticket\n[printline {Card No:} [format {XXXX XXXX XXXX %04d} [maskCardNo $cardno]]]"
	set ticket "$ticket\n[printline {Expiration Date:} [reformat {%04d} $expdate]]"
	set ticket "$ticket\n[printline {Transaction No.:} [reformat {%08d} $transno]]"
	set ticket "$ticket\n[printline {Transaction Date:} [clock format $now -format {%Y-%m-%d}]]"
	set ticket "$ticket\n[printline {Transaction Time:} [clock format $now -format {%H:%M:%S}]]"
	if {$result <= 0} {
		set ticket "$ticket\n[printline {Transaction State:} SUCCESSFUL]"
	} {
		set ticket "$ticket\n[printline {Transaction State:} FAILED]"
	}
	set ticket "$ticket\n\n[trailer]"
	setTicket [expr $result == 0 && $appresult == 0 && $status == 4 ? 1 : 2] $ticket
	set frame [format "E%d\2%s\2%s\2%s\2%s\2%s\2%s\2%s\2%s\2%s\3" [expr $result <= 0 ? ($appresult == 0 ? 0 : 1) : 2]\
		[expr $result < 0 ? 0 : $result]\
		[expr $result < 0 ? 0 : $appresult]\
		[expr $amount + $tip]\
		$tip\
		$issuer\
		[format "%016d" [maskCardNo $cardno]]\
		[reformat "%04d" $expdate]\
		[reformat "%08d" $transno]\
		[clock format $now -format "%Y%m%dT%H%M%S"]\
	]
	return $frame
}

proc startAbort {} {
	global Status TransList

	if {$Status == 2} {
		set index [expr [llength $TransList] - 1]
		lset TransList "$index 1" -1
	}
	if {$Status > 1} {
		setLine 0 ""
		setLine 1 ""
		setLine 2 "Abort by Cashier"
		setLine 3 "*** ABORTED ***"
		sendFrame "E3\3"
		set Status 1
		endTransaction
	}
}

proc startCommit {transno committed} {
	global Status TransList
	
	if {$Status == 2} {
		set index [lsearch -index 0 $TransList [reformat {%08d} $transno]]
		if {$index >= 0 && $index == [llength $TransList] - 1 && [lindex $TransList "$index 1"] > 0} {
			if $committed {
                setLine 2 "Verified"
				setLine 3 "*** SUCCESS ***"
				sendFrame "E0\3"
			} {
                setLine 2 "Verification failed"
				setLine 3 "*** ABORTED ***"
				lset TransList "$index 1" -2
				sendFrame "E2\3"
			}
		} {
			sendFrame "E8\3"
		}
		set Status 1
		endTransaction
		return 0
	}
	sendFrame [format "E%d\3" [expr $Status == 0 ? 4 : ($Status == 1 ? 5 : 7)]]
	return 1
}

proc debug {} {
	if [string equal [.co.debug cget -text] "Debug On"] {
		.co.debug configure -text "Debug Off"
		console show
		raise .
	} {
		.co.debug configure -text "Debug On"
		console hide
	}
}

proc startstop {} {
	global Port SFd Fd
	
	if [string equal [.co.startstop cget -text] "Start"] {
		if [catch {expr 1 / ($Port > 0 && $Port < 0xffff)}] {
			tk_messageBox -title "Startup error" -message "Invalid port $Port: Must be a numeric value between 1 and 65534" -icon error -type ok
		} {
			if [catch {socket -server service $Port} SFd] {
				tk_messageBox -title "Startup error" -message "Server could not be created, reason: $SFd" -icon error -type ok
				set SFd ""
			} {
				.co.startstop configure -text "Stop"
				puts "Waiting for connect on port $Port..."
			}
		}
	} {
		.co.startstop configure -text "Start"
		catch {close $SFd}
		catch {close $Fd}
		set SFd [set Fd ""]
	}
}

proc service {fd addr port} {
	global SFd Fd
	
	if {$Fd == ""} {
		puts "Connect from $addr:$port accepted, fd = $fd"
		set Fd $fd
		fconfigure $fd -blocking 0 -buffering none -translation binary -encoding binary
		fileevent $Fd readable {processInput}
	} {
		puts "Connect from $addr:$port rejected"
		close $fd
	}
}

# Command handler. Each command will be terminated with ETX (03h). Commands must be formatted as follows:
#	The following format specifications follow the format of Tcl/Tk format command (which is compatible to
#	the printf format of C):
#		p%d\3			Set print line width. Parameters: Line width (must be >= 28).
#		l%d\3			Lock or unlock terminal. Parameters: 0: unlock, 1: lock
#		b\3				Begin transaction. s, v or r must follow.
#		s%f\3			Set sale amount. Parameters: Amount.
#		c%d\2%d\3		Commit operation. Parameters: No. of transaction to be committed, result (0: Verification
#						error, 1: Signature verified). Mandatory after sign-based sale operations.
#		r%f\3			Set return amount. Parameters: Amount.
#		v%d\3		    Void transaction. Parameters: No. of transaction to be voided.
#		a\3				Abort operation.

# Frames sent by the simulator will be terminated with ETX (03h). They have one of the following formats:
# 	D%d\2%s\3									Display line. Parameters: line no (0-3), contents (UTF-8).
#	P%d\2%s\3									Print ticket. Parameters: count (1-2), ticket data (UTF-8), may
#												contain line feeds.
#	L%d\3										Lock terminal. Parameters: Result code (0: OK, 4: just locked).
#	U%d\3										Unlock terminal. Parameters: Result code (0: OK, 4: just unlocked).
#   B%d\3                                       Begin transaction. Parameters: Result code (0: OK, 4: just locked,
#                                               6: waiting for commit, 7: authorization active)
#	E%d\3										End. Parameters: Result code (0: OK, 3: Abort, 4: locked, 5: no
#												transaction, 6: wait for commit, 7: other operation active,
#												8: invalid transaction).
#	E%d\2%s\2%s\2%s\2%s\2%s\2%s\2%s\2%s\2%s\3 	End processing. Parameters: Result code (0: OK, 1: wait for commit,
#												2: Error), Result code (0: no error), approval result (0: OK, 1111:
#												check, else error), balance, tip (included in balance), card issuer
#												(abbreviation, see IssuerList), card no (last 4 digits), expiration
#												date, transaction number, transaction time (format YYYYmmddTHHMMSS).

proc processInput {} {
	global Fd Buffer
	
	if [eof $Fd] {
		fileevent $Fd readable ""
		close $Fd
		set Fd ""
		puts "Connection disconnected. Waiting for connect"
	} {
		set Buffer [lindex [set commands [split "$Buffer[read $Fd]" "\3"]] end]
		set commands [lrange $commands 0 end-1]
		foreach cmd $commands {
			set frame [encoding convertfrom utf-8 $cmd]
			dumpframe [sourceText {◄}] "$frame\3"
			set what [string range $frame 0 0]
			set params [split [string range $frame 1 end] "\2"]
			switch $what l {
				# Lock / Unlock
				lassign $params flag
				
				if {[llength $params] == 1 && [scan $flag "%d" lock] == 1 && 0 <= $lock && $lock <= 1} {
					lock $lock
					continue
				}
			} b {
				# Begin Transaction
				if {[llength $params] == 0} {
					startTransaction
					continue
				}
			} s {
				# Set sale amount
				lassign $params amount
				
				if {[llength $params] == 1 && [scan $amount "%f" value] == 1 && $value > 0} {
					startSale $value
					continue
				}
			} r {
				# Set return amount
				lassign $params amount
				
				if {[llength $params] == 1 && [scan $amount "%f" value] == 1 && $value > 0} {
					startReturn $value
					continue
				}
			} a {
				# Abort operation
				if {[llength $params] == 0} {
					startAbort
					continue
				}
			} v {
				# Void transaction
				lassign $params transact
				
				if {[llength $params] == 1 && [scan $transact "%d" transno] == 1 && 0 <= $transno && $transno < 100000000} {
					 startVoid $transno
					 continue
				}
			} c {
				# Commit transaction
				lassign $params transact flag
				
				if {[llength $params] == 2 && [scan $transact "%d" transno] == 1 && [scan $flag "%d" ok] == 1 && 0 <= $transno && $transno < 100000000 && 0 <= $ok && $ok <= 1} {
					 startCommit $transno $ok
					 continue
				}
			} p {
				# Set print line width
				lassign $params linewidth
				if {[llength $params] == 1 && [scan $linewidth "%d" lw] == 1 && $lw >= 28} {
					global PrintLineLen
					
					set PrintLineLen $lw
					continue
				}
			}
			# Unknown command
			puts "******* INVALID COMMAND *******"
		}
	}
}

lock 1

###########################################################################
# Helper procs and variables for self-testing
set cfd ""
proc connectme {} {
	global cfd Port
	
	if {[catch {socket localhost $Port} cfd] == 0} {
		fconfigure $cfd -blocking 0 -buffering none -translation binary -encoding binary
		fileevent $cfd readable getme
	}
}
proc sendme {cmd args} {
	global cfd
	catch {
		puts -nonewline $cfd "$cmd[join $args "\2"]\3"
		puts "[sourceText {»}] <$cmd>[join $args "<STX>"]<ETX>"
	}
}
proc getme {} {
	global cfd
	if [eof $cfd] {
		fileevent $cfd readable {}
	} {
		foreach frame [split [read $cfd] "\3"] {
			if {$frame != ""} {
				puts "[sourceText {«}] <[string range $frame 0 0]>[join [split [join [split [string range $frame 1 end] "\n"] {<LF>}] "\2"] {<STX>}]<ETX>"
			}
		}
	}
}
proc endme {} {
	global cfd
	close $cfd
}
