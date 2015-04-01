package com.raritan.tdz.security;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.jaas.AuthorityGranter;

/**
 * A simple Authority Granter implementation to be used with JAAS for Bonita.
 * @author Andrew Cohen
 * @version 3.0
 */
public class BonitaAuthorityGranter implements AuthorityGranter {

	@Override
	public Set<String> grant(Principal principal) {
		Set<String> roles = new HashSet<String>();
		if (principal.getName().equals("bonita")) {
			roles.add("Bonita");
		}
		return roles;
	}
}
