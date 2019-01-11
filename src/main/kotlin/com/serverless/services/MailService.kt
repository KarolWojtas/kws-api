package com.serverless.services

import com.serverless.config.GmailAdapter

interface MailService{
    fun testReturnLabels(): String
}
class MailServiceImpl(val gmailAdapter: GmailAdapter): MailService{
    override fun testReturnLabels():String {
        val listResponse = gmailAdapter.service.users().labels().list("me").execute()
        val labels = listResponse.labels
        return if(labels.isNotEmpty()){
                    labels.joinToString { "${it.name}, " }
                } else {
                    "No labels"
                }
    }
}