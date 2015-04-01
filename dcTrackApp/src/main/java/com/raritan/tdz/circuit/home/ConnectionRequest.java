package com.raritan.tdz.circuit.home;

import java.util.LinkedList;
import java.util.List;

import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.UserInfo;

/**
 * Represents the state of a particular circuit connection.
 * @author Andrew Cohen
 */
public class ConnectionRequest {
	
	public enum Type {
		CONNECT {
			public String toString() {
				return "Connect";
			}
		},
		DISCONNECT_AND_MOVE {
			public String toString() {
				return  "Disconnect       and Move";
			}
		},
		DISCONNECT {
			public String toString() {
				return  "Disconnect";
			}
		},
		RECONNECT {
			public String toString() {
				return "Reconnect";
			}
		}
	};
	
	private ConnectionRequest relatedRequest;
	private LinkedList<ICircuitConnection> conns = new LinkedList<ICircuitConnection>();
	private ICircuitInfo circuit;
	private Type type;
	private String circuitType;
	private boolean cordsChanged;
	private UserInfo userInfo;
	
	ConnectionRequest(Type type, UserInfo userInfo) {
		this.type = type;
		this.userInfo = userInfo;
	}
	
	ConnectionRequest(ICircuitConnection conn, Type type, UserInfo userInfo) {
		this(type, userInfo);
		this.conns.add( conn );
		this.type = type;
		this.circuitType = conn.getCircuitType();
		this.userInfo = userInfo;
	}
	
	public ConnectionRequest getRelatedRequest() {
		return relatedRequest;
	}
	
	public Type getType() {
		return type;
	}
	
	public String getCircuitType() {
		if (circuitType != null) {
			return circuitType.toLowerCase();
		}
		return null;
	}
	
	public ICircuitInfo getCircuit() {
		return circuit;
	}
	
	public List<Long> getConnList() {
		if (circuit != null) {
			return circuit.getConnList();
		}
		else {
			List<Long> connIds = new LinkedList<Long>();
			for (ICircuitConnection conn : conns) {
				connIds.add( conn.getConnectionId() );
			}
			return connIds;
		}
	}
	
	public List<? extends ICircuitConnection> getConnections() {
		if (circuit != null) {
			return circuit.getCircuitConnections();
		}
		else {
			return conns;
		}
	}
	
	public ICircuitConnection getFirstConnection() {
		if (!conns.isEmpty()) {
			return conns.getFirst();
		}
		return null;
	}
	
	public boolean hasChanges() {
		return (cordsChanged || (!conns.isEmpty()));
	}
	
	void setRelatedRequest(ConnectionRequest relatedConnection) {
		this.relatedRequest = relatedConnection;
	}
	
	void addConnection(ICircuitConnection conn) {
		conns.add( conn );
	}
	
	void setCordsChanged(boolean cordsChanged) {
		this.cordsChanged = cordsChanged;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

}
