
/* can used to hide elements if data set is empty */
function isColumnPresent(col) {
    if (row[col] == null){
    	return false;
    }
    else{
    	return true;
    }
}

/* can used to hide elements if data cube is empty */
function isDataPresent(col) {
    if (data[col] == 0) {//trial and error ;-)
	return false;
    }
    else{
	return true;
    }
}

function getUPos(pos)  {
    /* -2 is above, move very high */
    /* -9 (unplaced) and -1 (below) should be fine with default sort */
    return pos != -2 ? pos : 10000;
}
var MAX_SUPPORTED_SUPPLIES = 8;


/*
 * we can not set the style name dynamicly :-(
 */
/*
.itemNameNACell {
	background-color: #00ffff;
}
.itemNameNotConnectedCell {
	background-color: red;
}
.itemNameCircuitRedundancyCell {
	background-color: orange;
}
.itemNamePowerSourceCell {
	background-color: yellow;
}
*/
/* power supply helper redundancy*/
function setPSupplyRedunItemNameColor(cell) {
    var redundancy = row["redundancy"];
    var qty = row["ps_qty"];
    var connected = getPSConnectedCount();
    /*
     * if redundancy is out of balance or not all ports are connected 
     * we will indicate this via Qty and/or Redundancy column color schema
     */
    if (redundancy >= qty/2 && connected == qty) {
        /* 
         * count connected ports per power supply item
         * use an Object as an associative array
         */
        var i = 0;
        var rpdus = new Object();
        var brkrs = new Object();
        var ups_banks = new Object();
        
        for (i = 0; i < MAX_SUPPORTED_SUPPLIES; i++) {
            setPSupplyRedunItemNameColorCountHelper(row["ups_id_" + String(i)], ups_banks);
            if (!setPSupplyRedunItemNameColorCountHelper(row["ps_input_cord_id_" + String(i)], rpdus)) {
        	setPSupplyRedunItemNameColorCountHelper(row["ps_brkr_port_id_" + String(i)], brkrs);
            }
        }
        /*
         * now look if one causes a redundancy problem
         */
        for (var ups_item in ups_banks ) {
            var ps_ports = ups_banks[ups_item];
            if (ps_ports > redundancy) {
        	for (var ps_item in rpdus ) {
        	    ps_ports = rpdus[ps_item];
        	    if (ps_ports > redundancy) {
        		cell.getStyle().backgroundColor = "red";//ups and item aren't redundant
        		return;
        	    }
        	}
        	for (var ps_item in brkrs ) {
        	    ps_ports = brkrs[ps_item];
        	    if (ps_ports > redundancy) {
        		cell.getStyle().backgroundColor = "red";//ups and item aren't redundant
        		return;
        	    }
        	}
        	cell.getStyle().backgroundColor = "orange";//just ups isn't redundant
        	return;
            }
        }
        cell.getStyle().backgroundColor = "yellow";//all is redundant
    } else if (redundancy >= qty/2 && connected < qty) {//go and connect your ports
	cell.getStyle().backgroundColor = "red";
    } else {//incorrect
	cell.getStyle().backgroundColor = "#00ffff";
    }
}

function setPSupplyRedunItemNameColorCountHelper(item, collection) {
    if (item != null) {
	if (collection[item] == null) collection[item] = 1;//first of this item
	else collection[item]++;
	return true;
    }
    return false;
}

function getPSConnectedCount() {
    var count = 0;
    for ( var i = 0; i < MAX_SUPPORTED_SUPPLIES; ++i) {
	if (row["ps_port_name_"+ String(i)]) ++count;
    }
    return count;
}

function allPSConnected() {
    return row["ps_qty"] == getPSConnectedCount();
}

/* power supply helper by model*/
function getRedunCapacity(qty, redun, watts) {
    if (redun == null || redun == '') redun = 0;
    return watts * (qty - redun);
}

//power panel report
function getVolts(phases, phase_volt, line_volts) {
    if (phases == 1) return phase_volt;
    return line_volts;
}

function getBPRowHeight() {
    poles = row["poles"] ? row["poles"] : 1;
    return String(poles * 20) + "px";
}

function getPhaseConImg(offset, secondhalf) {
    poles = row["poles"] ? row["poles"] : 1;
    if (secondhalf) {
	offset -= reportContext.getGlobalVariable("current_poles")/2;
    }
    rest = (row["running_poles"] - poles + offset) % 3;
    if (rest == 1) return "phaseA-con.png";
    else if (rest == 2) return "phaseB-con.png";
    else return "phaseC-con.png";
}

function getPhaseImg() {
    rest = row["pos"] % 3;
    if (rest == 1) return "phaseA.png";
    else if (rest == 2) return "phaseB.png";
    else return "phaseC.png";
}

function getPhaseLeftImg() {
    rest = row["pos"] % 3;
    if (rest == 1) return "phaseA-left.png";
    else if (rest == 2) return "phaseB-left.png";
    else return "phaseC-left.png";
}

function getPhaseRightImg() {
    rest = row["pos"] % 3;
    if (rest == 1) return "phaseA-right.png";
    else if (rest == 2) return "phaseB-right.png";
    else return "phaseC-right.png";
}

/* power path */
function calcAmpsA(a, ab, ca, transform_factor) {
    if (transform_factor == null) transform_factor = 1;
    return formatNumber2((a + Math.sqrt(ab * ab + ca * ca + ab * ca)) * transform_factor);
}

