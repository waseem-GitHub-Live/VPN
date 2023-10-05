package com.xilli.stealthnet.model

import android.os.Parcel
import android.os.Parcelable

class Countries : Parcelable {
    var type: Int = 0
    var country: String? = null
    var flagUrl: String? = null
    var ovpn: String? = null
    var ovpnUserName: String? = null
    var ovpnUserPassword: String? = null
    var signal: Int = 0
    var crown: Int = 0
    var radiobutton: Boolean = false

    constructor() {}

    constructor(country: String?, flagUrl: String?, ovpn: String?) {
        this.country = country
        this.flagUrl = flagUrl
        this.ovpn = ovpn
    }

    constructor(country: String?, flagUrl: String?, ovpn: String?, ovpnUserName: String?, ovpnUserPassword: String?) {
        this.country = country
        this.flagUrl = flagUrl
        this.ovpn = ovpn
        this.ovpnUserName = ovpnUserName
        this.ovpnUserPassword = ovpnUserPassword
    }

    protected constructor(`in`: Parcel) {
        country = `in`.readString()
        flagUrl = `in`.readString()
        ovpn = `in`.readString()
        ovpnUserName = `in`.readString()
        ovpnUserPassword = `in`.readString()
        signal = `in`.readInt() // Read as Int
        crown = `in`.readInt() // Read as Int
    }


    override fun describeContents(): Int {
        return 0
    }
    fun getCountry1(): String? {
        return country
    }
    fun getFlagUrl1(): String? {
        return flagUrl
    }
    fun getOvpnUserName1(): String? {
        return ovpnUserName
    }
    fun getOvpnUserPassword1(): String? {
        return ovpnUserPassword
    }
    fun getOvpn1(): String? {
        return ovpn
    }
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(country)
        dest.writeString(flagUrl)
        dest.writeString(ovpn)
        dest.writeString(ovpnUserName)
        dest.writeString(ovpnUserPassword)
        dest.writeInt(signal)
        dest.writeInt(crown)
    }

    companion object CREATOR : Parcelable.Creator<Countries> {
        override fun createFromParcel(parcel: Parcel): Countries {
            return Countries(parcel)
        }

        override fun newArray(size: Int): Array<Countries?> {
            return arrayOfNulls(size)
        }
    }
}
