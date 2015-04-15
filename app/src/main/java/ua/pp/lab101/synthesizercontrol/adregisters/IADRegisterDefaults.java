package ua.pp.lab101.synthesizercontrol.adregisters;

public interface IADRegisterDefaults {
	
	
	//Default values constants
	/*
	 * Default hardcoded values is shit. Nowadays we use brave new methods to perform 
	 * this shit
	 */
//	public static final int REG0DEFAULT = 0b00000001010000000000000000000000;
//	public static final int REG1DEFAULT = 0b00001000000000001111110100000001;
//	public static final int REG2DEFAULT = 0b00011010000000010101111001000010;
//	public static final int REG3DEFAULT = 0b00000000000000000000010000111011;
//	public static final int REG4DEFAULT = 0b00000000111010001100000000110100;
//	public static final int REG5DEFAULT = 0b00000000010110000000000000000101;
	
	/*
	 * Hardcoding only necessary values. Such as register control bits and
	 * reserved values.
	 */
	public static final int REG0DEFAULT = 0b00000000000000000000000000000000;
	public static final int REG1DEFAULT = 0b00000000000000000000000000000001;
	public static final int REG2DEFAULT = 0b00000000000000000000000000000010;
	public static final int REG3DEFAULT = 0b00000000000000000000000000000011;
	public static final int REG4DEFAULT = 0b00000000000000000000000000000100;
	public static final int REG5DEFAULT = 0b00000000000110000000000000000101;
}