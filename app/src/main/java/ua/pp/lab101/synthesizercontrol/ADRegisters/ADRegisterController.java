package ua.pp.lab101.synthesizercontrol.ADRegisters;

import java.util.HashMap;

import ua.pp.lab101.synthesizercontrol.ADParameters.AntibackslashPulseWidth;
import ua.pp.lab101.synthesizercontrol.ADParameters.AuxOutputEnable;
import ua.pp.lab101.synthesizercontrol.ADParameters.AuxOutputPower;
import ua.pp.lab101.synthesizercontrol.ADParameters.AuxOutputSelect;
import ua.pp.lab101.synthesizercontrol.ADParameters.BandSelectClockMode;
import ua.pp.lab101.synthesizercontrol.ADParameters.CPThreeState;
import ua.pp.lab101.synthesizercontrol.ADParameters.ChargeCancellation;
import ua.pp.lab101.synthesizercontrol.ADParameters.ChargePumpCurrentSetting;
import ua.pp.lab101.synthesizercontrol.ADParameters.ClockDividerMode;
import ua.pp.lab101.synthesizercontrol.ADParameters.CounterReset;
import ua.pp.lab101.synthesizercontrol.ADParameters.CycleSlipReduction;
import ua.pp.lab101.synthesizercontrol.ADParameters.DoubleBuffer;
import ua.pp.lab101.synthesizercontrol.ADParameters.FeedbackSelect;
import ua.pp.lab101.synthesizercontrol.ADParameters.LDF;
import ua.pp.lab101.synthesizercontrol.ADParameters.LDP;
import ua.pp.lab101.synthesizercontrol.ADParameters.LDPinMode;
import ua.pp.lab101.synthesizercontrol.ADParameters.MuteTillLockDetect;
import ua.pp.lab101.synthesizercontrol.ADParameters.Muxout;
import ua.pp.lab101.synthesizercontrol.ADParameters.NoiseMode;
import ua.pp.lab101.synthesizercontrol.ADParameters.OutputPower;
import ua.pp.lab101.synthesizercontrol.ADParameters.PDPolarity;
import ua.pp.lab101.synthesizercontrol.ADParameters.PhaseAdjust;
import ua.pp.lab101.synthesizercontrol.ADParameters.PowerDown;
import ua.pp.lab101.synthesizercontrol.ADParameters.Prescaler;
import ua.pp.lab101.synthesizercontrol.ADParameters.RFDividerSelect;
import ua.pp.lab101.synthesizercontrol.ADParameters.RFOutputEnable;
import ua.pp.lab101.synthesizercontrol.ADParameters.ReferenceDivideByTwo;
import ua.pp.lab101.synthesizercontrol.ADParameters.ReferenceDoubler;
import ua.pp.lab101.synthesizercontrol.ADParameters.VcoPowerDown;

public class ADRegisterController implements IADRegisterDefaults, IADRegisterID {

	private HashMap<String, ADRegister> registers = new HashMap<String, ADRegister>();
	private ADRegister register5 = new ADRegister(REG5DEFAULT);
	private ADRegister register4 = new ADRegister(REG4DEFAULT);
	private ADRegister register3 = new ADRegister(REG3DEFAULT);
	private ADRegister register2 = new ADRegister(REG2DEFAULT);
	private ADRegister register1 = new ADRegister(REG1DEFAULT);
	private ADRegister register0 = new ADRegister(REG0DEFAULT);

	public ADRegisterController() {
		registers.put(ADRegisterController.REGISTER5, register5);
		registers.put(ADRegisterController.REGISTER4, register4);
		registers.put(ADRegisterController.REGISTER3, register3);
		registers.put(ADRegisterController.REGISTER2, register2);
		registers.put(ADRegisterController.REGISTER1, register1);
		registers.put(ADRegisterController.REGISTER0, register0);
	}

	public ADRegister getRegister(String registerID) {
		return registers.get(registerID);
	}

	public byte[] getRegisterData(String registerID) {
		return registers.get(registerID).getStateAsBytes();
	}

	public String getRegisteStateAsString(String registerID) {
		String registerState = "";
		byte[] state = registers.get(registerID).getStateAsBytes();
		for (byte entry : state) {
			registerState += Integer.toBinaryString(entry) + " ";
		}
		return registerState;
	}

	// REGISTER 5 settings
	/**
	 * Sets the LDpin mode value in register5. Bits[DB23:DB22] set the operation
	 * of the lock detect (LD) pin.
	 * 
	 * @see LDPinMode
	 * @param pinMode
	 *            is pin mode from {@link LDPinMode}
	 */
	public void setLDPinMode(LDPinMode pinMode) {
		switch (pinMode) {
		case LOW:
			register5.clearBitByPosition(23);
			register5.clearBitByPosition(22);
			break;
		case DIGITAL_LD:
			register5.clearBitByPosition(23);
			register5.setBitByPosition(22);
			break;
		case HIGH:
			register5.setBitByPosition(23);
			register5.setBitByPosition(22);
			break;
		}
	}

