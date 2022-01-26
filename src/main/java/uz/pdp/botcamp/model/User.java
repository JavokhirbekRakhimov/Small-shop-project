package uz.pdp.botcamp.model;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.botcamp.constants.ConstantWord;
import uz.pdp.botcamp.constants.UserLastOperation;
import uz.pdp.botcamp.constants.UserRole;

import java.util.ArrayList;
import java.util.List;

@Data
public class User {
    private Long userId;
    private String firstName;
    private String lastName = ConstantWord.DEFAULT_WORD;
    private String userName = ConstantWord.DEFAULT_WORD;
    private String phoneNumber ;

    private List<UserRole> role = new ArrayList<>(List.of(UserRole.USER));
    private boolean active = true;
    private UserLastOperation lastOperation = UserLastOperation.START;

    public User(Message message, Contact contact) {
        this.userId = message.getChatId();

        this.firstName = message.getFrom().getFirstName();

        if (message.getFrom().getLastName() != null)
            this.lastName = message.getFrom().getLastName();

        if (message.getFrom().getUserName() != null)
            this.userName = message.getFrom().getUserName();
        String phoneNumber = contact.getPhoneNumber();
        if (!phoneNumber.contains("+"))
            this.phoneNumber = "+" + contact.getPhoneNumber();
        else
            this.phoneNumber = contact.getPhoneNumber();
    }
}
