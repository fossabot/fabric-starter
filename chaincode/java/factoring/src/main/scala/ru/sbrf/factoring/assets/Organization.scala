package ru.sbrf.factoring.assets

//case class Organization (id: String ,mspId: String, role: String, name: String = "*")

case class Organization(id: String,
                        mspId: String,
                        name: String,
                        role: String,
                        peerIpAddress: String = "",
                        peerDNSName: String = "",
                        isMe: Boolean = false)