	// REGISTER 4 settings
	/**
	 * Sets the Feedback Select value in register 4. The DB23 bit selects the
	 * feedback from the VCO output to the N counter. When this bit is set to 1,
	 * the signal is taken directly from the VCO. When this bit is set to 0, the
	 * signal is taken from the output of the output dividers. The dividers
	 * enable coverage of the wide frequency band (34.375 MHz to 4.4 GHz). When
	 * the dividers are enabled and the feedback signal is taken from the
	 * output, the RF output signals of two separately configured PLLs are in
	 * phase. This is useful in some applications where the positive
	 * interference of signals is required to increase the power.
	 * 
	 * @see FeedbackSelect
	 * @param feedbackSelectValue
	 *            is feedback select value from {@link FeedbackSelect}
	 */
	public void setFeedbackSelect(FeedbackSelect feedbackSelectValue) {
		switch (feedbackSelectValue) {
		case DIVIDED:
			register4.clearBitByPosition(23);
			break;
		case FUNDAMENTAL:
			register4.setBitByPosition(23);
			break;
		}
	}

	/**
	 * Sets the RF Divider Select value in register 4. Bits[DB22:DB20] select
	 * the value of the RF output divider.
	 * 
	 * @see RFDividerSelect
	 * @param rfDividerSelectValue
	 *            is RF divider select value from {@link RFDividerSelect}
	 */
	public void setRFDividerSelect(RFDividerSelect rfDividerSelectValue) {
		switch (rfDividerSelectValue) {
		case PLUS_1:
			register4.clearBitByPosition(22);
			register4.clearBitByPosition(21);
			register4.clearBitByPosition(20);
			break;
		case PLUS_2:
			register4.clearBitByPosition(22);
			register4.clearBitByPosition(21);
			register4.setBitByPosition(20);
			break;
		case PLUS_4:
			register4.clearBitByPosition(22);
			register4.setBitByPosition(21);
			register4.clearBitByPosition(20);
			break;
		case PLUS_8:
			register4.clearBitByPosition(22);
			register4.setBitByPosition(21);
			register4.setBitByPosition(20);
			break;
		case PLUS_16:
			register4.setBitByPosition(22);
			register4.clearBitByPosition(21);
			register4.clearBitByPosition(20);
			break;
		case PLUS_32:
			register4.setBitByPosition(22);
			register4.clearBitByPosition(21);
			register4.setBitByPosition(20);
			break;
		case PLUS_64:
			register4.setBitByPosition(22);
			register4.setBitByPosition(21);
			register4.clearBitByPosition(20);
			break;
		}
	}

	/**
	 * Sets 8-bit band select clock divider value in register 4. Bits[DB19:DB12]
	 * set a divider for the band select logic clock input. By default, the
	 * output of the R counter is the value used to clock the band select logic,
	 * but, if this value is too high (>125 kHz), a divider can be switched on
	 * to divide the R counter output to a smaller value.
	 * 
	 * @param selectClockDividerValue
	 *            - band select clock divider
	 * @throws IllegalArgumentException
	 *             - if value is less than 0 or more than 255
	 */
	public void setBandSelectClockDivider(int selectClockDividerValue) {
		if (selectClockDividerValue < 0 || selectClockDividerValue > 255)
			throw new IllegalArgumentException("Incorrect value");
		int clearMask = 0b11111111111100000000111111111111;
		register4.clearStateByMask(clearMask);
		int setStateMask = selectClockDividerValue << 12;
		register4.setStateByMask(setStateMask);

	}

	/**
	 * Sets VCO Power Down state in register 4. Setting the DB11 bit to 0 powers
	 * the VCO up; setting this bit to 1 powers the VCO down.
	 * 
	 * @param vcoPowerDownValue
	 *            - VCO Power Down state
	 * @see VcoPowerDown
	 */
	public void setVcoPowerDown(VcoPowerDown vcoPowerDownValue) {
		switch (vcoPowerDownValue) {
		case VCO_POWERED_UP:
			register4.clearBitByPosition(11);
			break;
		case VCO_POWERED_DOWN:
			register4.setBitByPosition(11);
			break;
		}
	}

	/**
	 * Sets Mute till Lock Detect value in register 4. When the DB10 bit is set
	 * to 1, the supply current to the RF output stage is shut down until the
	 * part achieves lock, as measured by the digital lock detect circuitry.
	 * 
	 * @param mtld
	 *            is MTLD state
	 * @see MuteTillLockDetect
	 */
	public void setMuteTillLockDetect(MuteTillLockDetect mtld) {
		switch (mtld) {
		case MUTE_DISABLED:
			register4.clearBitByPosition(10);
			break;
		case MUTE_ENABLED:
			register4.setBitByPosition(10);
			break;
		}
	}

	/**
	 * Sets Aux Output Select value in register 4. The DB9 bit sets the
	 * auxiliary RF output. If DB9 is set to 0, the auxiliary RF output is the
	 * output of the RF dividers; if DB9 is set to 1, the auxiliary RF output is
	 * the fundamental VCO frequency.
	 * 
	 * 
	 * @param auxOutputValue
	 *            is aux output select state
	 * @see AuxOutputSelect
	 */
	public void setAuxOutputSelect(AuxOutputSelect auxOutputValue) {
		switch (auxOutputValue) {
		case DIVIDED_OUTPUT:
			register4.clearBitByPosition(9);
			break;
		case FUNDAMENTAL:
			register4.setBitByPosition(9);
			break;
		}
	}

