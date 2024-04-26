package com.example.ihomie

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class Property(
    val zpid: Int?,
    val price:Int?,
    val address: Address?,
    val propertyTaxRate: Double?,
    val contactRecipients: List<ContactRecipient>?,
    val listed_by: ListedBy?,
    @field:JsonAdapter(OpenHouseScheduleDeserializer::class)
    val openHouseSchedule: List<OpenHouseSchedule>?,
    val longitude: Double?,
    val latitude: Double?,
    val zestimate: Int?,
    val description: String?,
    val resoFacts: ResoFacts?,
    val homeType: String?,
    val rentZestimate: Int?,
    val bedrooms:Int?,
    val bathrooms: Int?,
    val annualHomeownersInsurance: Int?,
    val mortgageRates: MortgageRates?,
    val datePosted: String?,
    val priceHistory: List<PriceHistory>?,
    val schools: List<School>?,
    val isListedByOwner: Boolean?,
    val listingSubtype: ListingSubtype?,
    val homeStatus: String?,
    val imageUrl: String?
)

data class ContactRecipient(
    val displayName: String?,
    val badgeType: String?,
    val phone: Phone?
)
data class ListedBy(
    val agent_reason: Int?,
    val zpro: Boolean?,
    val recent_sales: Int?,
    val review_count: Int?,
    val display_name: String?,
    val badge_type: String?,
    val business_name: String?,
    val rating_average: Int?,
    val phone: Phone?
)

data class Phone(
    val prefix: String?,
    val areacode: String?,
    val number: String?
)

data class ResoFacts(
    val hasAttachedProperty: Boolean?,
    val poolFeatures: List<String>?,
    val flooring: List<String>?,
    val hasGarage: Boolean?,
    val hasPetsAllowed: Boolean?,
    val waterViewYN: Boolean?,
    val atAGlanceFacts: List<FactItem>?,
    val belowGradeFinishedArea: String?,
    val feesAndDues:  Any?=null,
    val hasPrivatePool: Boolean?,
    val hasFireplace: Boolean?,
    val waterSource: List<String>?,
    val gas: List<String>?,
    val sewer:List<String>?,
    val lotSize: String?,
    val stories: Int?,
    val bathrooms: Int?,
    val heating: List<String>?,
    val cooling: List<String>?,
    val hasAttachedGarage: Boolean?,
    val bedrooms: Int?,
    val architecturalStyle: String?,
    val structureType: String?,
    val interiorFeatures: List<String>?,
    val electric: List<String>?,
    val roofType: String?,
    val taxAnnualAmount: Int?,
    val hasHeating: Boolean?,
    val constructionMaterials: Any? = null,
    val basementYN: Boolean?,
    val hoaFeeTotal: String?,
    val appliances: List<String>?,
    val fencing: String?,
    val yearBuiltEffective: String?,
    val hasCooling: Boolean?,
    val foundationDetails: Any? = null,
    val otherFacts: Any?=null,
    val isSeniorCommunity: Boolean?,
    val parcelNumber: String?,
    val yearBuilt: Int?,
    val parkingFeatures: Any?,
    val zoning: String?,
    val hoaFee: String?,
    val availabilityDate: String?,
    val securityFeatures: List<String>?,
    val attic: String?,
    val fireplaceFeatures: List<String>?,
    val mainLevelBathrooms: String?,
    val doorFeatures:List<String>?,
    val ownershipType: String?,
    val associationName: String?,
    val waterView: String?,
    val garageParkingCapacity: String?,
    val carportParkingCapacity:String?,
    val laundryFeatures: List<String>?,
    val homeType: String?
)

data class FactItem(
    val factValue: String?,
    val factLabel: String?
)

data class OtherFacts(
    val value: String?,
    val name: String?
)

data class feesAndDues(
    val phone: String?,
    val name: String?,
    val fee: String?,
    val type: String?
)
data class MortgageRates(
    val thirtyYearFixedRate: Double?
)

data class PriceHistory(
    val date: String?,
    val event: String?,
    val price: Int?
)

data class School(
    val rating: Int?,
    val distance: Double?,
    val name: String?,
    val level: String?,
    val grades: String?,
    val type: String?
)

data class ListingSubtype(
    val isFSBA: Boolean?,
    val iscomingSoon:Boolean?,
    val isnewHome: Boolean?,
    val ispending: Boolean?,
    val isforAuction: Boolean?,
    val isforeclosure: Boolean?,
    val isbankOwned: Boolean?,
    val isopenHouse: Boolean?,
    val isFSBO: Boolean?

)

data class OpenHouseSchedule(
    val startTime: String?,
    val endTime: String?
)

data class Address(
    val city: String,
    val state: String,
    val streetAddress: String,
    val zipcode: String
)