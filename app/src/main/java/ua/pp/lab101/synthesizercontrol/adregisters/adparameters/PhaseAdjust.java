package ua.pp.lab101.synthesizercontrol.adregisters.adparameters;

/**
 * The phase adjust bit (Bit DB28) enables adjustment of the output phase of a
 * given output frequency. Disabling VCO band selection is recommended only for
 * fixed frequency applications or for frequency deviations of <1 MHz from the
 * originally selected frequency.
 * 
 * @author Cherniakh B.
 *
 */
public enum PhaseAdjust {
	/**
	 * When phase adjustment is disabled (Bit DB28 is set to 0), the part
	 * performs VCO band selection and phase resync (if phase resync is enabled
	 * in Register 3, Bits[DB16:DB15]) when Register 0 is updated.
	 */
	OFF,
	/**
	 * When phase adjustment is enabled (Bit DB28 is set to 1), the part does
	 * not perform VCO band selection or phase resync when Register 0 is
	 * updated.
	 */
	ON;
}
