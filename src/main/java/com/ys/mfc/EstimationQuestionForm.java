package com.ys.mfc;

import com.WacomGSS.STU.ITabletHandler;
import com.WacomGSS.STU.Protocol.*;
import com.WacomGSS.STU.STUException;
import com.WacomGSS.STU.Tablet;
import com.WacomGSS.STU.UsbDevice;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EstimationQuestionForm  extends JDialog implements ITabletHandler {
    private String indicatorQueue;
    private String indicatorId;
    private String indicatorTitle;
    private String inkicatorDescription;
    List<AnswerVariant> answerVariants;

    private Tablet tablet;
    private Capability capability;
    private Information information;

    public EstimationQuestionForm(UsbDevice usbDevice,
                                  String indicatorQueue,
                                  String indicatorId,
                                  String indicatorTitle,
                                  String inkicatorDescription,
                                  List<AnswerVariant> answerVariants) throws STUException, InterruptedException {
        this.indicatorQueue = indicatorQueue;
        this.indicatorId = indicatorId;
        this.indicatorTitle = indicatorTitle;
        this.inkicatorDescription = inkicatorDescription;
        this.answerVariants = answerVariants;
        this.tablet = new Tablet();

        for (int i = 0; i < 10; i++) {
            int e = tablet.usbConnect(usbDevice, true);
            if (e == 0) {
                this.capability = tablet.getCapability();
                this.information = tablet.getInformation();
                break;
            } else {
                if ( i < 9) {
                    Thread.sleep(500);
                    continue;
                }
                throw new RuntimeException("Failed to connect to USB tablet, error " + e);
            }
        }

        // Set the size of the client window to be actual size,
        // based on the reported DPI of the monitor.
        int screenResolution = this.getToolkit().getScreenResolution();
        Dimension d = new Dimension(this.capability.getTabletMaxX()
                * screenResolution / 2540,
                this.capability.getTabletMaxY() * screenResolution
                        / 2540);
    }


    @Override
    public void onGetReportException(STUException e) {

    }

    @Override
    public void onUnhandledReportData(byte[] bytes) {

    }

    @Override
    public void onPenData(PenData penData) {

    }

    @Override
    public void onPenDataOption(PenDataOption penDataOption) {

    }

    @Override
    public void onPenDataEncrypted(PenDataEncrypted penDataEncrypted) {

    }

    @Override
    public void onPenDataEncryptedOption(PenDataEncryptedOption penDataEncryptedOption) {

    }

    @Override
    public void onPenDataTimeCountSequence(PenDataTimeCountSequence penDataTimeCountSequence) {

    }

    @Override
    public void onPenDataTimeCountSequenceEncrypted(PenDataTimeCountSequenceEncrypted penDataTimeCountSequenceEncrypted) {

    }

    @Override
    public void onEventDataPinPad(EventDataPinPad eventDataPinPad) {

    }

    @Override
    public void onEventDataKeyPad(EventDataKeyPad eventDataKeyPad) {

    }

    @Override
    public void onEventDataSignature(EventDataSignature eventDataSignature) {

    }

    @Override
    public void onEventDataPinPadEncrypted(EventDataPinPadEncrypted eventDataPinPadEncrypted) {

    }

    @Override
    public void onEventDataKeyPadEncrypted(EventDataKeyPadEncrypted eventDataKeyPadEncrypted) {

    }

    @Override
    public void onEventDataSignatureEncrypted(EventDataSignatureEncrypted eventDataSignatureEncrypted) {

    }

    @Override
    public void onDevicePublicKey(DevicePublicKey devicePublicKey) {

    }

    @Override
    public void onEncryptionStatus(EncryptionStatus encryptionStatus) {

    }
}
