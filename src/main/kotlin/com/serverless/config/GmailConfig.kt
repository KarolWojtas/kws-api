package com.serverless.config

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import java.io.File
import java.io.InputStreamReader
import java.util.*

class GmailAdapter{

    companion object {
        private val SCOPES = listOf(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_SEND)
        private val CREDENTIALS_FILE_PATH = "/credentials.json"
        private val TOKENS_DIRECTORY_PATH = "tokens"
        private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
        private val APP_NAME = "kws-api"
        private fun getCredentials(httpTransport: NetHttpTransport): Credential{
            val inputStream = GmailAdapter.javaClass.getResourceAsStream(CREDENTIALS_FILE_PATH)
            val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))
            val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).apply {
                setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
                accessType = "offline"
            }.build()
            val receiver = LocalServerReceiver.Builder().apply {
                port = 8888
            }.build()
            return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
        }
    }
    private val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
    val service = Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).apply {
        applicationName = APP_NAME
    }.build()

}