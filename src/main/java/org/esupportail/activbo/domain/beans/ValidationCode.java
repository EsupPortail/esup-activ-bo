package org.esupportail.activbo.domain.beans;

import org.esupportail.activbo.exceptions.UserPermissionException;

public interface ValidationCode{

    class UserData {
        public String code;
        public String date;
        String channel; // optional
    }

    public boolean verify(String id,String code) throws UserPermissionException;        
    
    public UserData generateChannelCode(String id,int codeDelay, String channelName);

    // code envoyé directement à l'utilisateur (quand il a donné son mot de passe ou qque infos pour un compte non activé)
    public String generateCode(String id);
    
    public void removeCode(String userId);

    public void afterRemoveCode();
    
}
