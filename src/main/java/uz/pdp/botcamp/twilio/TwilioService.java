package uz.pdp.botcamp.twilio;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import uz.pdp.botcamp.repository.DataBase;

public class TwilioService {
    public static void sentSMSCode(Long messeageId, String phoneNumber) {
        Twilio.init(TwilioKey.Account_SID, TwilioKey.Auth_Token);
        int randomCode= (int) (Math.random()*89999+10000);
        Message message=Message.creator(new PhoneNumber(phoneNumber),
                new PhoneNumber("+14632192160"),"Verification code: "+randomCode).create();
        DataBase.smsCode.put(messeageId,randomCode);

    }

}