	/**
	 * Sets Aux Output Enable value in register 4. The DB8 bit enables or
	 * disables the auxiliary RF output. If DB8 is set to 0, the auxiliary RF
	 * output is disabled; if DB8 is set to 1, the auxiliary RF output is
	 * enabled.
	 * 
	 * @param auxOutputEnableValue
	 *            is an aux output enable state
	 * @see AuxOutputEnable
	 */
	public void setAuxOutputEnable(AuxOutputEnable auxOutputEnableValue) {
		switch (auxOutputEnableValue) {
		case DISABLED:
			register4.clearBitByPosition(8);
			break;
		case ENABLED:
			register4.setBitByPosition(8);
			break;
		}
	}

	/**
	 * Sets AUX Output Power in register 4. Bits[DB7:DB6] set the value of the
	 * auxiliary RF output power level.
	 * 
	 * @param auxOutputPowerValue
	 *            is aux output power value
	 * @see AuxOutputPower
	 */
	public void setAuxOutputPower(AuxOutputPower auxOutputPowerValue) {
		switch (auxOutputPowerValue) {
		case MINUS_4_dBm:
			register4.clearBitByPosition(7);
			register4.clearBitByPosition(6);
			break;
		case MINUS_1_dBm:
			register4.clearBitByPosition(7);
			register4.setBitByPosition(6);
			break;
		case PLUS_2_dBm:
			register4.setBitByPosition(7);
			register4.clearBitByPosition(6);
			break;
		case PLUS_5_dBm:
			register4.setBitByPosition(7);
			register4.setBitByPosition(6);
			break;
		}
	}

	/**
	 * Sets RF Output Enable value in register 4. The DB5 bit enables or
	 * disables the primary RF output. If DB5 is set to 0, the primary RF output
	 * is disabled; if DB5 is set to 1, the primary RF output is enabled.
	 * 
	 * @param rfOutrutEnableValue
	 * @see RFOutputEnable
	 */
	public void setRFOutputEnable(RFOutputEnable rfOutrutEnableValue) {
		switch (rfOutrutEnableValue) {
		case DISABLED:
			register4.clearBitByPosition(5);
			break;
		case ENABLED:
			register4.setBitByPosition(5);
			break;
		}
	}

	/**
	 * Sets Output power in register 4. Bits[DB4:DB3] set the value of the
	 * primary RF output power level.
	 * 
	 * @param outputPowerValue
	 * @see OutputPower
	 */
	public void setOutputPower(OutputPower outputPowerValue) {
		switch (outputPowerValue) {
		case MINUS_4_dBm:
			register4.clearBitByPosition(4);
			register4.clearBitByPosition(3);
			break;
		case MINUS_1_dBm:
			register4.clearBitByPosition(4);
			register4.setBitByPosition(3);
			break;
		case PLUS_2_dBm:
			register4.setBitByPosition(4);
			register4.clearBitByPosition(3);
			break;
		case PLUS_5_dBm:
			register4.setBitByPosition(4);
			register4.setBitByPosition(3);
			break;
		}
	}

	// REGISTER 3 settings:
	/**
	 * Sets band Select Clock Mode value in register 3. Setting the DB23 bit to
	 * 1 selects a faster logic sequence of band selection, which is suitable
	 * for high PFD frequencies and is necessary for fast lock applications.
	 * Setting the DB23 bit to 0 is recommended for low PFD (<125 kHz) values.
	 * For the faster band select logic modes (DB23 set to 1), the value of the
	 * band select clock divider must be less than or equal to 254.
	 * 
	 * @param bandSelectClockModeValue
	 * @see BandSelectClockMode
	 */
	public void setBandSelectClockMode(
			BandSelectClockMode bandSelectClockModeValue) {
		switch (bandSelectClockModeValue) {
		case LOW:
			register3.clearBitByPosition(23);
			break;
		case HIGH:
			register3.setBitByPosition(23);
			break;
		}
	}

	/**
	 * Sets ABP value in register 3. Bit DB22 sets the PFD antibacklash pulse
	 * width. When Bit DB22 is set to 0, the PFD antibacklash pulse width is 6
	 * ns. This setting is recommended for fractional-N use. When Bit DB22 is
	 * set to 1, the PFD antibacklash pulse width is 3 ns, which results in
	 * phase noise and spur improvements in integer-N operation. For
	 * fractional-N operation, the 3 ns setting is not recommended.
	 * 
	 * @param abpValue
	 * @see AntibackslashPulseWidth
	 */
	public void setABP(AntibackslashPulseWidth abpValue) {
		switch (abpValue) {
		case FRAC_N:
			register3.clearBitByPosition(22);
			break;
		case INT_N:
			register3.setBitByPosition(22);
			break;
		}
	}

	/**
	 * Sets charge cancellation value in register 3. Setting the DB21 bit to 1
	 * enables charge pump charge cancelation. This has the effect of reducing
	 * PFD spurs in integer-N mode. In fractional-N mode, this bit should be set
	 * to 0.
	 * 
	 * @param chargeCancellationValue
	 * @see ChargeCancellation
	 */
	public void setChargeCancel(ChargeCancellation chargeCancellationValue) {
		switch (chargeCancellationValue) {
		case DISABLED:
			register3.clearBitByPosition(21);
			break;
		case ENABLED:
			register3.setBitByPosition(21);
			break;
		}
	}

