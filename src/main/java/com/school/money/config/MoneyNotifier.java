package com.school.money.config;

import com.school.money.MoneyProperties;
import com.school.money.port.StaffDirectoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Plain-text email alerts. Never blocks or breaks a payment: mail is sent on a background thread,
 * AFTER the database transaction commits (so nobody is told about something that was rolled back),
 * and any failure is only logged. Does nothing when no mail server is configured.
 */
@Component
public class MoneyNotifier {

    private static final Logger log = LoggerFactory.getLogger(MoneyNotifier.class);

    private final ObjectProvider<JavaMailSender> mail;
    private final StaffDirectoryPort directory;
    private final MoneyProperties props;
    private final ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "money-notifier");
        t.setDaemon(true);
        return t;
    });

    public MoneyNotifier(ObjectProvider<JavaMailSender> mail, StaffDirectoryPort directory, MoneyProperties props) {
        this.mail = mail;
        this.directory = directory;
        this.props = props;
    }

    /** To one person (their login email). */
    public void toUser(String email, String subject, String body) {
        if (email == null || email.isBlank()) return;
        later(() -> deliver(Set.of(email), subject, body));
    }

    /** To everyone holding one of these roles. */
    public void toRoles(String subject, String body, String... roles) {
        later(() -> {
            Set<String> to = new LinkedHashSet<>(directory == null ? java.util.List.of() : directory.emailsOfRoles(roles));
            deliver(to, subject, body);
        });
    }

    private void later(Runnable r) {
        if (props == null || !props.getNotify().isEnabled() || mail == null) return;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { pool.submit(r); }
            });
        } else {
            pool.submit(r);
        }
    }

    private void deliver(Set<String> to, String subject, String body) {
        try {
            JavaMailSender sender = mail.getIfAvailable();
            if (sender == null || to.isEmpty()) return;
            String school = props.getNotify().getSchoolName();
            for (String address : to) {
                SimpleMailMessage m = new SimpleMailMessage();
                if (!props.getNotify().getFrom().isBlank()) m.setFrom(props.getNotify().getFrom());
                m.setTo(address);
                m.setSubject("[" + school + "] " + subject);
                m.setText(body + "\n\n-- " + school + " finance system");
                sender.send(m);
            }
        } catch (Exception e) {
            log.warn("Could not send notification '{}': {}", subject, e.getMessage());
        }
    }
}