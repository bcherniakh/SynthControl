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

public class ADFrequencyController {
	public ADRegisterController adr = new ADRegisterController();
	public DataConverter dc = new DataConverter();
	
	
	public ADFrequencyController() {
		//Register 5 initialization
		adr.setLDPinMode(LDPinMode.DIGITAL_LD);
		
		//Register 4 initialization
		adr.setFeedbackSelect(FeedbackSelect.FUNDAMENTAL);
		adr.setRFDividerSelect(RFDividerSelect.PLUS_64);
		adr.setVcoPowerDown(VcoPowerDown.VCO_POWERED_UP);
        adr.setBandSelectClockDivider(140);
		adr.setMuteTillLockDetect(MuteTillLockDetect.MUTE_DISABLED);
		adr.setAuxOutputSelect(AuxOutputSelect.DIVIDED_OUTPUT);
		adr.setAuxOutputEnable(AuxOutputEnable.DISABLED);
		adr.setAuxOutputPower(AuxOutputPower.MINUS_4_dBm);
		adr.setRFOutputEnable(RFOutputEnable.ENABLED);
		adr.setOutputPower(OutputPower.PLUS_2_dBm);
		
		//Register 3 initialization
		adr.setBandSelectClockMode(BandSelectClockMode.LOW);
		adr.setABP(AntibackslashPulseWidth.FRAC_N);
		adr.setChargeCancel(ChargeCancellation.DISABLED);
		adr.setCSR(CycleSlipReduction.DISABLED);
		adr.setClockDividerMode(ClockDividerMode.CLOCK_DIVIDER_OFF);
		adr.setClockDividerValue(0x87);
		
		//Register 2 initialization
		adr.setLowNoiseAndLowSpurModes(NoiseMode.LOW_NOISE_MODE);
		adr.setMouxout(Muxout.DIGITAL_LOCK_DETECT);
		adr.setReferenceDoublerDBR(ReferenceDoubler.ENABLED);
		adr.setReferenceDivideByTwo(ReferenceDivideByTwo.DISABLED);
		adr.setRCounter(5);
		adr.setDoubleBuffer(DoubleBuffer.DISABLED);
		adr.setChargePumpCurrent(ChargePumpCurrentSetting.ICP_5_00_mA);
		adr.setLDF(LDF.FRAC_N);
		adr.setLDP(LDP.LDP_10_ns);
		adr.setPDPolarity(PDPolarity.POSITIVE);
		adr.setPowerDown(PowerDown.DISABLED);
		adr.setCPThreeState(CPThreeState.DISABLED);
		adr.setCounterReset(CounterReset.DISABLED);
		
		//Register 1 initialization
		adr.setPhaseAdjust(PhaseAdjust.OFF);
		adr.setPrescaler(Prescaler.PRESCALER_8_9);
		adr.setPhase(1);
		adr.setModulus(4000);
		
		//Register 0 initialization
		adr.setInteger(640);
		adr.setFractional(0);
    }

    /**
     * Method returns the two dimentional byte array
     * @return
     */
    public byte[][] getCommandSequence() {
        byte[][] commandSequence;
        commandSequence = new byte[6][];
        commandSequence[0] = dc.getFinalSequence(adr.getRegisterData(ADRegisterController.REGISTER0));
        commandSequence[1] = dc.getFinalSequence(adr.getRegisterData(ADRegisterController.REGISTER1));
        commandSequence[2] = dc.getFinalSequence(adr.getRegisterData(ADRegisterController.REGISTER2));
        commandSequence[3] = dc.getFinalSequence(adr.getRegisterData(ADRegisterController.REGISTER3));
        commandSequence[4] = dc.getFinalSequence(adr.getRegisterData(ADRegisterController.REGISTER4));
        commandSequence[5] = dc.getFinalSequence(adr.getRegisterData(ADRegisterController.REGISTER5));
        return commandSequence;
    }

	public void setFrequency(double frequencyValue){
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
		
	}

    private void setFracValue(int fracValue) {
        adr.setFractional(fracValue);
    }

    private void setMODValue(int modValue) {
        adr.setModulus(modValue);
    }

    private void setINTValue(int intValue) {
        adr.setInteger(intValue);
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
                adr.setRFDividerSelect(RFDividerSelect.PLUS_1);
                break;
            case 2:
                adr.setRFDividerSelect(RFDividerSelect.PLUS_2);
                break;
            case 4:
                adr.setRFDividerSelect(RFDividerSelect.PLUS_4);
                break;
            case 8:
                adr.setRFDividerSelect(RFDividerSelect.PLUS_8);
                break;
            case 16:
                adr.setRFDividerSelect(RFDividerSelect.PLUS_16);
                break;
            case 32:
                adr.setRFDividerSelect(RFDividerSelect.PLUS_32);
                break;
            case 64:
                adr.setRFDividerSelect(RFDividerSelect.PLUS_64);
                break;
        }
    }

	public String getCurrentState(){
		return adr.toString();
	}
	
}