	/**
	 * Sets cycle slip reduction value in register 3. Setting the DB18 bit to 1
	 * enables cycle slip reduction. CSR is a method for improving lock times.
	 * Note that the signal at the phase frequency detector (PFD) must have a
	 * 50% duty cycle for cycle slip reduction to work. The charge pump current
	 * setting must also be set to a minimum. For more information, see the
	 * Cycle Slip Reduction for Faster Lock Times section.
	 * 
	 * @param csrValue
	 * @see CycleSlipReduction
	 */
	public void setCSR(CycleSlipReduction csrValue) {
		switch (csrValue) {
		case DISABLED:
			register3.clearBitByPosition(18);
			break;
		case ENABLED:
			register3.setBitByPosition(18);
			break;
		}
	}

	/**
	 * Sets clock divider mode in register 3. Bits[DB16:DB15] must be set to 10
	 * to activate phase resync (see the Phase Resync section). These bits must
	 * be set to 01 to activate fast lock (see the Fast Lock Timer and Register
	 * Sequences section). Setting Bits[DB16:DB15] to 00 disables the clock
	 * divider.
	 * 
	 * @param cdmValue
	 * @see ClockDividerMode
	 */
	public void setClockDividerMode(ClockDividerMode cdmValue) {
		switch (cdmValue) {
		case CLOCK_DIVIDER_OFF:
			register3.clearBitByPosition(16);
			register3.clearBitByPosition(15);
			break;
		case FAST_LOCK_ENABLE:
			register3.clearBitByPosition(16);
			register3.setBitByPosition(15);
			break;
		case RESYNC_ENABLE:
			register3.setBitByPosition(16);
			register3.clearBitByPosition(15);
			break;
		case RESERVED:
			register3.setBitByPosition(16);
			register3.setBitByPosition(15);
			break;
		}
	}

	/**
	 * Sets 12 bit clock divider value in register 3. Bits[DB14:DB3] set the
	 * 12-bit clock divider value. This value is the timeout counter for
	 * activation of phase resync (see the Phase Resync section). The clock
	 * divider value also sets the timeout counter for fast lock (see the Fast
	 * Lock Timer and Register Sequences section).
	 * 
	 * @param clockDividerValue
	 *            is an integer value from 0 to 4095
	 * @throws IllegalArgumentException
	 *             if clockDividerValue is less than 0 or more than 4095
	 */
	public void setClockDividerValue(int clockDividerValue) {
		if (clockDividerValue < 0 || clockDividerValue > 4095)
			throw new IllegalArgumentException("Incorrect value");
		/*
		 * magic number clearMask is a mask for clear method it's purpose is to
		 * clear bits 3 to 14 included in register 3
		 */
		int clearMask = 0b11111111111111111000000000000111;
		register3.clearStateByMask(clearMask);

		/*
		 * setStateMask sets the 12 bit integer value into register 3. This
		 * value is shifted left 3 times. The point is to change bits 3 to 14 is
		 * register 3.
		 */
		int setStateMask = clockDividerValue << 3;
		register3.setStateByMask(setStateMask);
	}

	// REGISTER 2 settings
	/**
	 * Sets Low Noise and Low Spur mode in register 2. The noise mode on the
	 * ADF4351 is controlled by setting Bits[DB30:DB29] in Register 2. The noise
	 * mode allows the user to optimize a design either for improved spurious
	 * performance or for improved phase noise performance. When the low spur
	 * mode is selected, dither is enabled. Dither randomizes the fractional
	 * quantization noise so that it resembles white noise rather than spurious
	 * noise. As a result, the part is optimized for improved spurious
	 * performance. Low spur mode is normally used for fast-locking applications
	 * when the PLL closed-loop bandwidth is wide. Wide loop bandwidth is a loop
	 * bandwidth greater than 1/10 of the RFOUT channel step resolu- tion
	 * (fRES). A wide loop filter does not attenuate the spurs to the same level
	 * as a narrow loop bandwidth. For best noise performance, use the low noise
	 * mode option. When the low noise mode is selected, dither is disabled.
	 * This mode ensures that the charge pump operates in an optimum region for
	 * noise performance. Low noise mode is extremely useful when a narrow loop
	 * filter bandwidth is available. The synthesizer ensures extremely low
	 * noise, and the filter attenuates the spurs. Figure 10 through Figure 12
	 * show the trade-offs in a typical W-CDMA setup for different noise and
	 * spur settings.
	 * 
	 * @param noiseModeValue
	 * @see NoiseMode
	 */
	public void setLowNoiseAndLowSpurModes(NoiseMode noiseModeValue) {
		switch (noiseModeValue) {
		case LOW_NOISE_MODE:
			register2.clearBitByPosition(30);
			register2.clearBitByPosition(29);
			break;
		case LOW_SPUR_MODE:
			register2.setBitByPosition(30);
			register2.setBitByPosition(29);
			break;
		}
	}

