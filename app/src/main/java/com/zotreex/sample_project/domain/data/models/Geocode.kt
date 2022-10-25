package com.zotreex.sample_project.domain.data.models

import com.google.gson.annotations.SerializedName

data class Geocode (val response: Response)

data class Response (
    @SerializedName("GeoObjectCollection")
    val geoObjectCollection: GeoObjectCollection
)

data class GeoObjectCollection (
    val metaDataProperty: GeoObjectCollectionMetaDataProperty,
    val featureMember: List<FeatureMember>
)

data class FeatureMember (
    @SerializedName("GeoObject")
    val geoObject: GeoObject
)

data class GeoObject (
    val metaDataProperty: GeoObjectMetaDataProperty,
    val name: String,
    val description: String? = null,
    val boundedBy: BoundedBy,

    @SerializedName("Point")
    val point: Point
)

data class BoundedBy (
    @SerializedName("Envelope")
    val envelope: Envelope
)

data class Envelope (
    val lowerCorner: String,
    val upperCorner: String
)

data class GeoObjectMetaDataProperty (
    @SerializedName("GeocoderMetaData")
    val geocoderMetaData: GeocoderMetaData
)

data class GeocoderMetaData (
    val precision: Kind,
    val text: String,
    val kind: Kind,

    @SerializedName("Address")
    val address: Address,

    @SerializedName("AddressDetails")
    val addressDetails: AddressDetails
)

data class Address (
    @SerializedName("country_code")
    val countryCode: String? = null,

    val formatted: String,

    @SerializedName("Components")
    val components: List<Component>
)

data class Component (
    val kind: Kind,
    val name: String
)

enum class Kind(val value: String) {
    Area("area"),
    Country("country"),
    Other("other"),
    Province("province");

    companion object {
        public fun fromValue(value: String): Kind = when (value) {
            "area"     -> Area
            "country"  -> Country
            "other"    -> Other
            "province" -> Province
            else       -> throw IllegalArgumentException()
        }
    }
}

data class AddressDetails (
    @SerializedName("Country")
    val country: AddressDetailsCountry? = null,

    @SerializedName("Address")
    val address: String? = null
)

data class AddressDetailsCountry (
    @SerializedName("AddressLine")
    val addressLine: String,

    @SerializedName("CountryNameCode")
    val countryNameCode: String,

    @SerializedName("CountryName")
    val countryName: String,

    @SerializedName("AdministrativeArea")
    val administrativeArea: AdministrativeArea? = null,

    @SerializedName("Country")
    val country: CountryCountry? = null
)

data class AdministrativeArea (
    @SerializedName("AdministrativeAreaName")
    val administrativeAreaName: String,

    @SerializedName("SubAdministrativeArea")
    val subAdministrativeArea: SubAdministrativeArea? = null
)

data class SubAdministrativeArea (
    @SerializedName("SubAdministrativeAreaName")
    val subAdministrativeAreaName: String
)

data class CountryCountry (
    @SerializedName("Locality")
    val locality: Locality
)

data class Locality (
    @SerializedName("Premise")
    val premise: Premise
)

data class Premise (
    @SerializedName("PremiseName")
    val premiseName: String
)

data class Point (
    val pos: String
)

data class GeoObjectCollectionMetaDataProperty (
    @SerializedName("GeocoderResponseMetaData")
    val geocoderResponseMetaData: GeocoderResponseMetaData
)

data class GeocoderResponseMetaData (
    @SerializedName("Point")
    val point: Point,

    val request: String,
    val results: String,
    val found: String
)