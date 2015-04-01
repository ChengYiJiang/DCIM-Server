
//format H x W x D at common place
function getHWD() {
    if (row["dim_h"] && row["dim_w"] && row["dim_d"]) {
	return row["dim_h"] + " x " + row["dim_w"] + " x " + row["dim_d"];
    }
    return "";
}

function getClassSubClass() {
    if (row["class"] && row["subclass"] ) {
        return row["class"] + "  /  " + row["subclass"] ;
    } else if (row["class"] ) {
        return row["class"] ;
    } else if (row["subclass"] ) {
        return row["subclass"] ;
    }
    return "TestingNo class subclass info avail";
}

function getMounting() {
    if (row["Class"] && row["mounting"] ) {
        return row["Class"] + "  /  " + row["mounting"] ;
    } else if (row["mounting"] ) {
        return row["mounting"] ;
    }
    return "No class subclass info avail";
}

function getUPosition() {
	if (row["u_position"] == SystemLookup.SpecialUPositions.BELOW) {
		return "Below";
	} else if (row["u_position"] == SystemLookup.SpecialUPositions.ABOVE) {
		return "Above";
	}
	return row["u_position"];
}

function getCabName() {
	if (row["class_code"] == 1100 /*item class cabinet */) {
		return row["item_name"];
	}
	return row["parent_item_name" ];
}

function getElevationSide() {
	importPackage(Packages.com.raritan.tdz.reports.eventhandler);

	var side = params ?  new String(params[ReportConstants.CAB_IMAGERAIL_PARAM_ID]) : ReportConstants.IMAGERAIL_FRONT;

	side = side != "null" ? side.toLowerCase() : ReportConstants.IMAGERAIL_FRONT;
	
	if (side == ReportConstants.IMAGERAIL_FRONT) {
		return "Front";
	}
	else if (side == ReportConstants.IMAGERAIL_REAR) {
		return "Back";
	}
	else if (side == ReportConstants.IMAGERAIL_BOTH) {
		return "Elevation: Both";
	}
	return "Elevation: unknown";
}

function getPosition() {
	importPackage(Packages.com.raritan.tdz.lookup);
	
	var position = row["u_position"];
	
	if (null == position) return "";
	
	if (position == SystemLookup.SpecialUPositions.ABOVE) {
		position = "Above";
	}
	else if (position == SystemLookup.SpecialUPositions.BELOW) {
		position = "Below";
	}
	else if (position == SystemLookup.SpecialUPositions.NOPOS) {
		position = "";
	}
	
	var mounting = row["mounting"];
	var postfix = null;
	
	// display the zeroU tag for the ZeroU mounted items
	if (mounting) {
		if (mounting == SystemLookup.Mounting.ZERO_U) {
			postfix = " (" + mounting + ")";
			position = ((parseInt(position) > 9) ? position : (position + " ")) + postfix;
		}
		
		// display shelf position for the Non-Rackable items but power outlet
		else if (mounting == SystemLookup.Mounting.NON_RACKABLE) {
			var shelf_pos = row["shelf_position"];
			
			// check for power outlets and do not show shelf position
			var class_lkp_code = row["class_lkp_code"];
			if (class_lkp_code && class_lkp_code != SystemLookup.Class.FLOOR_OUTLET) {
				postfix = " (" + shelf_pos + ")";
			}
			
			if (postfix) {
				position += postfix;
			}
		}
	}
	return position;
}

function fixDecimal2Digits(n) {
    return n > 9 ? "" + n: n + " ";
}

function getRowLabel() {
	if (row["mounting"] == 'Free-Standing' & row["class"] == 'Device') {
		return row["fs_cab_row_label"];
	}  
	return row["row_label"];
}

function getRowPosition() {
	if (row["mounting"] == 'Free-Standing' & row["class"] == 'Device') {
		return row["fs_cab_position_in_row"];
	}
	return row["position_in_row"];
}

/* power supply report helper */
function getSupplyStr(idx) {
    var port = row["ps_port_name_" + String(idx)];
    if (port) {
	var item = row["ps_item_name_" + String(idx)];
	if (item) return port + " @ " + item;
    }
    return "-";
}

function getNumberPattern(dec_places) {
    pattern = "###0";
    if (dec_places != null && dec_places > 0) {
	pattern += ".";
	for (var i = 0; i < dec_places; i++) {
	    pattern += "0";
	}
    }
    return pattern;
}

function formatNumber(number, pattern) {
    df = new Packages.java.text.DecimalFormat(pattern);
    return df.format(number);
}

function formatNumber0(num) {
    return formatNumber(num, "###0");
}
function formatNumber1(num) {
    return formatNumber(num, "###0.0");
}
function formatNumber2(num) {
    return formatNumber(num, "###0.00");
}

function bladeFilterFace() {
	importPackage(Packages.com.raritan.tdz.reports.eventhandler);
	importPackage(Packages.com.raritan.tdz.lookup);

	var rails = params[ReportConstants.CAB_IMAGERAIL_PARAM_ID];
	var chassisFaces = row["chassisfaces"]; //row._outer["facing_lkp_value_code"];
	var face_lks_id = "";

	/*if (null == chassisFaces) {
		return 20502;
	}*/

	if (rails != null) {
		rails = rails.toLowerCase();
	} 
	else {
		rails = "front";
	}

	if (rails == ReportConstants.IMAGERAIL_FRONT) {
		if (chassisFaces == SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT) {
			face_lks_id = 20501;
		}
		else if (chassisFaces == SystemLookup.Orientation.ITEM_REAR_FACES_CABINET_FRONT) {
			face_lks_id = 20502;
		}	
	} else if (rails == ReportConstants.IMAGERAIL_REAR) {
		if (chassisFaces == SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT) {
			face_lks_id = 20502;
		}
		else if (chassisFaces == SystemLookup.Orientation.ITEM_REAR_FACES_CABINET_FRONT) {
			face_lks_id = 20501;
		}	
	} else if (rails == ReportConstants.IMAGERAIL_BOTH) {
		face_lks_id = "20501, 20502";
	}

	return face_lks_id;
}