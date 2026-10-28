package com.satishnada.scopevpn.data.vpn

import com.satishnada.scopevpn.domain.model.AppRoutingPolicy
import com.satishnada.scopevpn.domain.model.RoutingMode
import com.wireguard.config.Config
import com.wireguard.config.Interface as WireGuardInterface

internal fun Config.withRoutingPolicy(policy: AppRoutingPolicy): Config {
    policy.validationError()?.let { message -> throw IllegalArgumentException(message) }

    val original = getInterface()
    val interfaceBuilder = WireGuardInterface.Builder()
        .addAddresses(original.getAddresses())
        .addDnsServers(original.getDnsServers())
        .addDnsSearchDomains(original.getDnsSearchDomains())
        .setKeyPair(original.getKeyPair())

    original.getListenPort().ifPresent { port -> interfaceBuilder.setListenPort(port) }
    original.getMtu().ifPresent { mtu -> interfaceBuilder.setMtu(mtu) }

    val packages = policy.selectedPackages.sorted()
    when (policy.mode) {
        RoutingMode.ALL_APPS -> Unit
        RoutingMode.ONLY_SELECTED -> interfaceBuilder.includeApplications(packages)
        RoutingMode.EXCLUDE_SELECTED -> interfaceBuilder.excludeApplications(packages)
    }

    return Config.Builder()
        .setInterface(interfaceBuilder.build())
        .addPeers(getPeers())
        .build()
}