	/**
	 * Sets MUXOUT in register 2. The on-chip multiplexer is controlled by
	 * Bits[DB28:DB26]. Note that N counter output must be disabled for VCO band
	 * selection to operate correctly.
	 * 
	 * @param muxoutValue
	 * @see Muxout
	 */
	public void setMouxout(Muxout muxoutValue) {
		switch (muxoutValue) {
		case THREE_STATE_OUTPUT:
			register2.clearBitByPosition(28);
			register2.clearBitByPosition(27);
			register2.clearBitByPosition(26);
			break;
		case DV_DD:
			register2.clearBitByPosition(28);
			register2.clearBitByPosition(27);
			register2.setBitByPosition(26);
			break;
		case DGND:
			register2.clearBitByPosition(28);
			register2.setBitByPosition(27);
			register2.clearBitByPosition(26);
			break;
		case R_COUNTER_OUTPUT:
			register2.clearBitByPosition(28);
			register2.setBitByPosition(27);
			register2.setBitByPosition(26);
			break;
		case N_DIVIDER_OUTPUT:
			register2.setBitByPosition(28);
			register2.clearBitByPosition(27);
			register2.clearBitByPosition(26);
			break;
		case ANALOG_LOCK_DETECT:
			register2.setBitByPosition(28);
			register2.clearBitByPosition(27);
			register2.setBitByPosition(26);
			break;
		case DIGITAL_LOCK_DETECT:
			register2.setBitByPosition(28);
			register2.setBitByPosition(27);
			register2.clearBitByPosition(26);
			break;
		}
	}

	/**
	 * Sets Reference Doubler value in register 2. Setting the DB25 bit to 0
	 * disables the doubler and feeds the REFIN signal directly into the 10-bit
	 * R counter. Setting this bit to 1 multiplies the REFIN frequency by a
	 * factor of 2 before feeding it into the 10-bit R counter. When the doubler
	 * is disabled, the REFIN falling edge is the active edge at the PFD input
	 * to the fractional synthesizer. When the doubler is enabled, both the
	 * rising and falling edges of REFIN become active edges at the PFD input.
	 * When the doubler is enabled and the low spur mode is selected, the
	 * in-band phase noise performance is sensitive to the REFIN duty cycle. The
	 * phase noise degradation can be as much as 5 dB for REFIN duty cycles
	 * outside a 45% to 55% range. The phase noise is insensitive to the REFIN
	 * duty cycle in the low noise mode and when the doubler is disabled. The
	 * maximum allowable REFIN frequency when the doubler is enabled is 30 MHz.
	 * 
	 * 
	 * @param referenceDoublerValue
	 * @see ReferenceDoubler
	 */
	public void setReferenceDoublerDBR(ReferenceDoubler referenceDoublerValue) {
		switch (referenceDoublerValue) {
		case DISABLED:
			register2.clearBitByPosition(25);
			break;
		case ENABLED:
			register2.setBitByPosition(25);
			break;
		}
	}

	/**
	 * Sets Reference divide by 2 value in register 2. Setting the DB24 bit to 1
	 * inserts a divide-by-2 toggle flip-flop between the R counter and the PFD,
	 * which extends the maximum REFIN input rate. This function allows a 50%
	 * duty cycle signal to appear at the PFD input, which is necessary for
	 * cycle slip reduction.
	 * 
	 * @param referenceDivideByTwoValue
	 * @see ReferenceDivideByTwo
	 */
	public void setReferenceDivideByTwo(
			ReferenceDivideByTwo referenceDivideByTwoValue) {
		switch (referenceDivideByTwoValue) {
		case DISABLED:
			register2.clearBitByPosition(24);
			break;
		case ENABLED:
			register2.setBitByPosition(24);
			break;
		}
	}

	/**
	 * Sets 10 bit R Counter value in register 2. The 10-bit R counter
	 * (Bits[DB23:DB14]) allows the input reference frequency (REFIN) to be
	 * divided down to produce the reference clock to the PFD. Division ratios
	 * from 1 to 1023 are allowed.
	 * 
	 * @param rCounterValue
	 *            is an integer value from 0 to 1023
	 * @throws IllegalArgumentException
	 *             if clockDividerValue is less than 0 or more than 1023
	 */
	public void setRCounter(int rCounterValue) {
		if (rCounterValue < 0 || rCounterValue > 1023)
			throw new IllegalArgumentException("Incorrect value");
		/*
		 * magic number clearMask is a mask for clear method it's purpose is to
		 * clear bits 14 to 23 included in register 2
		 */
		int clearMask = 0b11111111000000000011111111111111;
		register2.clearStateByMask(clearMask);

		/*
		 * setStateMask sets the 10 bit integer value into register 3. This
		 * value is shifted left 14 times. The point is to change bits 14 to 23
		 * is register 2.
		 */
		int setStateMask = rCounterValue << 14;
		register2.setStateByMask(setStateMask);
	}

	/**
	 * Sets Double Buffer value in register 2. The DB13 bit enables or disables
	 * double buffering of Bits[DB22:DB20] in Register 4. For information about
	 * how double buffering works, see the Program Modes section of the
	 * datasheet.
	 * 
	 * @param doubleBufferValue
	 * @see DoubleBuffer
	 */
	public void setDoubleBuffer(DoubleBuffer doubleBufferValue) {
		switch (doubleBufferValue) {
		case DISABLED:
			register2.clearBitByPosition(13);
			break;
		case ENABLED:
			register2.setBitByPosition(13);
			break;

		}
	}

