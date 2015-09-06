package ua.pp.lab101.synthesizercontrol.ftdiconverter;

/**
 * Created by ashram on 2/9/15.
 */


public class DataConverter {


    public DataConverter() {
    }


    public byte[] getFinalSequence(byte[] inputData) {
        byte[] result = new byte[66];
        result[0] = (byte) 0b00000000;
        addDataToResultedArray(result, calculateCommandSequence(inputData), 1);
        result[65] = (byte) 0b00000010;
        return result;
    }
    private byte[] calculateCommandSequence(byte[] inputData){
        byte[] outputArray = new byte[64];
        for (int i = 0; i < inputData.length; i++) {
            byte[] tempCommands = new byte[16];
            tempCommands = calculateByte(inputData[i]);
            //printByteArray(tempCommands, "Array " + i+1 + " is: ");
            addDataToResultedArray(outputArray, tempCommands,i*16);

        }
        return outputArray;
    }

    private byte[] calculateByte(byte inputByte) {
        byte[] outputSubArray = new byte[16];
        int currentByteToFill = 0;

        for (int i = 7; i >= 0; i--) {
            byte b = (byte) Math.pow(2, i);
            if ((inputByte&b) == b) {
                outputSubArray[currentByteToFill] = setBitByPosition(outputSubArray[currentByteToFill], 3);
                outputSubArray[currentByteToFill] = clearBitByPosition(outputSubArray[currentByteToFill], 2);
                currentByteToFill++;
                outputSubArray[currentByteToFill] = setBitByPosition(outputSubArray[currentByteToFill], 3);
                outputSubArray[currentByteToFill] = setBitByPosition(outputSubArray[currentByteToFill], 2);
                currentByteToFill++;
            } else {
                outputSubArray[currentByteToFill] = clearBitByPosition(outputSubArray[currentByteToFill], 3);
                outputSubArray[currentByteToFill] = clearBitByPosition(outputSubArray[currentByteToFill], 2);
                currentByteToFill++;
                outputSubArray[currentByteToFill] = clearBitByPosition(outputSubArray[currentByteToFill], 3);
                outputSubArray[currentByteToFill] = setBitByPosition(outputSubArray[currentByteToFill], 2);
                currentByteToFill++;
            }
        }
        return outputSubArray;

    }


    private byte setBitByPosition(byte data, int bitPosition){
        return setBitByMask(data, returnSetBitMask(bitPosition));

    }

    private byte clearBitByPosition(byte data, int bitPosition){
        return clearBitByMask(data, returnClearBitMask(bitPosition));
    }

    private byte setBitByMask(byte data, byte mask) {
        return data |= mask;
    }

    private byte clearBitByMask(byte data, byte mask) {
        return data &= mask;

    }

    private byte returnSetBitMask(int bitPosition){
        byte mask = (byte)(1 << bitPosition);
        return mask;
    }

    private byte returnClearBitMask(int bitPosition){
        byte mask = (byte)(~(1 << bitPosition));
        return mask;
    }

    private void addDataToResultedArray(byte[] firstArray, byte[] secondArray, int fromPosition) {
        int currentFillPosition = fromPosition;
        for (int i = 0; i < secondArray.length; i++) {
            firstArray[currentFillPosition] = secondArray[i];
            currentFillPosition++;
        }
    }

    public void printByteArray(byte[] array, String name){
        System.out.println(name);
        for (int i = 0; i < array.length; i++) {
            int num = array[i];
            System.out.println(i + " byte is: " + Integer.toBinaryString(num));
        }
    }
}