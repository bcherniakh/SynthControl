package ua.pp.lab101.synthesizercontrol.adregisters;

import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;

public class ADRegister {

	private int currentState;

	/**
	 * Default constructor fills all register bits with 0.
	 */
	public ADRegister() {
		// Default constructor fills register state to zeros.
		currentState = 0;
		
	}
	
	/**
	 * Constructor which creates array with some predefined data
	 * @param data is 32 bit integer value which presents AD register
	 */
	public ADRegister(int data) {
		this.currentState = data;
	}
	
	//need this for sending API
	/**
	 * Returns current state on the register as 
	 * array of bytes.
	 * @return current state as byte[]
	 */
	public byte[] getStateAsBytes() {
		return ByteBuffer.allocate(4).putInt(currentState).array();
	}

	/**
	 * Sets defined bit in 32 bit register.
	 * 
	 * @param position
	 *            is bits position between 0 and 31
	 * @throws IllegalArgumentException - if position value less than 0 or more than 31
	 */
	public void setBitByPosition(int position) {
		if (!isSuitablePositionValue(position))
			throw new IllegalArgumentException("Incorrect bit position");
		currentState = currentState | (1 << position);
	}
	
	/**
	 * Clears defined bit in 32 bit register
	 * @param position is bits position between 0 and 31
	 * @throws IllegalArgumentException - if position value less than 0 or more than 31
	 */
	public void clearBitByPosition(int position){
		if (!isSuitablePositionValue(position))
			throw new IllegalArgumentException("Incorrect bit position");
		currentState = currentState & ~(1 << position);
	}
	
	/**
	 * Sets the register bits by 1s in mask. Uses OR bit operation.
	 * @param mask is a bitmask where 1 will be set in register.
	 */
	public void setStateByMask(int mask) {
		currentState = currentState | mask;
	}
	/**
	 * Clears the register bits by 0s in mask. Uses AND bit operation.
	 * @param mask
	 */
	public void clearStateByMask(int mask) {
		currentState = currentState & mask;
	}
	
	private boolean isSuitablePositionValue(int position){
		if (position < 0 || position > 31) return false;
		return true;		
	}

	@Override
	public String toString() {
		return "currentState = " + Integer.toBinaryString(currentState);
	}
	
	

}