	/**
	 * Sets Charge pump current setting value in register 2. Bits[DB12:DB9] set
	 * the charge pump current. This value should be set to the charge pump
	 * current that the loop filter is designed with.
	 * 
	 * @param chargePumpCurrentValue
	 * @see ChargePumpCurrentSetting
	 */
	public void setChargePumpCurrent(
			ChargePumpCurrentSetting chargePumpCurrentValue) {
		switch (chargePumpCurrentValue) {
		case ICP_0_31_mA:
			register2.clearBitByPosition(12);
			register2.clearBitByPosition(11);
			register2.clearBitByPosition(10);
			register2.clearBitByPosition(9);
			break;
		case ICP_0_63_mA:
			register2.clearBitByPosition(12);
			register2.clearBitByPosition(11);
			register2.clearBitByPosition(10);
			register2.setBitByPosition(9);
			break;
		case ICP_0_94_mA:
			register2.clearBitByPosition(12);
			register2.clearBitByPosition(11);
			register2.setBitByPosition(10);
			register2.clearBitByPosition(9);
			break;
		case ICP_1_25_mA:
			register2.clearBitByPosition(12);
			register2.clearBitByPosition(11);
			register2.setBitByPosition(10);
			register2.setBitByPosition(9);
			break;
		case ICP_1_56_mA:
			register2.clearBitByPosition(12);
			register2.setBitByPosition(11);
			register2.clearBitByPosition(10);
			register2.clearBitByPosition(9);
			break;
		case ICP_1_88_mA:
			register2.clearBitByPosition(12);
			register2.setBitByPosition(11);
			register2.clearBitByPosition(10);
			register2.setBitByPosition(9);
			break;
		case ICP_2_19_mA:
			register2.clearBitByPosition(12);
			register2.setBitByPosition(11);
			register2.setBitByPosition(10);
			register2.clearBitByPosition(9);
			break;
		case ICP_2_50_mA:
			register2.clearBitByPosition(12);
			register2.setBitByPosition(11);
			register2.setBitByPosition(10);
			register2.setBitByPosition(9);
			break;
		case ICP_2_81_mA:
			register2.setBitByPosition(12);
			register2.clearBitByPosition(11);
			register2.clearBitByPosition(10);
			register2.clearBitByPosition(9);
			break;
		case ICP_3_13_mA:
			register2.setBitByPosition(12);
			register2.clearBitByPosition(11);
			register2.clearBitByPosition(10);
			register2.setBitByPosition(9);
			break;
		case ICP_3_44_mA:
			register2.setBitByPosition(12);
			register2.clearBitByPosition(11);
			register2.setBitByPosition(10);
			register2.clearBitByPosition(9);
			break;
		case ICP_3_75_mA:
			register2.setBitByPosition(12);
			register2.clearBitByPosition(11);
			register2.setBitByPosition(10);
			register2.setBitByPosition(9);
			break;
		case ICP_4_06_mA:
			register2.setBitByPosition(12);
			register2.setBitByPosition(11);
			register2.clearBitByPosition(10);
			register2.clearBitByPosition(9);
			break;
		case ICP_4_38_mA:
			register2.setBitByPosition(12);
			register2.setBitByPosition(11);
			register2.clearBitByPosition(10);
			register2.setBitByPosition(9);
			break;
		case ICP_4_69_mA:
			register2.setBitByPosition(12);
			register2.setBitByPosition(11);
			register2.setBitByPosition(10);
			register2.clearBitByPosition(9);
			break;
		case ICP_5_00_mA:
			register2.setBitByPosition(12);
			register2.setBitByPosition(11);
			register2.setBitByPosition(10);
			register2.setBitByPosition(9);
			break;
		}
	}

	/**
	 * Sets LDF value in register 2. The DB8 bit configures the lock detect
	 * function (LDF). The LDF controls the number of PFD cycles monitored by
	 * the lock detect circuit to ascertain whether lock has been achieved. When
	 * DB8 is set to 0, the number of PFD cycles monitored is 40. When DB8 is
	 * set to 1, the number of PFD cycles monitored is 5. It is recommended that
	 * the DB8 bit be set to 0 for fractional-N mode and to 1 for integer-N
	 * mode.
	 * 
	 * @param ldfValue
	 * @see LDF
	 */
	public void setLDF(LDF ldfValue) {
		switch (ldfValue) {
		case FRAC_N:
			register2.clearBitByPosition(8);
			break;
		case INT_N:
			register2.setBitByPosition(8);
			break;
		}
	}

	/**
	 * Sets LDP value in register2. The lock detect precision bit (Bit DB7) sets
	 * the comparison window in the lock detect circuit. When DB7 is set to 0,
	 * the comparison window is 10 ns; when DB7 is set to 1, the window is 6 ns.
	 * The lock detect circuit goes high when n consecutive PFD cycles are less
	 * than the comparison window value; n is set by the LDF bit (DB8). For
	 * example, with DB8 = 0 and DB7 = 0, 40 consecutive PFD cycles of 10 ns or
	 * less must occur before digital lock detect goes high. For fractional-N
	 * applications, the recommended setting for Bits[DB8:DB7] is 00; for
	 * integer-N applications, the recommended setting for Bits[DB8:DB7] is 11.
	 * 
	 * @param ldpValue
	 * @see LDP
	 */
	public void setLDP(LDP ldpValue) {
		switch (ldpValue) {
		case LDP_10_ns:
			register2.clearBitByPosition(7);
			break;
		case LDP_6_ns:
			register2.clearBitByPosition(7);
			break;
		}
	}

