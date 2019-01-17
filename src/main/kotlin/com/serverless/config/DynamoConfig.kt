package com.serverless.config

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.model.*

class DynamoDBAdapter{
    private val client = AmazonDynamoDBClientBuilder.defaultClient()
            //.withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://172.16.123.1:8000", Regions.EU_CENTRAL_1.name))

    private val mapperConfig = DynamoDBMapperConfig.Builder().apply {
        saveBehavior = DynamoDBMapperConfig.SaveBehavior.PUT
    }.build()

    val dynamoDb = DynamoDB(client)
    val dynamoDbMapper = DynamoDBMapper(client, mapperConfig)
    companion object {
        const val CONFIRMED_INDEX = "ConfirmedIndex"
    }
    fun createReservationTable(){

        val attributeDefs = listOf(
                AttributeDefinition().withAttributeName("Id").withAttributeType("S"),
                AttributeDefinition().withAttributeName("Date_Time").withAttributeType("S"),
                AttributeDefinition().withAttributeName("Confirmed").withAttributeType("N")
        )
        val tableKeySchema = listOf(
                KeySchemaElement().withAttributeName("Id").withKeyType(KeyType.HASH),
                KeySchemaElement().withAttributeName("Date_Time").withKeyType(KeyType.RANGE)
        )

        val globalIndex = GlobalSecondaryIndex()
                .withIndexName(CONFIRMED_INDEX)
                .withProvisionedThroughput(ProvisionedThroughput()
                        .withReadCapacityUnits( 10)
                        .withWriteCapacityUnits(1))
                        .withProjection(Projection().withProjectionType(ProjectionType.ALL))

        val indexKeySchema = listOf(
                KeySchemaElement().withAttributeName("Confirmed").withKeyType(KeyType.HASH),
                KeySchemaElement().withAttributeName("Date_Time").withKeyType(KeyType.RANGE)
        )
        globalIndex.setKeySchema(indexKeySchema)

        val createTableRequest = CreateTableRequest()
                .withTableName("Reservation")
                .withProvisionedThroughput(ProvisionedThroughput().withReadCapacityUnits(5).withWriteCapacityUnits(1))
                .withAttributeDefinitions(attributeDefs)
                .withKeySchema(tableKeySchema)
                .withGlobalSecondaryIndexes(globalIndex)
        try {
            println("Start create")
            val table: Table = dynamoDb.createTable(createTableRequest)
            table.waitForActive()
            println("Create success")
        }catch (e: Exception){
            println("Create error: \n ${e.message}")
        }

    }
    fun dropReservationTable(){
        val tableName = "Reservation"
        val table = dynamoDb.getTable(tableName)

        try{
            table.delete()
            println("Table deleted")
        }catch (e: Exception){
            println("Exception dropping table: ${e.message}")
        }
    }
    data class PropertyMapping(val name: String,val key: KeyType?, val type: String)
}