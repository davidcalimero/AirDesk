package pt.ulisboa.tecnico.cmov.airdesk.exception;

import java.io.Serializable;

public class CorruptMessageException extends RuntimeException implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public CorruptMessageException(){}

	public String getMessage(){
		return "CORRUPT MESSAGE";
	}
}