	/**
	 * Sets PD Polarity value in register 2. The DB6 bit sets the phase detector
	 * polarity. When a passive loop filter or a noninverting active loop filter
	 * is used, this bit should be set to 1. If an active filter with an
	 * inverting characteristic is used, this bit should be set to 0.
	 * 
	 * @param pdPolarityValue
	 * @see PDPolarity
	 */
	public void setPDPolarity(PDPolarity pdPolarityValue) {
		switch (pdPolarityValue) {
		case NEGATIVE:
			register2.clearBitByPosition(6);
			break;
		case POSITIVE:
			register2.setBitByPosition(6);
			break;
		}
	}

	/**
	 * Sets Power Down value in register 2. The DB5 bit provides the
	 * programmable power-down mode. Setting this bit to 1 performs a
	 * power-down. Setting this bit to 0 returns the synthesizer to normal
	 * operation. In software power-down mode, the part retains all information
	 * in its registers. The register contents are lost only if the supply
	 * voltages are removed. When power-down is activated, the following events
	 * occur:<br>
	 * Synthesizer counters are forced to their load state conditions.<br>
	 * VCO is powered down.<br>
	 * Charge pump is forced into three-state mode.<br>
	 * Digital lock detect circuitry is reset.<br>
	 * RFOUT buffers are disabled.<br>
	 * Input registers remain active and capable of loading and latching data.<br>
	 * 
	 * @param powerDownValue
	 * @see PowerDown
	 */
	public void setPowerDown(PowerDown powerDownValue) {
		switch (powerDownValue) {
		case DISABLED:
			register2.clearBitByPosition(5);
			break;
		case ENABLED:
			register2.setBitByPosition(5);
			break;
		}
	}

	/**
	 * Sets CP Three-State value in register 2. Setting the DB4 bit to 1 puts
	 * the charge pump into three-state mode. This bit should be set to 0 for
	 * normal operation.
	 * 
	 * @param cpThreeStateValue
	 * @see CPThreeState
	 */
	public void setCPThreeState(CPThreeState cpThreeStateValue) {
		switch (cpThreeStateValue) {
		case DISABLED:
			register2.clearBitByPosition(4);
			break;
		case ENABLED:
			register2.setBitByPosition(4);
			break;
		}
	}

	/**
	 * Sets Counter Reset value in register 2. The DB3 bit is the reset bit for
	 * the R counter and the N counter of the ADF4351. When this bit is set to
	 * 1, the RF synthesizer N counter and R counter are held in reset. For
	 * normal operation, this bit should be set to 0.
	 * 
	 * @param counterResetValue
	 * @see CounterReset
	 */
	public void setCounterReset(CounterReset counterResetValue) {
		switch (counterResetValue) {
		case DISABLED:
			register2.clearBitByPosition(3);
			break;
		case ENABLED:
			register2.setBitByPosition(3);
			break;
		}
	}

	// Register 1 settings

	/**
	 * Sets Phase Adjust value in register 1. The phase adjust bit (Bit DB28)
	 * enables adjustment of the output phase of a given output frequency. When
	 * phase adjustment is enabled (Bit DB28 is set to 1), the part does not
	 * perform VCO band selection or phase resync when Register 0 is updated.
	 * When phase adjustment is disabled (Bit DB28 is set to 0), the part
	 * performs VCO band selection and phase resync (if phase resync is enabled
	 * in Register 3, Bits[DB16:DB15]) when Register 0 is updated. Disabling VCO
	 * band selection is recommended only for fixed frequency applications or
	 * for frequency deviations of <1 MHz from the originally selected
	 * frequency.
	 * 
	 * @param phaseAdjustValue
	 * @see PhaseAdjust
	 */
	public void setPhaseAdjust(PhaseAdjust phaseAdjustValue) {
		switch (phaseAdjustValue) {
		case OFF:
			register1.clearBitByPosition(28);
			break;
		case ON:
			register1.setBitByPosition(28);
			break;
		}
	}

	/**
	 * Sets Prescaler value in register 1. The dual-modulus prescaler (P/P + 1),
	 * along with the INT, FRAC, and MOD values, determines the overall division
	 * ratio from the VCO output to the PFD input. The PR1 bit (DB27) in
	 * Register 1 sets the prescaler value. Operating at CML levels, the
	 * prescaler takes the clock from the VCO output and divides it down for the
	 * counters. The prescaler is based on a synchronous 4/5 core. When the
	 * prescaler is set to 4/5, the maximum RF frequency allowed is 3.6 GHz.
	 * Therefore, when operating the ADF4351 above 3.6 GHz, the prescaler must
	 * be set to 8/9. The prescaler limits the INT value as follows:<br>
	 * Prescaler = 4/5: NMIN = 23<br>
	 * Prescaler = 8/9: NMIN = 75<br>
	 * 
	 * @param prescalerValue
	 * @see Prescaler
	 */
	public void setPrescaler(Prescaler prescalerValue) {
		switch (prescalerValue) {
		case PRESCALER_4_5:
			register1.clearBitByPosition(27);
			break;
		case PRESCALER_8_9:
			register1.setBitByPosition(27);
			break;
		}
	}

