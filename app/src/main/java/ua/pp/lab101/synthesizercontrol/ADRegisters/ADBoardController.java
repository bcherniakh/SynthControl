package ua.pp.lab101.synthesizercontrol.ADRegisters;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
import ua.pp.lab101.synthesizercontrol.FTDIConverter.DataConverter;

public class ADBoardController {
	private ADRegisterController registerController = new ADRegisterController();
	private DataConverter dataConverter = new DataConverter();

    private static ADBoardController boardControllerInstance;
	
	
	private ADBoardController() {
		//Register 5 initialization
		registerController.setLDPinMode(LDPinMode.DIGITAL_LD);
		
		//Register 4 initialization
		registerController.setFeedbackSelect(FeedbackSelect.FUNDAMENTAL);
		registerController.setRFDividerSelect(RFDividerSelect.PLUS_64);
		registerController.setVcoPowerDown(VcoPowerDown.VCO_POWERED_UP);
        registerController.setBandSelectClockDivider(140);
		registerController.setMuteTillLockDetect(MuteTillLockDetect.MUTE_DISABLED);
		registerController.setAuxOutputSelect(AuxOutputSelect.DIVIDED_OUTPUT);
		registerController.setAuxOutputEnable(AuxOutputEnable.DISABLED);
		registerController.setAuxOutputPower(AuxOutputPower.MINUS_4_dBm);
		registerController.setRFOutputEnable(RFOutputEnable.ENABLED);
		registerController.setOutputPower(OutputPower.PLUS_2_dBm);
		
		//Register 3 initialization
		registerController.setBandSelectClockMode(BandSelectClockMode.LOW);
		registerController.setABP(AntibackslashPulseWidth.FRAC_N);
		registerController.setChargeCancel(ChargeCancellation.DISABLED);
		registerController.setCSR(CycleSlipReduction.DISABLED);
		registerController.setClockDividerMode(ClockDividerMode.CLOCK_DIVIDER_OFF);
		registerController.setClockDividerValue(0x87);
		
		//Register 2 initialization
		registerController.setLowNoiseAndLowSpurModes(NoiseMode.LOW_NOISE_MODE);
		registerController.setMouxout(Muxout.DIGITAL_LOCK_DETECT);
		registerController.setReferenceDoublerDBR(ReferenceDoubler.ENABLED);
		registerController.setReferenceDivideByTwo(ReferenceDivideByTwo.DISABLED);
		registerController.setRCounter(5);
		registerController.setDoubleBuffer(DoubleBuffer.DISABLED);
		registerController.setChargePumpCurrent(ChargePumpCurrentSetting.ICP_5_00_mA);
		registerController.setLDF(LDF.FRAC_N);
		registerController.setLDP(LDP.LDP_10_ns);
		registerController.setPDPolarity(PDPolarity.POSITIVE);
		registerController.setPowerDown(PowerDown.ENABLED);
		registerController.setCPThreeState(CPThreeState.DISABLED);
		registerController.setCounterReset(CounterReset.DISABLED);
		
		//Register 1 initialization
		registerController.setPhaseAdjust(PhaseAdjust.OFF);
		registerController.setPrescaler(Prescaler.PRESCALER_8_9);
		registerController.setPhase(1);
		registerController.setModulus(4000);
		
		//Register 0 initialization
		registerController.setInteger(640);
		registerController.setFractional(0);
    }

    public static synchronized ADBoardController getInstance(){
        if (boardControllerInstance == null) {
                    boardControllerInstance = new ADBoardController();
        }
            return boardControllerInstance;
    }
    /**
     * Method returns code sequence that is could be sent to the device by FTDIdriver.
     * It initializes all 6 registers with default values.
     * @return array of byte.
     */
    public byte[][] geiInitianCommanSequence(){
        //loadDefaults();
        return getCommandSequence(ADRegisterController.REGISTER5, ADRegisterController.REGISTER4,
                ADRegisterController.REGISTER3, ADRegisterController.REGISTER2,
                ADRegisterController.REGISTER1, ADRegisterController.REGISTER0);
    }

    public byte[][] turnOffTheDevice() {
        //Changing the state of register
        registerController.setPowerDown(PowerDown.ENABLED);
        return getCommandSequence(ADRegisterController.REGISTER5, ADRegisterController.REGISTER4,
                ADRegisterController.REGISTER3, ADRegisterController.REGISTER2,
                ADRegisterController.REGISTER1, ADRegisterController.REGISTER0);
    }

    public byte[][] turnOnDevice() {
        registerController.setPowerDown(PowerDown.DISABLED);
        return getCommandSequence(ADRegisterController.REGISTER5, ADRegisterController.REGISTER4,
                ADRegisterController.REGISTER3, ADRegisterController.REGISTER2,
                ADRegisterController.REGISTER1, ADRegisterController.REGISTER0);
    }

    /**
     * Method sets the frequency by calculating three values:<br>
     *     INT value <br>
     *     MOD value <br>
     *     FRAC value <br>
     * and updating corresponding registers.
     * @param frequencyValue
     */
	public byte[][] setFrequency(double frequencyValue){
		if ((frequencyValue < 35) | (frequencyValue > 4400)) {
			throw new IllegalArgumentException("Frequency value not in range");
		}
		int intValue = 0;
		int fracValue = 0;
		int modValue = 0;
		int pdfValue = 4;
		int rfDivider = 1;
		double VCO = 0.0;

        //Calculating RFDivider
		rfDivider = calculateRFDividerValue(frequencyValue);

        //Calculating INT value
		VCO = frequencyValue * rfDivider;
		intValue = (int) VCO / pdfValue;

        //Calculating MOD and FRAC values
        double vcoFracValue = VCO - (intValue * pdfValue);

        BigDecimal bd = new BigDecimal(vcoFracValue).setScale(3, RoundingMode.HALF_EVEN);
        vcoFracValue = bd.doubleValue();

        double clearFracVal = vcoFracValue / pdfValue;
        modValue = 4095;
        fracValue = (int) (modValue * clearFracVal);

        //Setting calculated values
        setRFDivider(rfDivider);
        setINTValue(intValue);
        setMODValue(modValue);
        setFracValue(fracValue);

        //Returns the command sequence to set up output frequency
        return getCommandSequence(ADRegisterController.REGISTER5, ADRegisterController.REGISTER4,
                ADRegisterController.REGISTER3, ADRegisterController.REGISTER2,
                ADRegisterController.REGISTER1, ADRegisterController.REGISTER0);
		
	}

