package com.obc.palmares.web.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service 
public class MailService {
    public final JavaMailSender mail;

    public MailService(JavaMailSender mail){
        this.mail = mail;
    }

    public void envoyerConfirmationModification(String email, String prenom, String lien) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("OBC Palmarès — Confirmez votre modification");
        msg.setText("""
            Bonjour %s,

            Vous avez demandé à modifier vos informations sur OBC Palmarès.
            Pour confirmer, cliquez sur le lien ci-dessous (valable 30 minutes) :

            %s

            Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.
            """.formatted(prenom, lien));
        mail.send(msg);
    }
}
