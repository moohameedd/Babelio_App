package com.example.tp;

import android.os.AsyncTask;
import android.util.Log;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    private static final String SENDER_EMAIL = "bablio.isamm@gmail.com";
    private static final String SENDER_PASS  = "zddg pjzd hsec pmmf"; // App Password

    public static void sendOrderConfirmation(String toEmail,
                                             String userName,
                                             List<Book> items,
                                             double subtotal) {
        new SendEmailTask(toEmail, userName, items, subtotal).execute();
    }

    private static class SendEmailTask extends AsyncTask<Void, Void, Void> {
        private final String toEmail, userName;
        private final List<Book> items;
        private final double subtotal;

        SendEmailTask(String toEmail, String userName, List<Book> items, double subtotal) {
            this.toEmail = toEmail;
            this.userName = userName;
            this.items = items;
            this.subtotal = subtotal;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASS);
                }
            });

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
                message.setSubject("✅ Order Confirmation - Babelio");
                message.setContent(buildHtmlBody(userName, items, subtotal), "text/html; charset=utf-8");

                Transport.send(message);
            } catch (MessagingException e) {
                Log.e("EmailSender", "Error sending email", e);
            }
            return null;
        }
    }

    private static String buildHtmlBody(String userName, List<Book> items, double subtotal) {
        double delivery = 10.0;
        double total    = subtotal + delivery;

        StringBuilder rows = new StringBuilder();
        for (Book item : items) {
            rows.append("<tr>")
                .append("<td style='padding:8px;border-bottom:1px solid #2c2f3e;color:#fff'>")
                .append(item.getTitle()).append("</td>")
                .append("<td style='padding:8px;border-bottom:1px solid #2c2f3e;color:#fff;text-align:center'>1</td>")
                .append("<td style='padding:8px;border-bottom:1px solid #2c2f3e;color:#fff;text-align:right'>")
                .append(String.format(Locale.getDefault(), "%.2f DT", item.getPrice()))
                .append("</td></tr>");
        }

        return "<!DOCTYPE html><html><body style='background:#13162A;font-family:sans-serif;padding:20px'>"
             + "<div style='max-width:600px;margin:auto;background:#1A1D2D;border-radius:12px;padding:30px'>"
             + "<h2 style='color:#F35A72;text-align:center'> Babelio</h2>"
             + "<h3 style='color:#fff'>Hello " + (userName != null ? userName : "User") + ",</h3>"
             + "<p style='color:#aaa'>Your order has been placed successfully. Here is your facture:</p>"
             + "<table width='100%' style='border-collapse:collapse;margin-top:16px'>"
             + "<thead><tr style='background:#2C2F3E'>"
             + "<th style='padding:10px;color:#F35A72;text-align:left'>Book</th>"
             + "<th style='padding:10px;color:#F35A72;text-align:center'>Qty</th>"
             + "<th style='padding:10px;color:#F35A72;text-align:right'>Price</th>"
             + "</tr></thead><tbody>" + rows + "</tbody></table>"
             + "<hr style='border-color:#2C2F3E;margin-top:16px'>"
             + "<table width='100%'>"
             + "<tr><td style='color:#aaa;padding:4px'>Subtotal</td>"
             + "    <td style='color:#fff;text-align:right;padding:4px'>" + String.format(Locale.getDefault(), "%.2f DT", subtotal) + "</td></tr>"
             + "<tr><td style='color:#aaa;padding:4px'>Delivery</td>"
             + "    <td style='color:#fff;text-align:right;padding:4px'>10.00 DT</td></tr>"
             + "<tr style='background:#2C2F3E'>"
             + "  <td style='color:#F35A72;padding:8px;font-weight:bold;font-size:16px'>TOTAL</td>"
             + "  <td style='color:#F35A72;text-align:right;padding:8px;font-weight:bold;font-size:16px'>"
             + String.format(Locale.getDefault(), "%.2f DT", total) + "</td></tr>"
             + "</table>"
             + "<p style='color:#aaa;margin-top:20px;font-size:12px;text-align:center'>Thank you for shopping with Babelio!huu</p>"
             + "</div></body></html>";
    }
}
