package com.github.K0zka.kerub.host

import org.apache.sshd.ClientSession

/**
 * Service to help creating ssh client sessions on hosts
 */
public interface SshClientService {
	/**
	 * Create an unauthenticated session
	 */
	fun createSession(address: String, userName : String) : ClientSession

	/**
	 * Create an authenticated session with public key authentication
	 */
	fun loginWithPublicKey(address: String, userName : String = "root") : ClientSession

	/**
	 * Create an authenticated session with
	 */
	fun loginWithPassword(address: String, userName : String, password : String) : ClientSession

	/**
	 * Install public key on a host.
	 */
	fun installPublicKey(session: ClientSession)

	/**
	 * Get the OpenSSH format of public key.
	 */
	fun getPublicKey() : String
}