    /**
     * Method returns current state of all 6 registers. It is good for debugging.
     * @return current register state as String.
     */
    public String getCurrentState(){
        return registerController.toString();
    }

    private void setFracValue(int fracValue) {
        registerController.setFractional(fracValue);
    }

    private void setMODValue(int modValue) {
        registerController.setModulus(modValue);
    }

    private void setINTValue(int intValue) {
        registerController.setInteger(intValue);
    }

    private int calculateRFDividerValue(double frequencyValue){
		double RFOut = frequencyValue;
		int RFDivider = 1;
		while ((RFOut < 2200) || (RFOut > 4400)){
			RFDivider = RFDivider * 2;
			RFOut =  frequencyValue * (double) RFDivider;
		}
		return RFDivider;
	}

    private void setRFDivider(int calculatedRFDividerValue){
        switch(calculatedRFDividerValue) {
            case 1:
                registerController.setRFDividerSelect(RFDividerSelect.PLUS_1);
                break;
            case 2:
                registerController.setRFDividerSelect(RFDividerSelect.PLUS_2);
                break;
            case 4:
                registerController.setRFDividerSelect(RFDividerSelect.PLUS_4);
                break;
            case 8:
                registerController.setRFDividerSelect(RFDividerSelect.PLUS_8);
                break;
            case 16:
                registerController.setRFDividerSelect(RFDividerSelect.PLUS_16);
                break;
            case 32:
                registerController.setRFDividerSelect(RFDividerSelect.PLUS_32);
                break;
            case 64:
                registerController.setRFDividerSelect(RFDividerSelect.PLUS_64);
                break;
        }
    }

    /**
     * Method returns the two dimensional array of bytes.
     * @return commandSequence - is a matrix of byte/
     */
    private byte[][] getCommandSequence(String... registers) {
        if (registers.length == 0) throw new IllegalArgumentException("No registers found!");

        byte[][] commandSequence;
        commandSequence = new byte[registers.length][];

        for (int i = 0; i < registers.length; i++) {
            commandSequence[i] = dataConverter.getFinalSequence(registerController.getRegisterData(registers[i]));
        }
        return commandSequence;
    }

    private void loadDefaults() {
        //Register 5 initialization
        registerController.setLDPinMode(LDPinMode.DIGITAL_LD);

        //Register 4 initialization
        registerController.setFeedbackSelect(FeedbackSelect.FUNDAMENTAL);
        registerController.setRFDividerSelect(RFDividerSelect.PLUS_64);
        registerController.setVcoPowerDown(VcoPowerDown.VCO_POWERED_UP);
        registerController.setBandSelectClockDivider(140);
        registerController.setMuteTillLockDetect(MuteTillLockDetect.MUTE_DISABLED);
        registerController.setAuxOutputSelect(AuxOutputSelect.DIVIDED_OUTPUT);
        registerController.setAuxOutputEnable(AuxOutputEnable.DISABLED);
        registerController.setAuxOutputPower(AuxOutputPower.MINUS_4_dBm);
        registerController.setRFOutputEnable(RFOutputEnable.ENABLED);
        registerController.setOutputPower(OutputPower.PLUS_2_dBm);

        //Register 3 initialization
        registerController.setBandSelectClockMode(BandSelectClockMode.LOW);
        registerController.setABP(AntibackslashPulseWidth.FRAC_N);
        registerController.setChargeCancel(ChargeCancellation.DISABLED);
        registerController.setCSR(CycleSlipReduction.DISABLED);
        registerController.setClockDividerMode(ClockDividerMode.CLOCK_DIVIDER_OFF);
        registerController.setClockDividerValue(0x87);

        //Register 2 initialization
        registerController.setLowNoiseAndLowSpurModes(NoiseMode.LOW_NOISE_MODE);
        registerController.setMouxout(Muxout.DIGITAL_LOCK_DETECT);
        registerController.setReferenceDoublerDBR(ReferenceDoubler.ENABLED);
        registerController.setReferenceDivideByTwo(ReferenceDivideByTwo.DISABLED);
        registerController.setRCounter(5);
        registerController.setDoubleBuffer(DoubleBuffer.DISABLED);
        registerController.setChargePumpCurrent(ChargePumpCurrentSetting.ICP_5_00_mA);
        registerController.setLDF(LDF.FRAC_N);
        registerController.setLDP(LDP.LDP_10_ns);
        registerController.setPDPolarity(PDPolarity.POSITIVE);
        registerController.setPowerDown(PowerDown.ENABLED);
        registerController.setCPThreeState(CPThreeState.DISABLED);
        registerController.setCounterReset(CounterReset.DISABLED);

        //Register 1 initialization
        registerController.setPhaseAdjust(PhaseAdjust.OFF);
        registerController.setPrescaler(Prescaler.PRESCALER_8_9);
        registerController.setPhase(1);
        registerController.setModulus(4000);

        //Register 0 initialization
        registerController.setInteger(640);
        registerController.setFractional(0);
    }
	
}
