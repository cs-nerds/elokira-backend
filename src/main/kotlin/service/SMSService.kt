package service

import com.africastalking.SmsService
import com.africastalking.AfricasTalking
import com.africastalking.sms.Recipient
import io.ktor.config.*

class SMSService(
    config: ApplicationConfig
) {

    init {
        val userName = config.property("sms.atUsername").getString()
        val apiKey = config.property("sms.atApiKey").getString()
        AfricasTalking.initialize(userName,apiKey)
    }

    private var sms: SmsService = AfricasTalking.getService(AfricasTalking.SERVICE_SMS)

    fun send(message: String, recipients: List<String>): List<Recipient> {
        return sms.send(message, recipients.toTypedArray(), true)
    }
}