	/**
	 * Sets 12 bit Phase value in register 1. Bits[DB26:DB15] control the phase
	 * word. The phase word must be less than the MOD value programmed in
	 * Register 1. The phase word is used to program the RF output phase from 0°
	 * to 360° with a resolution of 360°/MOD (see the Phase Resync section). In
	 * most applications, the phase relationship between the RF signal and the
	 * reference is not important. In such applications, the phase value can be
	 * used to optimize the fractional and sub- fractional spur levels. If
	 * neither the phase resync nor the spurious optimization func- tion is
	 * used, it is recommended that the phase word be set to 1.
	 * 
	 * @param phaseValue
	 *            is an integer value from 0 to 4095
	 * @throws IllegalArgumentException
	 *             if clockDividerValue is less than 0 or more than 4095
	 */
	public void setPhase(int phaseValue) {
		if (phaseValue < 0 || phaseValue > 4095)
			throw new IllegalArgumentException("Incorrect value");
		/*
		 * magic number clearMask is a mask for clear method it's purpose is to
		 * clear bits 15 to 26 included in register 1
		 */
		int clearMask = 0b11111000000000000111111111111111;
		register1.clearStateByMask(clearMask);

		/*
		 * setStateMask sets the 12 bit integer value into register 1. This
		 * value is shifted left 15 times. The point is to change bits 15 to 26
		 * is register 1.
		 */
		int setStateMask = phaseValue << 15;
		register1.setStateByMask(setStateMask);
	}

	/**
	 * Sets 12 bit Modulus value in register 1. The 12 MOD bits (Bits[DB14:DB3])
	 * set the fractional modulus. The fractional modulus is the ratio of the
	 * PFD frequency to the channel step resolution on the RF output. For more
	 * information, see the 12-Bit Programmable Modulus section of the
	 * datasheet.
	 * 
	 * @param modulusValue
	 *            is an integer value from 2 to 4095
	 * @throws IllegalArgumentException
	 *             if clockDividerValue is less than 2 or more than 4095
	 */
	public void setModulus(int modulusValue) {
		if (modulusValue < 2 || modulusValue > 4095)
			throw new IllegalArgumentException("Incorrect value");
		/*
		 * magic number clearMask is a mask for clear method it's purpose is to
		 * clear bits 3 to 14 included in register 1
		 */
		int clearMask = 0b11111111111111111000000000000111;
		register1.clearStateByMask(clearMask);

		/*
		 * setStateMask sets the 12 bit integer value into register 1. This
		 * value is shifted left 3 times. The point is to change bits 3 to 14 is
		 * register 1.
		 */
		int setStateMask = modulusValue << 3;
		register1.setStateByMask(setStateMask);
	}

	/**
	 * Sets 16 bit Integer value (INT) in register 0 The 16 INT bits.
	 * (Bits[DB30:DB15]) set the INT value, which determines the integer part of
	 * the feedback division factor. The INT value is used in Equation 1 (see
	 * the INT, FRAC, MOD, and R Counter Relationship section). Integer values
	 * from 23 to 65,535 are allowed for the 4/5 prescaler; for the 8/9
	 * prescaler, the minimum integer value is 75.
	 * 
	 * @param integerValue
	 *            is an integer value from 23 to 65535. Values less than 23 is
	 *            NOT ALLOWED!
	 * @throws IllegalArgumentException
	 *             if clockDividerValue is less than 23 or more than 65535
	 */
	public void setInteger(int integerValue) {
		if (integerValue < 23 || integerValue > 65535)
			throw new IllegalArgumentException("Incorrect value");
		/*
		 * magic number clearMask is a mask for clear method it's purpose is to
		 * clear bits 15 to 30 included in register 0
		 */
		int clearMask = 0b10000000000000000111111111111111;
		register0.clearStateByMask(clearMask);

		/*
		 * setStateMask sets the 16 bit integer value into register 0. This
		 * value is shifted left 15 times. The point is to change bits 15 to 30
		 * in register 0.
		 */
		int setStateMask = integerValue << 15;
		register0.setStateByMask(setStateMask);
	}

	/**
	 * Sets 12 bit Fractional value (FRAC) in register 0 The 12 FRAC bits.
	 * (Bits[DB14:DB3]) set the numerator of the fraction that is input to the
	 * Σ-Δ modulator. This fraction, along with the INT value, specifies the new
	 * frequency channel that the synthesizer locks to, as shown in the RF
	 * Synthesizer—A Worked Example section. FRAC values from 0 to (MOD − 1)
	 * cover channels over a frequency range equal to the PFD refer- ence
	 * frequency.
	 * 
	 * 
	 * @param fractionalValue
	 *            is an integer value from 0 to 4095.
	 * @throws IllegalArgumentException
	 *             if clockDividerValue is less than 0 or more than 4095
	 */
	public void setFractional(int fractionalValue) {
		if (fractionalValue < 0 || fractionalValue > 4095)
			throw new IllegalArgumentException("Incorrect value");
		/*
		 * magic number clearMask is a mask for clear method it's purpose is to
		 * clear bits 3 to 14 included in register 0
		 */
		int clearMask = 0b11111111111111111000000000000111;
		register0.clearStateByMask(clearMask);

		/*
		 * setStateMask sets the 12 bit integer value into register 0. This
		 * value is shifted left 3 times. The point is to change bits 3 to 14 in
		 * register 0.
		 */
		int setStateMask = fractionalValue << 3;
		register0.setStateByMask(setStateMask);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		result.append(super.toString());
		result.append(newLine);
		String[] registerValues = {register0.toString(),
				register1.toString(),
				register2.toString(),
				register3.toString(),
				register4.toString(),
				register5.toString() };
		for (int i = 0; i < registerValues.length; i++) {
			result.append("Register " + i + " : ");
			result.append(registerValues[i]);
			result.append(newLine);
		}
		return result.toString();
	}
}