function calcAmpsB(b, bc, ab, transform_factor) {
    if (transform_factor == null) transform_factor = 1;
    return formatNumber2((b + Math.sqrt(bc * bc + ab * ab + bc * ab)) * transform_factor);
}

function calcAmpsC(c, ca, bc, transform_factor) {
    if (transform_factor == null) transform_factor = 1;
    return formatNumber2((c + Math.sqrt(ca * ca + bc * bc + ca * bc)) * transform_factor);
}

/* power per cab */
function hasLeg(leg_alpha) {
    return String(row["legs_alpha"]).toLowerCase().indexOf(leg_alpha.toLowerCase()) >= 0 ;
}

function getActualAmps(phase, power_factor, amps_all_phases, amps_phase) {
    if (hasLeg(phase)) {
	if (power_factor == 1) {
	    return amps_all_phases ? formatNumber0(amps_all_phases) : 
		reportContext.getMessage("n/a", reportContext.getLocale());
	} else {
	    return amps_phase ? formatNumber0(amps_phase) : 
		reportContext.getMessage("n/a", reportContext.getLocale());
	}
    } 
    return formatNumber0(0);
}

function cumulateActAmpsVars(act_a, act_b, act_c, act, legs) {
    vars["SumActpA"] += act_a;
    vars["SumActpB"] += act_b;
    vars["SumActpC"] += act_c;
    if (legs == '1' || legs == '123') {
	vars["SumActA"] += act;
    }
    if (legs == '2' || legs == '123') {
	vars["SumActB"] += act;
    }
    if (legs == '3' || legs == '123') {
	vars["SumActC"] += act;
    }
    if (legs == '12') {
	vars["SumActAB"] += act;
    }
    if (legs == '23') {
	vars["SumActBC"] += act;
    }
    if (legs == '31') {
	vars["SumActCA"] += act;
    }
}

/* used by busway */
function get2ndLegPhase(first_phase) {
    if (first_phase == "a") return "b";
    if (first_phase == "b") return "c";
    return "a";
}

/* cabinet resources */
function getLargestMountingSpace(railusage, cabheight) {
    max = 0;
    current = 0;
    for ( var i = 0; i < cabheight; i++) {
	if (railusage[i] == 1) {//used
	    max = Math.max(max, current);
	    current = 0;
	} else {
	    current++;
	}
    }
    return Math.max(max, current);
}

/* functions used by itemDetails report */
function isRowLabelAndPositionInRowAllowed() {
	importPackage(Packages.com.raritan.tdz.lookup);
	/*if (! ( (row["mounting"] == 'Free-Standing') & (row["class_code"] == SystemLookup.Class.CABINET | row["class_code"] == SystemLookup.Class.DEVICE | row["class_code"] == SystemLookup.Class.NETWORK))) {
		return true;
	}
	return false; */
	return !((row["mounting"] == 'Free-Standing') & (row["class_code"] == SystemLookup.Class.CABINET | row["class_code"] == SystemLookup.Class.DEVICE | row["class_code"] == SystemLookup.Class.NETWORK));
}
function isRowLabelAllowed() {
	return isRowLabelAndPositionInRowAllowed();
}

function isPositionInRowAllowed() {
	return isRowLabelAndPositionInRowAllowed();
}

function isCabinetAllowed() {
	importPackage(Packages.com.raritan.tdz.lookup);
	return (row["mounting"] == 'Free-Standing') | (row["subclass_code"] == SystemLookup.SubClass.VIRTUAL_MACHINE);
}

function isRailsUsedAllowed() {
	importPackage(Packages.com.raritan.tdz.lookup);
	return !(((row["mounting"] == 'Rackable') | (row["mounting"] == 'Non-Rackable')) & (row["class_code"] != SystemLookup.Class.FLOOR_OUTLET) );
}

function isUPositionAllowed() {
	importPackage(Packages.com.raritan.tdz.lookup);
	return !(row["subclass_code"] != SystemLookup.SubClass.VIRTUAL_MACHINE & ((row["mounting"] == 'ZeroU') | (row["mounting"] == 'Rackable') | (row["mounting"] == 'Busway') | (row["mounting"] == 'Non-Rackable')));
}

function isOrderAllowed() {
	importPackage(Packages.com.raritan.tdz.lookup);
	return !((row["mounting"] == 'Non-Rackable') & (row["class_code"] != SystemLookup.Class.FLOOR_OUTLET));
}

function isOrientationAllowed() {
	importPackage(Packages.com.raritan.tdz.lookup);
	return !(((row["mounting"] == 'Rackable') | (row["mounting"] == 'Non-Rackable')) & (row["class_code"] != SystemLookup.Class.FLOOR_OUTLET));
}

function isFrontFacesAllowed() {
	return row["mounting"] != 'Free-Standing';
}

function isMountingZeroU() {
	return row["mounting"] == 'ZeroU';
}

function isDepthAllowed() {
	return !isMountingZeroU();
}

function isCabinetSideAllowed() {
	return !isMountingZeroU();
}

function isMountingChassisOrBladeRelated() {
	return row["mounting"] == 'Blade';
}

function isChassisAllowed() {
	return !isMountingChassisOrBladeRelated();
}

function isChassisFaceAllowed() {
	return !isMountingChassisOrBladeRelated();
}

function isSlotPositionAllowed() {
	return !isMountingChassisOrBladeRelated();
}

function wrap (longStr,width) { 
	if (longStr == null) return "";
	
	length = longStr.length;
	if(length <= width) { 
		return longStr;
	}
	return (longStr.substring(0, width) + "\n" + wrap(longStr.substring(width, length), width)); 
} 
	 
