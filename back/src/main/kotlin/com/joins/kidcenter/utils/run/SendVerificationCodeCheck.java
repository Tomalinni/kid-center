package com.joins.kidcenter.utils.run;

import com.joins.kidcenter.service.sms.senders.*;

import static com.joins.kidcenter.service.sms.senders.SmsProviderOperationStatus.success;

public class SendVerificationCodeCheck {
    public static void main(String[] args) {
        SmsSender smsSender = new SmsSenderCr6868();
//        SmsSender smsSender = new SmsSenderYuntongxun(new ObjectMapper());
//        SmsSender smsSender = new SmsSenderNetease()();

        SmsProviderResponse<VerificationCodeResponseData> response = smsSender.sendConfirmMobileCode("18620045470");
        SmsProviderOperationStatus status = response.getStatus();
        if (success.equals(status)) {
            System.out.printf("Verification code was sent: %s", response.getData());
        } else {
            System.out.printf("Verification code was NOT sent, sending status: %s", status);
        }

    }
}
