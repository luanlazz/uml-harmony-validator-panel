package com.inconsistency.dto;

import java.util.HashMap;
import java.util.Map;

public enum Severity {
	LOW(1),
	MEDIUM(2),
	HIGH(3);
	
	private int value;
    private static Map<Integer, Severity> map = new HashMap<Integer, Severity>();

    private Severity(int value) {
        this.value = value;
    }
    
    static {
        for (Severity severity : Severity.values()) {
            map.put(severity.value, severity);
        }
    }
    
    public static Severity valueOf(int severity) {
        return (Severity) map.get(severity);
    }

    public int getValue() {
        return value;
    }
}
