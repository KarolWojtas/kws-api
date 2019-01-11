package com.serverless.services

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64
import com.google.api.services.gmail.model.Message

import com.serverless.config.GmailAdapter
import com.serverless.domain.Reservation
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.util.*
import javax.mail.Message.RecipientType
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

interface MailService{
    fun testReturnLabels(): String
    fun sendConfirmationEmail(reservation: Reservation): Boolean
    fun sendNotificationEmail(reservation: Reservation): Boolean
    fun verifyEmail(email: String): Boolean
}
class MailServiceImpl(val gmailAdapter: GmailAdapter): MailService{
    private val MY_EMAIL = "karol.wojtas.aim@gmail.com"
    override fun testReturnLabels():String {
        val listResponse = gmailAdapter.service.users().labels().list("me").execute()
        val labels = listResponse.labels
        return if(labels.isNotEmpty()){
                    labels.joinToString { "${it.name}, " }
                } else {
                    "No labels"
                }
    }

    override fun verifyEmail(email: String): Boolean {
        var isValid = false
        try{
            val internetAddress = InternetAddress(email)
            internetAddress.validate()
            isValid = true
        } catch (e: AddressException){
            println("Email: $email doesn't exist")
        }
        return isValid
    }
    override fun sendConfirmationEmail(reservation: Reservation):Boolean {
        val mimeMessage = createConfirmationMimeMessage(reservation)
        return sendEmail(mimeMessage)
    }

    override fun sendNotificationEmail(reservation: Reservation):Boolean {
        val mimeMessage = createNotificationMimeMessage(reservation)
        return sendEmail(mimeMessage)
    }
    private fun sendEmail(mimeMessage: MimeMessage): Boolean{
        try{
            val buffer = ByteArrayOutputStream()
            mimeMessage.writeTo(buffer)
            val bytes = buffer.toByteArray()
            val message = Message().apply {
                raw = Base64.encodeBase64URLSafeString(bytes)
            }
            gmailAdapter.service.users().messages().send("me",message).execute()
            return true
        } catch (e: IOException){
            println("${e.message}")
        } catch (e: MessagingException){
            println("${e.message}")
        }
        return false
    }
    private fun createConfirmationMimeMessage(reservation: Reservation): MimeMessage{
        val props = Properties()
        val session = Session.getDefaultInstance(props)
        return MimeMessage(session).apply {
            addRecipient(RecipientType.TO, InternetAddress(reservation.email))
            subject = "Potwierdzenie rezerwacji"
            setText( emailText(reservation),"utf-8", "html")
        }
    }
    private fun createNotificationMimeMessage(reservation: Reservation): MimeMessage{
        val props = Properties()
        val session = Session.getDefaultInstance(props)
        return MimeMessage(session).apply {
            addRecipient(RecipientType.TO, InternetAddress(MY_EMAIL))
            subject = "Nowa rezerwacja"
            setText(emailText(reservation = reservation,header = "Rezerwacja ze strony", footer = "Utworzona: ${reservation.created}" ), "utf-8", "html")
        }
    }
    private fun emailText(reservation: Reservation, header: String = "Rezerwacja przyjęta, poniżej podajemy szczegóły", footer: String="Dziękujemy, restauracja Kluska"): String{
        return """
<head>
    <style>
        body {
              margin: 0;
              padding: 0;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto", "Oxygen", "Cantarell", "Fira Sans", "Droid Sans", "Helvetica Neue",
                sans-serif;
              -webkit-font-smoothing: antialiased;
              -moz-osx-font-smoothing: grayscale;
            }
        .root{
            padding: 16px;
            text-align: center;
            background-color: whitesmoke;
        }
        .content{
            width: 100%;
            height: auto;
            margin: auto;
            background-color: white;
            text-align: center;

        }
        .header{
            font-family: "Amatic SC",-apple-system,BlinkMacSystemFont, "Oswald", "Roboto", "Oxygen", "Cantarell", "Fira Sans", "Droid Sans", "Helvetica Neue", sans-serif;
            font-size: 2rem;
        }
        .footer{
            font-family: "Amatic SC",-apple-system,BlinkMacSystemFont, "Oswald", "Roboto", "Oxygen", "Cantarell", "Fira Sans", "Droid Sans", "Helvetica Neue", sans-serif;
            font-size: 1.5rem;
            text-align: end;
        }
        table {
            display: inline-block;
        }
        table, th, td{
            border: 1px solid black;
            border-collapse: collapse;
        }
        th, td{
           padding: 8px;
           text-align: center;
        }


    </style>
    <meta charset="UTF-8">
</head>
<body>
    <div class="root">
        <h3 class="header">$header</h3>
        <div class="content">
            <table>
                <thead>
                    <tr>
                        <th colspan="2">Sczegóły</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <th>Email</th>
                        <td>${reservation.email}</td>
                    </tr>
                    <tr>
                        <th>Data</th>
                        <td>${DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(reservation.date)}</td>
                    </tr>
                    <tr>
                        <th>Ilość miejsc</th>
                        <td>${reservation.seats}</td>
                    </tr>
                    <tr>
                        <th>Opis</th>
                        <td>${if(reservation.description != null) reservation.description else "Brak"}</td>
                    </tr>
                </tbody>
            </table>
        </div>
        <h4 class="footer">$footer</h4>
    </div>
</body>

        """.trimIndent()
    }

}